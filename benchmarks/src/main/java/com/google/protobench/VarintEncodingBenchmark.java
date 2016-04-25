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
  public enum Algorithm {
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
    REVERSE(new DirectionEncoder() {
      private final ReverseEncoder e = new ReverseEncoder(new byte[100], 0, 100);
      @Override
      void writeUInt32(int value) throws IOException {
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
    });

    Algorithm(DirectionEncoder encoder) {
      this.encoder = encoder;
    }

    final DirectionEncoder encoder;

    static abstract class DirectionEncoder {
      abstract void writeUInt32(int value) throws IOException;
      abstract void writeUInt64(long value) throws IOException;
      abstract void reset();
    }
  }

  @Param
  private Algorithm algorithm;

  @Param
  private VarintInput input;

  @Benchmark
  public void encode() throws Exception {
    if (input.fieldWidth() == VarintInput.FieldWidth.FW_32) {
      algorithm.encoder.writeUInt32(input.nextIntValue());
    } else {
      algorithm.encoder.writeUInt64(input.nextLongValue());
    }
    algorithm.encoder.reset();
  }
}
