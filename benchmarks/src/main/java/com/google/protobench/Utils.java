package com.google.protobench;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

public final class Utils {
  static final RandomGenerator RANDOM = new Well19937c(100);

  private Utils() {
  }

  static int calcNodesInTree(int branchingFactor, int treeHight) {
    return (int) ((Math.pow(branchingFactor, treeHight + 1) - 1) / (branchingFactor - 1));
  }

  static String randomString(int size) {
    StringBuilder builder = new StringBuilder(size);
    for (int i = 0; i < size; ++i) {
      builder.append((char) (RANDOM.nextInt('z' - 'a') + 'a'));
    }
    return builder.toString();
  }

  static int computeMessageSize(final int fieldNumber, final TestMessage value) {
    return computeTagSize(fieldNumber) + computeMessageSizeNoTag(value);
  }

  static int computeUInt32Size(final int fieldNumber, final int value) {
    return computeTagSize(fieldNumber) + computeUInt32SizeNoTag(value);
  }

  static int computeUInt64Size(final int fieldNumber, final long value) {
    return computeTagSize(fieldNumber) + computeUInt64SizeNoTag(value);
  }

  static int computeFloatSize(final int fieldNumber, final float value) {
    return computeTagSize(fieldNumber) + computeFloatSizeNoTag(value);
  }

  static int computeFloatSizeNoTag(final float value) {
    return WireFormat.FIXED_32_SIZE;
  }

  static int computeDoubleSize(final int fieldNumber, final double value) {
    return computeTagSize(fieldNumber) + computeDoubleSizeNoTag(value);
  }

  static int computeDoubleSizeNoTag(final double value) {
    return WireFormat.FIXED_64_SIZE;
  }

  static int computeTagSize(final int fieldNumber) {
    return computeUInt32SizeNoTag(WireFormat.makeTag(fieldNumber, 0));
  }

  static int computeMessageSizeNoTag(final TestMessage value) {
    return computeLengthDelimitedFieldSize(value.getSerializedSize());
  }

  static int computeLengthDelimitedFieldSize(int fieldLength) {
    return computeUInt32SizeNoTag(fieldLength) + fieldLength;
  }

  static int computeStringSize(final int fieldNumber, final String value) {
    return computeTagSize(fieldNumber) + computeStringSizeNoTag(value);
  }

  static int computeByteArraySize(final int fieldNumber, final byte[] value) {
    return computeTagSize(fieldNumber) + computeByteArraySizeNoTag(value);
  }

  static int computeByteArraySizeNoTag(final byte[] value) {
    return computeLengthDelimitedFieldSize(value.length);
  }

  static int computeBoolSize(final int fieldNumber, final boolean value) {
    return computeTagSize(fieldNumber) + 1;
  }

  static int computeStringSizeNoTag(final String value) {
    int length;
    try {
      length = Utf8.encodedLength(value);
    } catch (Utf8.UnpairedSurrogateException e) {
      // TODO(dweis): Consider using nio Charset methods instead.
      final byte[] bytes = value.getBytes(Utf8.UTF_8);
      length = bytes.length;
    }

    return computeLengthDelimitedFieldSize(length);
  }

  static byte computeUInt32SizeNoTag(final int value) {
    if ((value & (~0 << 7)) == 0) {
      return 1;
    }
    if ((value & (~0 << 14)) == 0) {
      return 2;
    }
    if ((value & (~0 << 21)) == 0) {
      return 3;
    }
    if ((value & (~0 << 28)) == 0) {
      return 4;
    }
    return 5;
  }

  static byte computeUInt64SizeNoTag(long value) {
    // handle two popular special cases up front ...
    if ((value & (~0L << 7)) == 0L) {
      // Byte 1
      return 1;
    }
    if (value < 0L) {
      // Byte 10
      return 10;
    }
    // ... leaving us with 8 remaining, which we can divide and conquer
    byte n = 2;
    if ((value & (~0L << 35)) != 0L) {
      // Byte 6-9
      n += 4;// + (value >>> 63);
      value >>>= 28;
    }
    if ((value & (~0L << 21)) != 0L) {
      // Byte 4-5 or 8-9
      n += 2;
      value >>>= 14;
    }
    if ((value & (~0L << 14)) != 0L) {
      // Byte 3 or 7
      n += 1;
    }
    return n;

    // handle two popular special cases up front ...
    /*if ((value & (~0L << 7)) == 0L) {
      return 1;
    }
    // ... leaving us with 8 remaining, which we can divide and conquer
    int n = 2;
    // Assume byte 3-5
    int intValue = ((int) (value >>> 14)) & 0x1FFFFF;
    if ((value & (~0L << 35)) != 0L) {
      // Byte 6-10
      // Adding an extra byte if the sign bit is set.
      n += 4 + (value >>> 63);
      intValue = ((int)(value >>> 42)) & 0x1FFFFF;
    }
    if ((intValue & ~0x7F) != 0L) {
      // Bytes 4-5 or 8-9
      n += 2;
      intValue >>>= 14;
    }
    if ((intValue & 0x7F) != 0L) {
      n += 1;
    }
    return n;*/
  }
}
