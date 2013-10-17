/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hive.ql.udf;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.serde2.io.ByteWritable;
import org.apache.hadoop.hive.serde2.io.DoubleWritable;
import org.apache.hadoop.hive.serde2.io.ShortWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;

/**
 * Base class for numeric operators like +, -, / etc. All these operators share
 * a common method resolver (NumericOpMethodResolver).
 */
public abstract class UDFBaseNumericUnaryOp extends UDF {

  /**
   * Constructor.
   */
  public UDFBaseNumericUnaryOp() {
    super();
  }

  protected ByteWritable byteWritable = new ByteWritable();
  protected ShortWritable shortWritable = new ShortWritable();
  protected IntWritable intWritable = new IntWritable();
  protected LongWritable longWritable = new LongWritable();
  protected FloatWritable floatWritable = new FloatWritable();
  protected DoubleWritable doubleWritable = new DoubleWritable();

  public abstract ByteWritable evaluate(ByteWritable a);

  public abstract ShortWritable evaluate(ShortWritable a);

  public abstract IntWritable evaluate(IntWritable a);

  public abstract LongWritable evaluate(LongWritable a);

  public abstract FloatWritable evaluate(FloatWritable a);

  public abstract DoubleWritable evaluate(DoubleWritable a);

}
