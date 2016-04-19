package com.google.protobench;

import benchmark.protobuf.UnittestProto;
import benchmark.protostuff.NestedTestAllTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class TestMessage {
  int optionalInt;
  long optionalLong;
  float optionalFloat;
  double optionalDouble;
  boolean optionalBoolean;
  String optionalString;
  byte[] optionalBytes;

  int[] repeatedInt;
  long[] repeatedLong;
  float[] repeatedFloat;
  double[] repeatedDouble;
  boolean[] repeatedBoolean;
  String[] repeatedString;
  byte[][] repeatedBytes;

  TestMessage[] children;

  private int serializedSize = -1;

  public void clearCachedSerializedSize() {
    serializedSize = -1;
    for(int i = 0; i<children.length; ++i) {
      children[i].clearCachedSerializedSize();
    }
  }

  public int getSerializedSize() {
    if (serializedSize == -1) {
      int size = 0;
      size += Utils.computeUInt32Size(3, optionalInt);
      size += Utils.computeUInt64Size(4, optionalLong);
      size += Utils.computeFloatSize(11, optionalFloat);
      size += Utils.computeDoubleSize(12, optionalDouble);
      size += Utils.computeBoolSize(13, optionalBoolean);
      size += Utils.computeStringSize(14, optionalString);
      size += Utils.computeByteArraySize(15, optionalBytes);

      if (children != null) {
        for (int ix = 0; ix < children.length; ++ix) {
          size += Utils.computeMessageSize(18, children[ix]);
        }
      }

      if (repeatedInt != null) {
        {
          int dataSize = 0;
          for (int i = 0; i < repeatedInt.length; i++) {
            dataSize += Utils.computeUInt32SizeNoTag(repeatedInt[i]);
          }
          size += dataSize;
          size += 2 * repeatedInt.length;
        }
      }
      if (repeatedLong != null) {
        {
          int dataSize = 0;
          for (int i = 0; i < repeatedLong.length; i++) {
            dataSize += Utils.computeUInt64SizeNoTag(repeatedLong[i]);
          }
          size += dataSize;
          size += 2 * repeatedLong.length;
        }
      }
      if (repeatedFloat != null) {
        {
          int dataSize = 0;
          for (int i = 0; i < repeatedFloat.length; i++) {
            dataSize += Utils.computeFloatSizeNoTag(repeatedFloat[i]);
          }
          size += dataSize;
          size += 2 * repeatedFloat.length;
        }
      }
      if (repeatedDouble != null) {
        {
          int dataSize = 0;
          for (int i = 0; i < repeatedDouble.length; i++) {
            dataSize += Utils.computeDoubleSizeNoTag(repeatedDouble[i]);
          }
          size += dataSize;
          size += 2 * repeatedDouble.length;
        }
      }
      if (repeatedBoolean != null) {
        {
          size += repeatedBoolean.length;
          size += 2 * repeatedBoolean.length;
        }
      }
      if (repeatedString != null) {
        {
          int dataSize = 0;
          for (int i = 0; i < repeatedString.length; i++) {
            dataSize += Utils.computeStringSizeNoTag(repeatedString[i]);
          }
          size += dataSize;
          size += 2 * repeatedString.length;
        }
      }
      if (repeatedBytes != null) {
        {
          int dataSize = 0;
          for (int i = 0; i < repeatedBytes.length; i++) {
            dataSize += Utils.computeByteArraySizeNoTag(repeatedBytes[i]);
          }
          size += dataSize;
          size += 2 * repeatedBytes.length;
        }
      }
      this.serializedSize = size;
    }

    return serializedSize;
  }

  public NestedTestAllTypes toProtostuff() {
    benchmark.protostuff.TestAllTypes proto = new benchmark.protostuff.TestAllTypes();
    proto.setOptionalUint32(optionalInt);
    proto.setOptionalUint64(optionalLong);
    proto.setOptionalFloat(optionalFloat);
    proto.setOptionalDouble(optionalDouble);
    proto.setOptionalBool(optionalBoolean);
    proto.setOptionalString(optionalString);
    proto.setOptionalBytes(io.protostuff.ByteString.copyFrom(optionalBytes));

    proto.setRepeatedUint32List(new ArrayList<Integer>(repeatedInt.length));
    proto.setRepeatedUint64List(new ArrayList<Long>(repeatedInt.length));
    proto.setRepeatedFloatList(new ArrayList<Float>(repeatedInt.length));
    proto.setRepeatedDoubleList(new ArrayList<Double>(repeatedInt.length));
    proto.setRepeatedBoolList(new ArrayList<Boolean>(repeatedInt.length));
    proto.setRepeatedStringList(new ArrayList<String>(repeatedInt.length));
    proto.setRepeatedBytesList(new ArrayList<io.protostuff.ByteString>(repeatedInt.length));

    for (int i = 0; i < repeatedInt.length; ++i) {
      proto.getRepeatedUint32List().add(repeatedInt[i]);
      proto.getRepeatedUint64List().add(repeatedLong[i]);
      proto.getRepeatedFloatList().add(repeatedFloat[i]);
      proto.getRepeatedDoubleList().add(repeatedDouble[i]);
      proto.getRepeatedBoolList().add(repeatedBoolean[i]);
      proto.getRepeatedStringList().add(repeatedString[i]);
      proto.getRepeatedBytesList().add(io.protostuff.ByteString.copyFrom(repeatedBytes[i]));
    }

    NestedTestAllTypes nested = new NestedTestAllTypes();
    nested.setPayload(proto);
    if (children != null) {
      List<NestedTestAllTypes> nestedChildren = new ArrayList<NestedTestAllTypes>(children.length);
      for (int i = 0; i < children.length; ++i) {
        nestedChildren.add(children[i].toProtostuff());
      }
      nested.setRepeatedChildList(nestedChildren);
    }
    return nested;
  }

  public UnittestProto.NestedTestAllTypes toProtobuf() {
    UnittestProto.TestAllTypes.Builder proto = UnittestProto.TestAllTypes.newBuilder();
    proto.setOptionalUint32(optionalInt);
    proto.setOptionalUint64(optionalLong);
    proto.setOptionalFloat(optionalFloat);
    proto.setOptionalDouble(optionalDouble);
    proto.setOptionalString(optionalString);
    proto.setOptionalBytes(com.google.protobuf.ByteString.copyFrom(optionalBytes));

    for (int i = 0; i < repeatedInt.length; ++i) {
      proto.addRepeatedUint32(repeatedInt[i]);
      proto.addRepeatedUint64(repeatedLong[i]);
      proto.addRepeatedFloat(repeatedFloat[i]);
      proto.addRepeatedDouble(repeatedDouble[i]);
      proto.addRepeatedBool(repeatedBoolean[i]);
      proto.addRepeatedString(repeatedString[i]);
      proto.addRepeatedBytes(com.google.protobuf.ByteString.copyFrom(repeatedBytes[i]));
    }

    UnittestProto.NestedTestAllTypes.Builder nested = UnittestProto.NestedTestAllTypes.newBuilder();
    nested.setPayload(proto.build());
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        nested.addRepeatedChild(children[i].toProtobuf());
      }
    }
    return nested.build();
  }

  public static TestMessage newRandomInstance(Random r,
                                              int depth,
                                              int stringLength,
                                              int numRepeatedFields,
                                              int treeHeight,
                                              int branchingFactor) {
    TestMessage info = new TestMessage();
    info.optionalInt = r.nextInt();
    info.optionalLong = r.nextLong();
    info.optionalFloat = r.nextFloat();
    info.optionalDouble = r.nextDouble();
    info.optionalBoolean = r.nextBoolean();
    info.optionalString = Utils.randomString(r, stringLength);
    info.optionalBytes = Utils.randomString(r, stringLength).getBytes();

    info.repeatedInt = new int[numRepeatedFields];
    info.repeatedLong = new long[numRepeatedFields];
    info.repeatedFloat = new float[numRepeatedFields];
    info.repeatedDouble = new double[numRepeatedFields];
    info.repeatedBoolean = new boolean[numRepeatedFields];
    info.repeatedString = new String[numRepeatedFields];
    info.repeatedBytes = new byte[numRepeatedFields][];

    for (int i = 0; i < numRepeatedFields; ++i) {
      info.repeatedInt[i] = r.nextInt();
      info.repeatedLong[i] = r.nextLong();
      info.repeatedFloat[i] = r.nextFloat();
      info.repeatedDouble[i] = r.nextDouble();
      info.repeatedBoolean[i] = r.nextBoolean();
      info.repeatedString[i] = Utils.randomString(r, stringLength);
      info.repeatedBytes[i] = Utils.randomString(r, stringLength).getBytes();
    }

    if (depth <= treeHeight) {
      info.children = new TestMessage[branchingFactor];
      for (int branch = 0; branch < branchingFactor; ++branch) {
        info.children[branch] = newRandomInstance(r, depth + 1, stringLength, numRepeatedFields,
                treeHeight, branchingFactor);
      }
    }
    return info;
  }
}
