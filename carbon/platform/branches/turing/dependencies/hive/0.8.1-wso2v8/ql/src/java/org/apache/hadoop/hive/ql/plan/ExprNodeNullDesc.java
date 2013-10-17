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

package org.apache.hadoop.hive.ql.plan;

import java.io.Serializable;

import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.io.NullWritable;

/**
 * ExprNodeNullDesc.
 *
 */
public class ExprNodeNullDesc extends ExprNodeDesc implements Serializable {

  private static final long serialVersionUID = 1L;

  public ExprNodeNullDesc() {
    super(TypeInfoFactory
        .getPrimitiveTypeInfoFromPrimitiveWritable(NullWritable.class));
  }

  public Object getValue() {
    return null;
  }

  @Explain(displayName = "expr")
  @Override
  public String getExprString() {
    return "null";
  }

  @Override
  public ExprNodeDesc clone() {
    return new ExprNodeNullDesc();
  }

  @Override
  public boolean isSame(Object o) {
    if (!(o instanceof ExprNodeNullDesc)) {
      return false;
    }
    if (!typeInfo.equals(((ExprNodeNullDesc) o).getTypeInfo())) {
      return false;
    }

    return true;
  }
}
