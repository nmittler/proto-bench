package com.google.protobench;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

public enum VarintInput {
  I32_1(new int[]{0, 50, 100, 127}),
  I32_2(new int[]{128, 500, 10000, 16383}),
  I32_3(new int[]{16384, 50000, 1000000, 2097151}),
  I32_4(new int[]{2097152, 10000000, 200000000, 268435455}),
  I32_5(new int[]{268435456, 0x30000000, 0x7FFFFFFF, 0xFFFFFFFF}),
  I64_1(new long[]{0, 50, 100, 127}),
  I64_2(new long[]{128, 500, 10000, 16383}),
  I64_3(new long[]{16384, 50000, 1000000, 2097151}),
  I64_4(new long[]{2097152, 10000000, 200000000, 268435455}),
  I64_5(new long[]{268435456, 0x30000000, 0x7FFFFFFF, 34359738367L}),
  I64_6(new long[]{34359738368L, 2000000000000L, 4000000000000L, 4398046511103L}),
  I64_7(new long[]{4398046511104L, 200000000000000L, 500000000000000L, 562949953421311L}),
  I64_8(new long[]{0x4000000000000L, 0x5000000000000L, 0x6000000000000L, 0x1FFFFFFFFFFFFFEL}),
  I64_9(new long[]{0x1FFFFFFFFFFFFFFL, 0x3FFFFFFFFFFFFFFFL, 0x5FFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL}),
  I64_10(new long[]{0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL});

  VarintInput(int[] values) {
    fieldWidth = FieldWidth.FW_32;
    longValues = null;
    intValues = values;
  }

  VarintInput(long[] values) {
    fieldWidth = FieldWidth.FW_64;
    intValues = null;
    longValues = values;
  }

  private final FieldWidth fieldWidth;
  private final int[] intValues;
  private final long[] longValues;
  private int nextIndex;

  FieldWidth fieldWidth() {
    return fieldWidth;
  }

  int nextIntValue() {
    return intValues[(nextIndex = (nextIndex + 1) & 3)];
  }

  long nextLongValue() {
    return longValues[(nextIndex = (nextIndex + 1) & 3)];
  }

  public enum FieldWidth {
    FW_32,
    FW_64
  }

  static int nextRandomIntValue() {
    switch(varint32Distribution.sample()) {
      case 1:
        return I32_1.nextIntValue();
      case 2:
        return I32_2.nextIntValue();
      case 3:
        return I32_3.nextIntValue();
      case 4:
        return I32_4.nextIntValue();
      default:
        return I32_5.nextIntValue();
    }
  }

  static long nextRandomLongValue() {
    switch(varint64Distribution.sample()) {
      case 1:
        return I64_1.nextLongValue();
      case 2:
        return I64_2.nextLongValue();
      case 3:
        return I64_3.nextLongValue();
      case 4:
        return I64_4.nextLongValue();
      case 5:
        return I64_5.nextLongValue();
      case 6:
        return I64_6.nextLongValue();
      case 7:
        return I64_7.nextLongValue();
      case 8:
        return I64_8.nextLongValue();
      case 9:
        return I64_9.nextLongValue();
      default:
        return I64_10.nextLongValue();
    }
  }

  // TODO(nmittler): Consider using more realistic distributions based on data analysis.
  private static final EnumeratedIntegerDistribution varint32Distribution = new EnumeratedIntegerDistribution(
          Utils.RANDOM,
          new int[]{1, 2, 3, 4, 5},
          new double[]{0.3, 0.2, 0.2, 0.1, 0.2});
  private static final EnumeratedIntegerDistribution varint64Distribution = new EnumeratedIntegerDistribution(
          Utils.RANDOM,
          new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
          new double[]{0.2, 0.1, 0.1, 0.5, 0.5, 0.5, 0.5, 0.1, 0.1, 0.2});
}
