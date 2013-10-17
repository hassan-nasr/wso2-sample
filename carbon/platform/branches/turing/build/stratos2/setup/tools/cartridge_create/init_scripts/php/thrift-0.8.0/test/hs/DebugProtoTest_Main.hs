--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership. The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License. You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied. See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

module Main where


import qualified Control.Exception
import qualified Data.ByteString.Lazy as DBL
import qualified Maybe
import qualified Network

import Thrift.Protocol.Binary
import Thrift.Server
import Thrift.Transport.Handle

import qualified ThriftTestUtils

import qualified DebugProtoTest_Types as Types
import qualified Inherited
import qualified Inherited_Client as IClient
import qualified Inherited_Iface as IIface
import qualified Srv_Client as SClient
import qualified Srv_Iface as SIface

-- we don't actually need this import, but force it to check the code generator exports proper Haskell syntax
import qualified Srv()


data InheritedHandler = InheritedHandler
instance SIface.Srv_Iface InheritedHandler where
    janky _ arg = do
        ThriftTestUtils.serverLog $ "Got janky method call: " ++ show arg
        return $ 31

    voidMethod _ = do
        ThriftTestUtils.serverLog "Got voidMethod method call"
        return ()

    primitiveMethod _ = do
        ThriftTestUtils.serverLog "Got primitiveMethod call"
        return $ 42

    structMethod _ = do
        ThriftTestUtils.serverLog "Got structMethod call"
        return $ Types.CompactProtoTestStruct {
            Types.f_CompactProtoTestStruct_a_byte = Just 0x01,
            Types.f_CompactProtoTestStruct_a_i16 = Just 0x02,
            Types.f_CompactProtoTestStruct_a_i32 = Just 0x03,
            Types.f_CompactProtoTestStruct_a_i64 = Just 0x04,
            Types.f_CompactProtoTestStruct_a_double = Just 0.1,
            Types.f_CompactProtoTestStruct_a_string = Just "abcdef",
            Types.f_CompactProtoTestStruct_a_binary = Just DBL.empty,
            Types.f_CompactProtoTestStruct_true_field = Just True,
            Types.f_CompactProtoTestStruct_false_field = Just False,
            Types.f_CompactProtoTestStruct_empty_struct_field = Just Types.Empty,
            
            Types.f_CompactProtoTestStruct_byte_list = Nothing,
            Types.f_CompactProtoTestStruct_i16_list = Nothing,
            Types.f_CompactProtoTestStruct_i32_list = Nothing,
            Types.f_CompactProtoTestStruct_i64_list = Nothing,
            Types.f_CompactProtoTestStruct_double_list = Nothing,
            Types.f_CompactProtoTestStruct_string_list = Nothing,
            Types.f_CompactProtoTestStruct_binary_list = Nothing,
            Types.f_CompactProtoTestStruct_boolean_list = Nothing,
            Types.f_CompactProtoTestStruct_struct_list = Just [Types.Empty],

            Types.f_CompactProtoTestStruct_byte_set = Nothing,
            Types.f_CompactProtoTestStruct_i16_set = Nothing,
            Types.f_CompactProtoTestStruct_i32_set = Nothing,
            Types.f_CompactProtoTestStruct_i64_set = Nothing,
            Types.f_CompactProtoTestStruct_double_set = Nothing,
            Types.f_CompactProtoTestStruct_string_set = Nothing,
            Types.f_CompactProtoTestStruct_binary_set = Nothing,
            Types.f_CompactProtoTestStruct_boolean_set = Nothing,
            Types.f_CompactProtoTestStruct_struct_set = Nothing,

            Types.f_CompactProtoTestStruct_byte_byte_map = Nothing,
            Types.f_CompactProtoTestStruct_i16_byte_map = Nothing,
            Types.f_CompactProtoTestStruct_i32_byte_map = Nothing,
            Types.f_CompactProtoTestStruct_i64_byte_map = Nothing,
            Types.f_CompactProtoTestStruct_double_byte_map = Nothing,
            Types.f_CompactProtoTestStruct_string_byte_map = Nothing,
            Types.f_CompactProtoTestStruct_binary_byte_map = Nothing,
            Types.f_CompactProtoTestStruct_boolean_byte_map = Nothing,

            Types.f_CompactProtoTestStruct_byte_i16_map = Nothing,
            Types.f_CompactProtoTestStruct_byte_i32_map = Nothing,
            Types.f_CompactProtoTestStruct_byte_i64_map = Nothing,
            Types.f_CompactProtoTestStruct_byte_double_map = Nothing,
            Types.f_CompactProtoTestStruct_byte_string_map = Nothing,
            Types.f_CompactProtoTestStruct_byte_binary_map = Nothing,
            Types.f_CompactProtoTestStruct_byte_boolean_map = Nothing,

            Types.f_CompactProtoTestStruct_list_byte_map = Nothing,
            Types.f_CompactProtoTestStruct_set_byte_map = Nothing,
            Types.f_CompactProtoTestStruct_map_byte_map = Nothing,

            Types.f_CompactProtoTestStruct_byte_map_map = Nothing,
            Types.f_CompactProtoTestStruct_byte_set_map = Nothing,
            Types.f_CompactProtoTestStruct_byte_list_map = Nothing }

    methodWithDefaultArgs _ arg = do
        ThriftTestUtils.serverLog $ "Got methodWithDefaultArgs: " ++ show arg
        return ()

    onewayMethod _ = do
        ThriftTestUtils.serverLog "Got onewayMethod"

instance IIface.Inherited_Iface InheritedHandler where
    identity _ arg = do
        ThriftTestUtils.serverLog $ "Got identity method: " ++ show arg
        return $ Maybe.fromJust arg

client :: (String, Network.PortID) -> IO ()
client addr = do
    to <- hOpen addr
    let p =  BinaryProtocol to
    let ps = (p,p)

    v1 <- SClient.janky ps 42
    ThriftTestUtils.clientLog $ show v1

    SClient.voidMethod ps

    v2 <- SClient.primitiveMethod ps
    ThriftTestUtils.clientLog $ show v2

    v3 <- SClient.structMethod ps
    ThriftTestUtils.clientLog $ show v3

    SClient.methodWithDefaultArgs ps 42

    SClient.onewayMethod ps

    v4 <- IClient.identity ps 42
    ThriftTestUtils.clientLog $ show v4

    return ()

server :: Network.PortNumber -> IO ()
server port = do 
    ThriftTestUtils.serverLog "Ready..."
    (runBasicServer InheritedHandler Inherited.process port)
    `Control.Exception.catch`
    (\(TransportExn s _) -> error $ "FAILURE: " ++ show s)

main :: IO ()
main = ThriftTestUtils.runTest server client
