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
public class EncodingAlgorithmBenchmark {
  private static final Random RANDOM = new Random();
  private static final int NUM_MESSAGES = 500;
  private static final int STRING_LENGTH = 50;
  private static final int NUM_REPEATED_FIELDS = 20;
  private static final int TREE_HEIGHT = 2;
  private static final int BRANCHING_FACTOR = 2;

  public enum Direction {
    FORWARD,
    REVERSE
  }

  @Param
  private Direction direction;

  private Encoder encoder;
  private byte[] output;
  TestMessage[] messages;
  private int messageIndex;

  @Setup
  public void setUp() throws Exception {

    messages = new TestMessage[NUM_MESSAGES];
    for (int i = 0; i < NUM_MESSAGES; ++i) {
      messages[i] = TestMessage.newRandomInstance(RANDOM, 0, STRING_LENGTH, NUM_REPEATED_FIELDS,
              TREE_HEIGHT, BRANCHING_FACTOR);
    }
    messageIndex = 0;

    output = new byte[1024 * 1024];

    switch (direction) {
      case FORWARD:
        encoder = new ForwardEncoder(output, 0, output.length);
        break;
      case REVERSE:
        encoder = new ReverseEncoder(output, 0, output.length);
        break;
    }
  }

  @Benchmark
  public void encode() throws Exception {
    encoder.encodeMessageNoTag(nextMessage());
    encoder.reset();
  }

  private TestMessage nextMessage() {
    TestMessage info = messages[messageIndex];
    messageIndex = (messageIndex + 1) % NUM_MESSAGES;
    return info;
  }

  public static void main(String[] args) throws Exception {
    EncodingAlgorithmBenchmark bm = new EncodingAlgorithmBenchmark();
    bm.direction = Direction.REVERSE;
    bm.setUp();
    bm.encode();
  }
}
