package com.google.protobench;

import com.google.protobuf.CodedOutputStream;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import benchmark.protobuf.UnittestProto;
import benchmark.protostuff.NestedTestAllTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@State(Scope.Benchmark)
@Fork(1)
public class EncodingBenchmark {
  private static final int NUM_MESSAGES = 500;
  private static final int STRING_LENGTH = 50;
  private static final int NUM_REPEATED_FIELDS = 20;
  private static final int TREE_HEIGHT = 2;
  private static final int BRANCHING_FACTOR = 2;

  public enum Impl {
    PROTOBUF,
    PROTOSTUFF
  }

  @Param
  private Impl impl;

  private final Random r = new Random();

  private byte[] output;
  ProtoInfo[] messages;
  private int messageIndex;

  @Setup
  public void setUp() throws Exception {

    messages = new ProtoInfo[NUM_MESSAGES];
    for (int i = 0; i < NUM_MESSAGES; ++i) {
      messages[i] = ProtoInfo.newRandomInstance(r, 0);
    }
    messageIndex = 0;

    output = new byte[1024 * 1024];
  }

  @Benchmark
  public void encode() throws Exception {
    switch(impl) {
      case PROTOBUF:
        CodedOutputStream encoder = CodedOutputStream.newInstance(output);
        nextMessage().toProtobuf().writeTo(encoder);
        break;
      case PROTOSTUFF:
        NestedTestAllTypes proto = nextMessage().toProtostuff();
        LinkedBuffer buffer = LinkedBuffer.use(output);
        ProtobufIOUtil.writeTo(buffer, proto, proto.cachedSchema());
        break;
    }
  }

  private static String randomString(Random r, int size) {
    StringBuilder builder = new StringBuilder(size);
    for(int i = 0; i < size; ++i) {
      builder.append((char) (r.nextInt('z' - 'a') + 'a'));
    }
    return builder.toString();
  }

  private ProtoInfo nextMessage() {
    ProtoInfo info = messages[messageIndex];
    messageIndex = (messageIndex + 1) % NUM_MESSAGES;
    return info;
  }

  private static class ProtoInfo {
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

    ProtoInfo[] children;

    NestedTestAllTypes toProtostuff() {
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

      for(int i = 0; i<repeatedInt.length; ++i) {
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
        for(int i = 0; i < children.length; ++i) {
          nestedChildren.add(children[i].toProtostuff());
        }
        nested.setRepeatedChildList(nestedChildren);
      }
      return nested;
    }

    UnittestProto.NestedTestAllTypes toProtobuf() {
      benchmark.protobuf.UnittestProto.TestAllTypes.Builder proto = benchmark.protobuf.UnittestProto.TestAllTypes.newBuilder();
      proto.setOptionalUint32(optionalInt);
      proto.setOptionalUint64(optionalLong);
      proto.setOptionalFloat(optionalFloat);
      proto.setOptionalDouble(optionalDouble);
      proto.setOptionalString(optionalString);
      proto.setOptionalBytes(com.google.protobuf.ByteString.copyFrom(optionalBytes));

      for(int i = 0; i<repeatedInt.length; ++i) {
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
        for(int i = 0; i < children.length; ++i) {
          nested.addRepeatedChild(children[i].toProtobuf());
        }
      }
      return nested.build();
    }

    private static ProtoInfo newRandomInstance(Random r, int depth) {
      ProtoInfo info = new ProtoInfo();
      info.optionalInt = r.nextInt();
      info.optionalLong = r.nextLong();
      info.optionalFloat = r.nextFloat();
      info.optionalDouble = r.nextDouble();
      info.optionalBoolean = r.nextBoolean();
      info.optionalString = randomString(r, STRING_LENGTH);
      info.optionalBytes = randomString(r, STRING_LENGTH).getBytes();

      info.repeatedInt = new int[NUM_REPEATED_FIELDS];
      info.repeatedLong = new long[NUM_REPEATED_FIELDS];
      info.repeatedFloat = new float[NUM_REPEATED_FIELDS];
      info.repeatedDouble = new double[NUM_REPEATED_FIELDS];
      info.repeatedBoolean = new boolean[NUM_REPEATED_FIELDS];
      info.repeatedString = new String[NUM_REPEATED_FIELDS];
      info.repeatedBytes = new byte[NUM_REPEATED_FIELDS][];

      for(int i=0; i < NUM_REPEATED_FIELDS; ++i) {
        info.repeatedInt[i] = r.nextInt();
        info.repeatedLong[i] = r.nextLong();
        info.repeatedFloat[i] = r.nextFloat();
        info.repeatedDouble[i] = r.nextDouble();
        info.repeatedBoolean[i] = r.nextBoolean();
        info.repeatedString[i] = randomString(r, STRING_LENGTH);
        info.repeatedBytes[i] = randomString(r, STRING_LENGTH).getBytes();
      }

      if (depth <= TREE_HEIGHT) {
        info.children = new ProtoInfo[BRANCHING_FACTOR];
        for (int branch = 0; branch < BRANCHING_FACTOR; ++branch) {
          info.children[branch] = newRandomInstance(r, depth + 1);
        }
      }
      return info;
    }
  }

  /*public static void main(String[] args) throws Exception {
    EncodingBenchmark bm = new EncodingBenchmark();
    bm.impl = Impl.PROTOSTUFF;
    bm.setUp();
    //ProtoInfo info = bm.nextProtoInfo();

    while (true) {
      switch (bm.impl) {
        case PROTOBUF:
          CodedOutputStream encoder = CodedOutputStream.newInstance(bm.output);
          info.toProtobuf().writeTo(encoder);
          encoder.flush();
          break;
        case PROTOSTUFF:
          NestedTestAllTypes proto = info.toProtostuff();
          LinkedBuffer buffer = LinkedBuffer.use(bm.output);
          ProtobufIOUtil.writeTo(buffer, proto, proto.cachedSchema());
          break;
      }

    }

    /*byte[] protobufOutput = Arrays.copyOf(bm.output, encoder.getTotalBytesWritten());

    bm.impl = Impl.PROTOSTUFF;
    bm.setUp();
    NestedTestAllTypes proto = info.toProtostuff();
    LinkedBuffer buffer = LinkedBuffer.use(bm.output);
    ProtobufIOUtil.writeTo(buffer, proto, proto.cachedSchema());
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    LinkedBuffer.writeTo(os, buffer);
    byte[] protostuffOutput = os.toByteArray();

    //Assert.assertArrayEquals(protobufOutput, protostuffOutput);
    FileOutputStream f = new FileOutputStream("test_protostuff.bin");
    f.write(protostuffOutput);
    f.close();
  }*/
}
