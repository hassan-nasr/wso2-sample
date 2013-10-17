/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Contains some contributions under the Thrift Software License.
 * Please see doc/old-thrift-license.txt in the Thrift distribution for
 * details.
 */

using System;

namespace Thrift.Protocol
{
	public enum TType : byte
	{
		Stop = 0,
		Void = 1,
		Bool = 2,
		Byte = 3,
		Double = 4,
		I16 = 6,
		I32 = 8,
		I64 = 10,
		String = 11,
		Struct = 12,
		Map = 13,
		Set = 14,
		List = 15
	}
}
