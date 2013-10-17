#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

$:.push File.dirname(__FILE__) + '/../gen-rb'
$:.push File.join(File.dirname(__FILE__), '../../../lib/rb/lib')
$:.push File.join(File.dirname(__FILE__), '../../../lib/rb/ext')

require 'thrift'
require 'ThriftTest'

class SimpleHandler
  [:testString, :testByte, :testI32, :testI64, :testDouble,
   :testStruct, :testMap, :testSet, :testList, :testNest,
   :testEnum, :testTypedef].each do |meth|

    define_method(meth) do |thing|
      thing
    end

  end

  def testInsanity(thing)
    num, uid = thing.userMap.find { true }
    return {uid => {num => thing}}
  end

  def testMapMap(thing)
    return {thing => {thing => thing}}
  end

  def testEnum(thing)
    return thing
  end

  def testTypedef(thing)
    return thing
  end

  def testException(thing)
    raise Thrift::Test::Xception, :message => 'error'
  end
end

@handler   = SimpleHandler.new
@processor = Thrift::Test::ThriftTest::Processor.new(@handler)
@transport = Thrift::ServerSocket.new(9090)
@server    = Thrift::ThreadedServer.new(@processor, @transport, Thrift::BufferedTransportFactory.new, Thrift::BinaryProtocolAcceleratedFactory.new)

@server.serve
