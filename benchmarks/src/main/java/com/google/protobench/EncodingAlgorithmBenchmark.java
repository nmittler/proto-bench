package com.google.protobench;

import com.google.protobench.TestMessage.SerializedSizeManager;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
@Fork(1)
public class EncodingAlgorithmBenchmark {
  public enum Direction {
    FORWARD,
    REVERSE
  }

  @Param
  public Direction direction;

  @Param({"50"})
  public int stringLength;

  @Param({"20"})
  public int numRepeatedFields;

  @Param({"2"})
  public int treeHeight;

  @Param({"4"})
  public int branchingFactor;

  private Encoder encoder;
  private byte[] output = new byte[1024 * 1024];
  private TestMessage message;
  private SerializedSizeManager sizeManager;


  @Setup
  public void setUp() throws Exception {
    // Create the message.
    int numMessages = Utils.calcNodesInTree(branchingFactor, treeHeight);
    sizeManager = new SerializedSizeManager(numMessages);
    message = TestMessage.newRandomInstance(0, stringLength, numRepeatedFields,
        treeHeight, branchingFactor, sizeManager);

    switch (direction) {
      case FORWARD:
        encoder = new ForwardEncoder(output, 0, output.length);
        break;
      case REVERSE:
        encoder = new ReverseEncoder(output, 0, output.length);
        break;
    }
  }

  @TearDown(Level.Invocation)
  public void tearDown() {
    encoder.reset();
    sizeManager.clearAll();
  }

  @Benchmark
  public void encode() throws Exception {
    encoder.encodeMessageNoTag(message);
  }
}
