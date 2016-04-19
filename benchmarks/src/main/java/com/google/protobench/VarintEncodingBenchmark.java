package com.google.protobench;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.Random;

@State(Scope.Benchmark)
@Fork(1)
public class VarintEncodingBenchmark {
  private static final int NUM_VALUES = 1024 * 10;
  private static final Random RANDOM = new Random();

  public enum Direction {
    FORWARD,
    REVERSE
  }

  public enum FieldSize {
    I32_1(32, 1),
    I32_2(32, 2),
    I32_3(32, 3),
    I32_4(32, 4),
    I32_5(32, 5),
    I64_1(64, 1),
    I64_2(64, 2),
    I64_3(64, 3),
    I64_4(64, 4),
    I64_5(64, 5),
    I64_6(64, 6),
    I64_7(64, 7),
    I64_8(64, 8),
    I64_9(64, 9),
    I64_10(64, 10);

    FieldSize(int bits, int serializedBytes) {
      this.bits = bits;
      if ((bits == 64 && serializedBytes == 10) || (bits == 32 && serializedBytes == 5)) {
        // Handle negative values.
        fn = new NextValueFunction() {
          @Override
          public long nextValue() {
            return -RANDOM.nextInt(Integer.MAX_VALUE) - 1;
          }
        };
      } else if (bits == 32){
        // 32 bits
        int msb = 1 << ((serializedBytes * 7) - 1);
        int max = (msb - 1) | msb;
        final int min = msb >>> 6;
        final int range = max - min;
        fn = new NextValueFunction() {
          @Override
          public long nextValue() {
            int nextInt = RANDOM.nextInt(range);
            return nextInt + min;
          }
        };
      } else {
        // 64 bits.
        long msb = 1L << ((serializedBytes * 7) - 1);
        long max = (msb - 1) | msb;
        final long min = msb >>> 6;
        final long range = max - min;
        fn = new NextValueFunction() {
          @Override
          public long nextValue() {
            long nextLong = Math.abs(RANDOM.nextLong());
            return (nextLong % range) + min;
          }
        };
      }
    }

    long nextValue() {
      return fn.nextValue();
    }

    private final int bits;
    private final NextValueFunction fn;
    private interface NextValueFunction {
      long nextValue();
    }
  }

  @Param
  private Direction direction;

  @Param
  private FieldSize fieldSize;

  private Encoder encoder;
  private byte[] buffer;
  private int[] ints;
  private long[] longs;
  private int valueIndex;

  @Setup
  public void setUp() throws Exception {
    buffer = new byte[100];
    switch (direction) {
      case FORWARD:
        encoder = new ForwardEncoder(buffer, 0, buffer.length);
        break;
      case REVERSE:
        encoder = new ReverseEncoder(buffer, 0, buffer.length);
        break;
    }

    ints = null;
    longs = null;
    if (fieldSize.bits == 32) {
      ints = new int[NUM_VALUES];
      for (int i = 0; i < NUM_VALUES; ++i) {
        ints[i] = (int) fieldSize.nextValue();
      }
    } else {
      longs = new long[NUM_VALUES];
      for (int i = 0; i < NUM_VALUES; ++i) {
        longs[i] = fieldSize.nextValue();
      }
    }

    valueIndex = 0;
  }

  @Benchmark
  public void encode() throws Exception {
    if (ints != null) {
      encoder.writeUInt32NoTag(ints[valueIndex++]);
    } else {
      encoder.writeUInt64NoTag(longs[valueIndex++]);
    }
    if (valueIndex >= NUM_VALUES) {
      valueIndex = 0;
    }
    encoder.reset();
  }

  public static void main(String[] args) throws Exception {
    VarintEncodingBenchmark bm = new VarintEncodingBenchmark();
    bm.direction = Direction.REVERSE;
    bm.fieldSize = FieldSize.I32_1;
    bm.setUp();
    for (int i=0; i<1000000; ++i) {
      bm.encode();
    }
  }
}
