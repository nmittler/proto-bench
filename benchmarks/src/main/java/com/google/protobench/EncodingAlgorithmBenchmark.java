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
  private byte[] output = new byte[1024 * 1024];
  private TestMessage message = TestMessage.newRandomInstance(0, STRING_LENGTH, NUM_REPEATED_FIELDS,
          TREE_HEIGHT, BRANCHING_FACTOR);

  @Setup
  public void setUp() throws Exception {
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
    encoder.encodeMessageNoTag(message);
    encoder.reset();
  }
}
