package com.google.protobench;

import com.google.protobench.TestMessage.SerializedSizeManager;
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

@State(Scope.Benchmark)
@Fork(1)
public class ProtostuffEncodingBenchmark {
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

  private byte[] output;
  TestMessage message;

  @Setup
  public void setUp() throws Exception {
    SerializedSizeManager sizeManager = new SerializedSizeManager(
                    Utils.calcNodesInTree(BRANCHING_FACTOR, TREE_HEIGHT));
    message = TestMessage.newRandomInstance(0, STRING_LENGTH, NUM_REPEATED_FIELDS,
              TREE_HEIGHT, BRANCHING_FACTOR, sizeManager);

    output = new byte[1024 * 1024];
  }

  @Benchmark
  public void encode() throws Exception {
    switch(impl) {
      case PROTOBUF:
        CodedOutputStream encoder = CodedOutputStream.newInstance(output);
        message.toProtobuf().writeTo(encoder);
        break;
      case PROTOSTUFF:
        NestedTestAllTypes proto = message.toProtostuff();
        LinkedBuffer buffer = LinkedBuffer.use(output);
        ProtobufIOUtil.writeTo(buffer, proto, proto.cachedSchema());
        break;
    }
  }
}
