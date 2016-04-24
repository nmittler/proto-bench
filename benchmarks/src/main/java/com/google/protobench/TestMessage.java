package com.google.protobench;

import static com.google.protobench.Utils.RANDOM;

import benchmark.protobuf.UnittestProto;
import benchmark.protostuff.NestedTestAllTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class TestMessage {
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

  private final SerializedSizeManager sizeManager;
  private final int sizeIndex;

  private TestMessage(SerializedSizeManager sizeManager) {
    this.sizeManager = sizeManager;
    this.sizeIndex = sizeManager.nextIndex();
  }

  TestMessage shallowCopy() {
    TestMessage copy = new TestMessage(sizeManager);
    copy.optionalInt = optionalInt;
    copy.optionalLong = optionalLong;
    copy.optionalFloat = optionalFloat;
    copy.optionalDouble = optionalDouble;
    copy.optionalBoolean = optionalBoolean;
    copy.optionalString = optionalString;
    copy.optionalBytes = optionalBytes;

    copy.repeatedInt = repeatedInt;
    copy.repeatedLong = repeatedLong;
    copy.repeatedFloat = repeatedFloat;
    copy.repeatedDouble = repeatedDouble;
    copy.repeatedBoolean = repeatedBoolean;
    copy.repeatedString = repeatedString;
    copy.repeatedBytes = repeatedBytes;

    if (children != null) {
      copy.children = new TestMessage[children.length];
      for (int i = 0; i < children.length; ++i) {
        copy.children[i] = children[i].shallowCopy();
      }
    }
    return copy;
  }

  public int getSerializedSize() {
    int serializedSize = sizeManager.getSerializedSize(sizeIndex);
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
      serializedSize = size;
      sizeManager.setSerializedSize(sizeIndex, serializedSize);
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

  public static TestMessage newRandomInstance(int depth,
                                              int stringLength,
                                              int numRepeatedFields,
                                              int treeHeight,
                                              int branchingFactor,
                                              SerializedSizeManager sizeManager) {
    TestMessage info = new TestMessage(sizeManager);
    info.optionalInt = VarintInput.nextRandomIntValue();
    info.optionalLong = VarintInput.nextRandomLongValue();
    info.optionalFloat = RANDOM.nextFloat();
    info.optionalDouble = RANDOM.nextDouble();
    info.optionalBoolean = RANDOM.nextBoolean();
    info.optionalString = Utils.randomString(stringLength);
    info.optionalBytes = Utils.randomString(stringLength).getBytes();

    info.repeatedInt = new int[numRepeatedFields];
    info.repeatedLong = new long[numRepeatedFields];
    info.repeatedFloat = new float[numRepeatedFields];
    info.repeatedDouble = new double[numRepeatedFields];
    info.repeatedBoolean = new boolean[numRepeatedFields];
    info.repeatedString = new String[numRepeatedFields];
    info.repeatedBytes = new byte[numRepeatedFields][];

    for (int i = 0; i < numRepeatedFields; ++i) {
      info.repeatedInt[i] = VarintInput.nextRandomIntValue();
      info.repeatedLong[i] = VarintInput.nextRandomLongValue();
      info.repeatedFloat[i] = RANDOM.nextFloat();
      info.repeatedDouble[i] = RANDOM.nextDouble();
      info.repeatedBoolean[i] = RANDOM.nextBoolean();
      info.repeatedString[i] = Utils.randomString(stringLength);
      info.repeatedBytes[i] = Utils.randomString(stringLength).getBytes();
    }

    if (depth < treeHeight) {
      info.children = new TestMessage[branchingFactor];
      for (int branch = 0; branch < branchingFactor; ++branch) {
        info.children[branch] = newRandomInstance(depth + 1, stringLength, numRepeatedFields,
                treeHeight, branchingFactor, sizeManager);
      }
    }
    return info;
  }

  final static class SerializedSizeManager {
    private final int[] sizes;
    private int nextIndex;

    SerializedSizeManager(int size) {
      sizes = new int[size];
    }

    int getSerializedSize(int index) {
      return sizes[index];
    }

    void setSerializedSize(int index, int value) {
      sizes[index] = value;
    }

    int nextIndex() {
      if (nextIndex >= sizes.length) {
        throw new IndexOutOfBoundsException("sizes.length=" + sizes.length + ", nextIndex=" + nextIndex);
      }
      return nextIndex++;
    }

    void clearAll() {
      Arrays.fill(sizes, -1);
    }
  }
}
