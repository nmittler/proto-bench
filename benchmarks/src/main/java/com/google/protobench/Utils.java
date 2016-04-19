package com.google.protobench;

import com.google.protobuf.MessageLite;

import java.util.Random;

public final class Utils {
  private Utils() {
  }

  static String randomString(Random r, int size) {
    StringBuilder builder = new StringBuilder(size);
    for (int i = 0; i < size; ++i) {
      builder.append((char) (r.nextInt('z' - 'a') + 'a'));
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

  static int computeUInt32SizeNoTag(final int value) {
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

  static int computeUInt64SizeNoTag(long value) {
    // handle two popular special cases up front ...
    if ((value & (~0L << 7)) == 0L) {
      return 1;
    }
    if (value < 0L) {
      return 10;
    }
    // ... leaving us with 8 remaining, which we can divide and conquer
    int n = 2;
    if ((value & (~0L << 35)) != 0L) {
      n += 4;
      value >>>= 28;
    }
    if ((value & (~0L << 21)) != 0L) {
      n += 2;
      value >>>= 14;
    }
    if ((value & (~0L << 14)) != 0L) {
      n += 1;
    }
    return n;
  }
}
