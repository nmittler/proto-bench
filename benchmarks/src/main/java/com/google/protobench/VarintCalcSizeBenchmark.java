package com.google.protobench;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
@Fork(1)
public class VarintCalcSizeBenchmark {

  @Param
  private VarintSizeCalculator calc;

  @Param
  private VarintInput input;

  @Benchmark
  public byte calcSize() {
    if (input.fieldWidth() == VarintInput.FieldWidth.FW_32) {
      return calc.calcUInt32Size(input.nextIntValue());
    } else {
      return calc.calcUInt64Size(input.nextLongValue());
    }
  }
}
