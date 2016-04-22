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

import benchmark.protostuff.NestedTestAllTypes;

import java.util.Random;

@State(Scope.Benchmark)
@Fork(1)
public class ProtostuffEncodingBenchmark {
  private static final Random RANDOM = new Random();
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

  private Encoder encoder;
  private byte[] output;
  TestMessage[] messages;
  private int messageIndex;

  @Setup
  public void setUp() throws Exception {

    messages = new TestMessage[NUM_MESSAGES];
    for (int i = 0; i < NUM_MESSAGES; ++i) {
      messages[i] = TestMessage.newRandomInstance(0, STRING_LENGTH, NUM_REPEATED_FIELDS,
              TREE_HEIGHT, BRANCHING_FACTOR);
    }
    messageIndex = 0;

    output = new byte[1024 * 1024];
  }

  @Benchmark
  public void encode() throws Exception {
    encoder.encodeMessageNoTag(nextMessage());
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

  private TestMessage nextMessage() {
    TestMessage info = messages[messageIndex];
    messageIndex = (messageIndex + 1) % NUM_MESSAGES;
    return info;
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
