package com.google.protobench;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;

@State(Scope.Benchmark)
@Fork(1)
public class VarintEncodingBenchmark {
  public enum Direction {
    FORWARD (new DirectionEncoder() {
      private final ForwardEncoder e = new ForwardEncoder(new byte[100], 0, 100);

      @Override
      void writeUInt32(int value) throws IOException{
        e.writeUInt32NoTag(value);
      }

      @Override
      void writeUInt64(long value) throws IOException {
        e.writeUInt64NoTag(value);
      }

      @Override
      void reset() {
        e.reset();
      }
    }),
    REVERSE_CALC(new DirectionEncoder() {
      private final ReverseEncoder e = new ReverseEncoder(new byte[100], 0, 100);
      @Override
      void writeUInt32(int value) throws IOException {
        e.writeUInt32NoTagCalc(value);
      }
      @Override
      void writeUInt64(long value) throws IOException {
        e.writeUInt64NoTagCalc(value);
      }
      @Override
      void reset() {
        e.reset();
      }
    }),
    REVERSE_CLZ(new DirectionEncoder() {
      private final ReverseEncoder e = new ReverseEncoder(new byte[100], 0, 100);
      @Override
      void writeUInt32(int value) throws IOException {
        e.writeUInt32NoTagClz(value);
      }
      @Override
      void writeUInt64(long value) throws IOException {
        e.writeUInt64NoTagClz(value);
      }
      @Override
      void reset() {
        e.reset();
      }
    });

    Direction(DirectionEncoder encoder) {
      this.encoder = encoder;
    }

    final DirectionEncoder encoder;

    static abstract class DirectionEncoder {
      final byte[] buffer = new byte[100];
      abstract void writeUInt32(int value) throws IOException;
      abstract void writeUInt64(long value) throws IOException;
      abstract void reset();
    }
  }

  public enum FieldWidth {
    FW_32,
    FW_64
  }

  public enum Operation {
    I32_1(new int[]{0, 100, 200, 255}),
    I32_2(new int[]{256, 10000, 30000, 32767}),
    I32_3(new int[]{32768, 50000, 1000000, 4194303}),
    I32_4(new int[]{4194304, 10000000, 200000000, 536870911}),
    I32_5(new int[]{0x20000000, 0x30000000, 0x7FFFFFFF, 0xFFFFFFFF}),
    I64_1(new long[]{0, 100, 200, 255}),
    I64_2(new long[]{256, 10000, 30000, 32767}),
    I64_3(new long[]{32768, 50000, 1000000, 4194303}),
    I64_4(new long[]{4194304, 10000000, 200000000, 536870911}),
    I64_5(new long[]{0x20000000L, 0x30000000L, 0x7FFFFFFFL, 68719476737L}),
    I64_6(new long[]{68719476736L, 2000000000000L, 6000000000000L, 8796093022207L}),
    I64_7(new long[]{8796093022208L, 200000000000000L, 1000000000000000L, 1125899906842623L}),
    I64_8(new long[]{0x4000000000000L, 0x5000000000000L, 0x6000000000000L, 0x1FFFFFFFFFFFFFEL}),
    I64_9(new long[]{0x1FFFFFFFFFFFFFFL, 0x3FFFFFFFFFFFFFFFL, 0x5FFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL}),
    I64_10(new long[]{0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL});

    Operation(int[] values) {
      fieldWidth = FieldWidth.FW_32;
      longValues = null;
      intValues = values;
    }

    Operation(long[] values) {
      fieldWidth = FieldWidth.FW_64;
      intValues = null;
      longValues = values;
    }

    int nextIntValue() {
      return intValues[(nextIndex = (nextIndex + 1) & 3)];
    }

    long nextLongValue() {
      return longValues[(nextIndex = (nextIndex + 1) & 3)];
    }

    private final FieldWidth fieldWidth;
    private final int[] intValues;
    private final long[] longValues;
    private int nextIndex;
  }

  @Param
  private Direction direction;

  @Param
  private Operation operation;

  @Benchmark
  public void encode() throws Exception {
    if (operation.fieldWidth == FieldWidth.FW_32) {
      direction.encoder.writeUInt32(operation.nextIntValue());
    } else {
      direction.encoder.writeUInt64(operation.nextLongValue());
    }
    direction.encoder.reset();
  }
}
