/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cassandra.db;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

import org.junit.Test;

import org.apache.cassandra.SchemaLoader;
import org.apache.cassandra.db.columniterator.IdentityQueryFilter;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.dht.*;
import org.apache.cassandra.config.*;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.*;
import static org.apache.cassandra.Util.dk;


/**
 * Test cases where multiple keys collides, ie have the same token.
 * Order preserving partitioner have no possible collision and creating
 * collision for the RandomPartitioner is ... difficult, so we create a dumb
 * length partitioner that takes the length of the key as token, making
 * collision easy and predictable.
 */
public class KeyCollisionTest extends SchemaLoader
{
    IPartitioner oldPartitioner;
    private static final String KEYSPACE = "Keyspace1";
    private static final String CF = "Standard1";

    protected void setUp()
    {
        oldPartitioner = DatabaseDescriptor.getPartitioner();
        DatabaseDescriptor.setPartitioner(new LengthPartitioner());
    }

    protected void tearDown()
    {
        DatabaseDescriptor.setPartitioner(oldPartitioner);
    }

    @Test
    public void testGetSliceWithCollision() throws Exception
    {
        Table table = Table.open(KEYSPACE);
        ColumnFamilyStore cfs = table.getColumnFamilyStore(CF);
        cfs.clearUnsafe();

        insert("k1", "k2", "k3");       // token = 2
        insert("key1", "key2", "key3"); // token = 4
        insert("longKey1", "longKey2"); // token = 8

        List<Row> rows = cfs.getRangeSlice(null, new Bounds<RowPosition>(dk("k2"), dk("key2")), 10000, new IdentityQueryFilter(), null);
        assert rows.size() == 4 : "Expecting 4 keys, got " + rows.size();
        assert rows.get(0).key.key.equals(ByteBufferUtil.bytes("k2"));
        assert rows.get(1).key.key.equals(ByteBufferUtil.bytes("k3"));
        assert rows.get(2).key.key.equals(ByteBufferUtil.bytes("key1"));
        assert rows.get(3).key.key.equals(ByteBufferUtil.bytes("key2"));
    }

    private void insert(String... keys) throws IOException
    {
        for (String key : keys)
            insert(key);
    }

    private void insert(String key) throws IOException
    {
        RowMutation rm;
        rm = new RowMutation(KEYSPACE, ByteBufferUtil.bytes(key));
        rm.add(new QueryPath(CF, null, ByteBufferUtil.bytes("column")), ByteBufferUtil.bytes("asdf"), 0);
        rm.apply();
    }

    public static class LengthPartitioner extends AbstractPartitioner<BigIntegerToken>
    {
        public static final BigInteger ZERO = new BigInteger("0");
        public static final BigIntegerToken MINIMUM = new BigIntegerToken("-1");

        private static final byte DELIMITER_BYTE = ":".getBytes()[0];

        public DecoratedKey<BigIntegerToken> decorateKey(ByteBuffer key)
        {
            return new DecoratedKey<BigIntegerToken>(getToken(key), key);
        }

        public DecoratedKey<BigIntegerToken> convertFromDiskFormat(ByteBuffer fromdisk)
        {
            throw new UnsupportedOperationException();
        }

        public Token midpoint(Token ltoken, Token rtoken)
        {
            // the symbolic MINIMUM token should act as ZERO: the empty bit array
            BigInteger left = ltoken.equals(MINIMUM) ? ZERO : ((BigIntegerToken)ltoken).token;
            BigInteger right = rtoken.equals(MINIMUM) ? ZERO : ((BigIntegerToken)rtoken).token;
            Pair<BigInteger,Boolean> midpair = FBUtilities.midpoint(left, right, 127);
            // discard the remainder
            return new BigIntegerToken(midpair.left);
        }

        public BigIntegerToken getMinimumToken()
        {
            return MINIMUM;
        }

        public BigIntegerToken getRandomToken()
        {
            return new BigIntegerToken(BigInteger.valueOf(new Random().nextInt(15)));
        }

        private final Token.TokenFactory<BigInteger> tokenFactory = new Token.TokenFactory<BigInteger>() {
            public ByteBuffer toByteArray(Token<BigInteger> bigIntegerToken)
            {
                return ByteBuffer.wrap(bigIntegerToken.token.toByteArray());
            }

            public Token<BigInteger> fromByteArray(ByteBuffer bytes)
            {
                return new BigIntegerToken(new BigInteger(ByteBufferUtil.getArray(bytes)));
            }

            public String toString(Token<BigInteger> bigIntegerToken)
            {
                return bigIntegerToken.token.toString();
            }

            public Token<BigInteger> fromString(String string)
            {
                return new BigIntegerToken(new BigInteger(string));
            }

            public void validate(String token) {}
        };

        public Token.TokenFactory<BigInteger> getTokenFactory()
        {
            return tokenFactory;
        }

        public boolean preservesOrder()
        {
            return false;
        }

        public BigIntegerToken getToken(ByteBuffer key)
        {
            if (key.remaining() == 0)
                return MINIMUM;
            return new BigIntegerToken(BigInteger.valueOf(key.remaining()));
        }

        public Map<Token, Float> describeOwnership(List<Token> sortedTokens)
        {
            // allTokens will contain the count and be returned, sorted_ranges is shorthand for token<->token math.
            Map<Token, Float> allTokens = new HashMap<Token, Float>();
            List<Range<Token>> sortedRanges = new ArrayList<Range<Token>>();

            // this initializes the counts to 0 and calcs the ranges in order.
            Token lastToken = sortedTokens.get(sortedTokens.size() - 1);
            for (Token node : sortedTokens)
            {
                allTokens.put(node, new Float(0.0));
                sortedRanges.add(new Range<Token>(lastToken, node));
                lastToken = node;
            }

            for (String ks : Schema.instance.getTables())
            {
                for (CFMetaData cfmd : Schema.instance.getKSMetaData(ks).cfMetaData().values())
                {
                    for (Range<Token> r : sortedRanges)
                    {
                        // Looping over every KS:CF:Range, get the splits size and add it to the count
                        allTokens.put(r.right, allTokens.get(r.right) + StorageService.instance.getSplits(ks, cfmd.cfName, r, 1).size());
                    }
                }
            }

            // Sum every count up and divide count/total for the fractional ownership.
            Float total = new Float(0.0);
            for (Float f : allTokens.values())
                total += f;
            for (Map.Entry<Token, Float> row : allTokens.entrySet())
                allTokens.put(row.getKey(), row.getValue() / total);

            return allTokens;
        }
    }
}
