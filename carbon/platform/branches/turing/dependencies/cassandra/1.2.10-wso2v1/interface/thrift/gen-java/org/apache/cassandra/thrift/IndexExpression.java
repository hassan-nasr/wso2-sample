/**
 * Autogenerated by Thrift Compiler (0.7.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package org.apache.cassandra.thrift;
/*
 * 
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
 * 
 */


import org.apache.commons.lang.builder.HashCodeBuilder;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexExpression implements org.apache.thrift.TBase<IndexExpression, IndexExpression._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("IndexExpression");

  private static final org.apache.thrift.protocol.TField COLUMN_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("column_name", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField OP_FIELD_DESC = new org.apache.thrift.protocol.TField("op", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("value", org.apache.thrift.protocol.TType.STRING, (short)3);

  public ByteBuffer column_name; // required
  /**
   * 
   * @see IndexOperator
   */
  public IndexOperator op; // required
  public ByteBuffer value; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    COLUMN_NAME((short)1, "column_name"),
    /**
     * 
     * @see IndexOperator
     */
    OP((short)2, "op"),
    VALUE((short)3, "value");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // COLUMN_NAME
          return COLUMN_NAME;
        case 2: // OP
          return OP;
        case 3: // VALUE
          return VALUE;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments

  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.COLUMN_NAME, new org.apache.thrift.meta_data.FieldMetaData("column_name", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.OP, new org.apache.thrift.meta_data.FieldMetaData("op", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, IndexOperator.class)));
    tmpMap.put(_Fields.VALUE, new org.apache.thrift.meta_data.FieldMetaData("value", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(IndexExpression.class, metaDataMap);
  }

  public IndexExpression() {
  }

  public IndexExpression(
    ByteBuffer column_name,
    IndexOperator op,
    ByteBuffer value)
  {
    this();
    this.column_name = column_name;
    this.op = op;
    this.value = value;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public IndexExpression(IndexExpression other) {
    if (other.isSetColumn_name()) {
      this.column_name = org.apache.thrift.TBaseHelper.copyBinary(other.column_name);
;
    }
    if (other.isSetOp()) {
      this.op = other.op;
    }
    if (other.isSetValue()) {
      this.value = org.apache.thrift.TBaseHelper.copyBinary(other.value);
;
    }
  }

  public IndexExpression deepCopy() {
    return new IndexExpression(this);
  }

  @Override
  public void clear() {
    this.column_name = null;
    this.op = null;
    this.value = null;
  }

  public byte[] getColumn_name() {
    setColumn_name(org.apache.thrift.TBaseHelper.rightSize(column_name));
    return column_name == null ? null : column_name.array();
  }

  public ByteBuffer bufferForColumn_name() {
    return column_name;
  }

  public IndexExpression setColumn_name(byte[] column_name) {
    setColumn_name(column_name == null ? (ByteBuffer)null : ByteBuffer.wrap(column_name));
    return this;
  }

  public IndexExpression setColumn_name(ByteBuffer column_name) {
    this.column_name = column_name;
    return this;
  }

  public void unsetColumn_name() {
    this.column_name = null;
  }

  /** Returns true if field column_name is set (has been assigned a value) and false otherwise */
  public boolean isSetColumn_name() {
    return this.column_name != null;
  }

  public void setColumn_nameIsSet(boolean value) {
    if (!value) {
      this.column_name = null;
    }
  }

  /**
   * 
   * @see IndexOperator
   */
  public IndexOperator getOp() {
    return this.op;
  }

  /**
   * 
   * @see IndexOperator
   */
  public IndexExpression setOp(IndexOperator op) {
    this.op = op;
    return this;
  }

  public void unsetOp() {
    this.op = null;
  }

  /** Returns true if field op is set (has been assigned a value) and false otherwise */
  public boolean isSetOp() {
    return this.op != null;
  }

  public void setOpIsSet(boolean value) {
    if (!value) {
      this.op = null;
    }
  }

  public byte[] getValue() {
    setValue(org.apache.thrift.TBaseHelper.rightSize(value));
    return value == null ? null : value.array();
  }

  public ByteBuffer bufferForValue() {
    return value;
  }

  public IndexExpression setValue(byte[] value) {
    setValue(value == null ? (ByteBuffer)null : ByteBuffer.wrap(value));
    return this;
  }

  public IndexExpression setValue(ByteBuffer value) {
    this.value = value;
    return this;
  }

  public void unsetValue() {
    this.value = null;
  }

  /** Returns true if field value is set (has been assigned a value) and false otherwise */
  public boolean isSetValue() {
    return this.value != null;
  }

  public void setValueIsSet(boolean value) {
    if (!value) {
      this.value = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case COLUMN_NAME:
      if (value == null) {
        unsetColumn_name();
      } else {
        setColumn_name((ByteBuffer)value);
      }
      break;

    case OP:
      if (value == null) {
        unsetOp();
      } else {
        setOp((IndexOperator)value);
      }
      break;

    case VALUE:
      if (value == null) {
        unsetValue();
      } else {
        setValue((ByteBuffer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case COLUMN_NAME:
      return getColumn_name();

    case OP:
      return getOp();

    case VALUE:
      return getValue();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case COLUMN_NAME:
      return isSetColumn_name();
    case OP:
      return isSetOp();
    case VALUE:
      return isSetValue();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof IndexExpression)
      return this.equals((IndexExpression)that);
    return false;
  }

  public boolean equals(IndexExpression that) {
    if (that == null)
      return false;

    boolean this_present_column_name = true && this.isSetColumn_name();
    boolean that_present_column_name = true && that.isSetColumn_name();
    if (this_present_column_name || that_present_column_name) {
      if (!(this_present_column_name && that_present_column_name))
        return false;
      if (!this.column_name.equals(that.column_name))
        return false;
    }

    boolean this_present_op = true && this.isSetOp();
    boolean that_present_op = true && that.isSetOp();
    if (this_present_op || that_present_op) {
      if (!(this_present_op && that_present_op))
        return false;
      if (!this.op.equals(that.op))
        return false;
    }

    boolean this_present_value = true && this.isSetValue();
    boolean that_present_value = true && that.isSetValue();
    if (this_present_value || that_present_value) {
      if (!(this_present_value && that_present_value))
        return false;
      if (!this.value.equals(that.value))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();

    boolean present_column_name = true && (isSetColumn_name());
    builder.append(present_column_name);
    if (present_column_name)
      builder.append(column_name);

    boolean present_op = true && (isSetOp());
    builder.append(present_op);
    if (present_op)
      builder.append(op.getValue());

    boolean present_value = true && (isSetValue());
    builder.append(present_value);
    if (present_value)
      builder.append(value);

    return builder.toHashCode();
  }

  public int compareTo(IndexExpression other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    IndexExpression typedOther = (IndexExpression)other;

    lastComparison = Boolean.valueOf(isSetColumn_name()).compareTo(typedOther.isSetColumn_name());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetColumn_name()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.column_name, typedOther.column_name);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetOp()).compareTo(typedOther.isSetOp());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOp()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.op, typedOther.op);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetValue()).compareTo(typedOther.isSetValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetValue()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.value, typedOther.value);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    org.apache.thrift.protocol.TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == org.apache.thrift.protocol.TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: // COLUMN_NAME
          if (field.type == org.apache.thrift.protocol.TType.STRING) {
            this.column_name = iprot.readBinary();
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // OP
          if (field.type == org.apache.thrift.protocol.TType.I32) {
            this.op = IndexOperator.findByValue(iprot.readI32());
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 3: // VALUE
          if (field.type == org.apache.thrift.protocol.TType.STRING) {
            this.value = iprot.readBinary();
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();

    // check for required fields of primitive type, which can't be checked in the validate method
    validate();
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    if (this.column_name != null) {
      oprot.writeFieldBegin(COLUMN_NAME_FIELD_DESC);
      oprot.writeBinary(this.column_name);
      oprot.writeFieldEnd();
    }
    if (this.op != null) {
      oprot.writeFieldBegin(OP_FIELD_DESC);
      oprot.writeI32(this.op.getValue());
      oprot.writeFieldEnd();
    }
    if (this.value != null) {
      oprot.writeFieldBegin(VALUE_FIELD_DESC);
      oprot.writeBinary(this.value);
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("IndexExpression(");
    boolean first = true;

    sb.append("column_name:");
    if (this.column_name == null) {
      sb.append("null");
    } else {
      org.apache.thrift.TBaseHelper.toString(this.column_name, sb);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("op:");
    if (this.op == null) {
      sb.append("null");
    } else {
      sb.append(this.op);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("value:");
    if (this.value == null) {
      sb.append("null");
    } else {
      org.apache.thrift.TBaseHelper.toString(this.value, sb);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (column_name == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'column_name' was not present! Struct: " + toString());
    }
    if (op == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'op' was not present! Struct: " + toString());
    }
    if (value == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'value' was not present! Struct: " + toString());
    }
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

}

