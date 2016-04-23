package com.google.protobench;

import static com.google.protobench.UnsafeUtil.ARRAY_BASE_OFFSET;
import static com.google.protobench.UnsafeUtil.HAS_UNSAFE_ARRAY_OPERATIONS;
import static com.google.protobench.UnsafeUtil.UNSAFE;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

final class ReverseEncoder implements Encoder {
  private final byte[] buffer;
  private final int offset;
  private final int limit;
  private final int offsetMinusOne;
  private int position;

  ReverseEncoder(byte[] buffer, int offset, int length) {
    if (buffer == null) {
      throw new NullPointerException("buffer");
    }
    if ((offset | length | (buffer.length - (offset + length))) < 0) {
      throw new IllegalArgumentException(String.format(
              "Array range is invalid. Buffer.length=%d, offset=%d, length=%d",
              buffer.length, offset, length));
    }
    this.buffer = buffer;
    this.offset = offset;
    limit = offset + length;
    offsetMinusOne = offset - 1;
    reset();
  }

  @Override
  public void encodeMessage(int fieldNumber, TestMessage message) throws IOException {
    encodeMessageNoTag(message);
    writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
  }

  @Override
  public void encodeMessageNoTag(TestMessage message) throws IOException {
    final int prevPos = position;

    if (message.repeatedBytes != null) {
      for (int ix = message.repeatedBytes.length - 1; ix >= 0; --ix) {
        writeByteArray(45, message.repeatedBytes[ix]);
      }
    }
    if (message.repeatedString != null) {
      for (int ix = message.repeatedString.length - 1; ix >= 0; --ix) {
        writeString(44, message.repeatedString[ix]);
      }
    }
    if (message.repeatedBoolean != null) {
      for (int ix = message.repeatedBoolean.length - 1; ix >= 0; --ix) {
        writeBool(43, message.repeatedBoolean[ix]);
      }
    }
    if (message.repeatedDouble != null) {
      for (int ix = message.repeatedDouble.length - 1; ix >= 0; --ix) {
        writeDouble(42, message.repeatedDouble[ix]);
      }
    }
    if (message.repeatedFloat != null) {
      for (int ix = message.repeatedFloat.length - 1; ix >= 0; --ix) {
        writeFloat(41, message.repeatedFloat[ix]);
      }
    }
    if (message.repeatedLong != null) {
      for (int ix = message.repeatedLong.length - 1; ix >= 0; --ix) {
        writeUInt64(34, message.repeatedLong[ix]);
      }
    }
    if (message.repeatedInt != null) {
      for (int ix = message.repeatedInt.length - 1; ix >= 0; --ix) {
        writeUInt32(33, message.repeatedInt[ix]);
      }
    }

    if (message.children != null) {
      for (int ix = message.children.length - 1; ix >= 0; --ix) {
        encodeMessage(18, message.children[ix]);
      }
    }

    writeByteArray(15, message.optionalBytes);
    writeString(14, message.optionalString);
    writeBool(13, message.optionalBoolean);
    writeDouble(12, message.optionalDouble);
    writeFloat(11, message.optionalFloat);
    writeUInt64(4, message.optionalLong);
    writeUInt32(3, message.optionalInt);

    // Now write out the serialized size for this message.
    int serializedSize = position - prevPos;
    writeUInt32NoTag(serializedSize);
  }

  @Override
  public void reset() {
    position = limit - 1;
  }

  public final void writeTag(final int fieldNumber, final int wireType) throws IOException {
    writeUInt32NoTag(WireFormat.makeTag(fieldNumber, wireType));
  }

  public final void writeInt32(final int fieldNumber, final int value) throws IOException {
    writeInt32NoTag(value);
    writeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
  }

  public final void writeUInt32(final int fieldNumber, final int value) throws IOException {
    writeUInt32NoTag(value);
    writeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
  }

  public final void writeFixed32(final int fieldNumber, final int value) throws IOException {
    writeFixed32NoTag(value);
    writeTag(fieldNumber, WireFormat.WIRETYPE_FIXED32);
  }

  public final void writeUInt64(final int fieldNumber, final long value) throws IOException {
    writeUInt64NoTag(value);
    writeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
  }

  public final void writeFixed64(final int fieldNumber, final long value) throws IOException {
    writeFixed64NoTag(value);
    writeTag(fieldNumber, WireFormat.WIRETYPE_FIXED64);
  }

  public final void writeBool(final int fieldNumber, final boolean value) throws IOException {
    write((byte) (value ? 1 : 0));
    writeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
  }

  public void writeFloat(int fieldNumber, float value) throws IOException {
    writeFixed32(fieldNumber, Float.floatToRawIntBits(value));
  }

  public void writeDouble(int fieldNumber, double value) throws IOException {
    writeFixed64(fieldNumber, Double.doubleToRawLongBits(value));
  }

  public final void writeString(final int fieldNumber, final String value) throws IOException {
    writeStringNoTag(value);
    writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
  }

  public final void writeByteArray(final int fieldNumber, final byte[] value) throws IOException {
    writeByteArray(fieldNumber, value, 0, value.length);
  }

  public final void writeByteArray(
          final int fieldNumber, final byte[] value, final int offset, final int length)
          throws IOException {
    writeByteArrayNoTag(value, offset, length);
    writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
  }

  public final void writeByteBuffer(final int fieldNumber, final ByteBuffer value)
          throws IOException {
    writeRawBytes(value);
    writeUInt32NoTag(value.capacity());
    writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
  }

  public final void writeByteArrayNoTag(final byte[] value, int offset, int length)
          throws IOException {
    write(value, offset, length);
    writeUInt32NoTag(length);
  }

  public final void writeRawBytes(final ByteBuffer value) throws IOException {
    if (value.hasArray()) {
      write(value.array(), value.arrayOffset(), value.capacity());
    } else {
      ByteBuffer duplicated = value.duplicate();
      duplicated.clear();
      write(duplicated);
    }
  }

  public final void write(byte value) throws IOException {
    if (position < offset) {
      throw new OutOfSpaceException(new IndexOutOfBoundsException(
              String.format("Pos: %d, offset: %d, len: %d", position, offset, 1)));
    }
    buffer[position--] = value;
  }

  public final void writeInt32NoTag(int value) throws IOException {
    if (value >= 0) {
      writeUInt32NoTag(value);
    } else {
      // Must sign-extend.
      writeUInt64NoTag(value);
    }
  }

  private static final byte CLZ_LOOKUP[] = {
          32, 31, 30, 30, 29, 29, 29, 29,
          28, 28, 28, 28, 28, 28, 28, 28,
          27, 27, 27, 27, 27, 27, 27, 27,
          27, 27, 27, 27, 27, 27, 27, 27,
          26, 26, 26, 26, 26, 26, 26, 26,
          26, 26, 26, 26, 26, 26, 26, 26,
          26, 26, 26, 26, 26, 26, 26, 26,
          26, 26, 26, 26, 26, 26, 26, 26,
          25, 25, 25, 25, 25, 25, 25, 25,
          25, 25, 25, 25, 25, 25, 25, 25,
          25, 25, 25, 25, 25, 25, 25, 25,
          25, 25, 25, 25, 25, 25, 25, 25,
          25, 25, 25, 25, 25, 25, 25, 25,
          25, 25, 25, 25, 25, 25, 25, 25,
          25, 25, 25, 25, 25, 25, 25, 25,
          25, 25, 25, 25, 25, 25, 25, 25,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24,
          24, 24, 24, 24, 24, 24, 24, 24
  };
  private static int countLeadingZeros(int x) {
    byte n;
    if (x < 0 || x >= (1L << 16)) {
      if (x < 0 || x >= (1L << 24)) {
        n = 24;
      }
      else {
        n = 16;
      }
    }
    else {
      if (x >= (1L << 8)) {
        n = 8;
      }
      else {
        n = 0;
      }
    }
    return CLZ_LOOKUP[x >> n] - n;
  }
  
  private static final int[] SHIFT = new int[10];
  private static final int[] ABOVE_MASKS_32 = new int[32];
  private static final int[] BELOW_MASKS_32 = new int[32];
  private static final long[] ABOVE_MASKS_64 = new long[64];
  private static final long[] BELOW_MASKS_64 = new long[64];
  private static final byte[] LEADING_ZEROS_TO_BYTES_32 = new byte[33];
  private static final byte[] LEADING_ZEROS_TO_BYTES_64 = new byte[65];
  static {
    for (int i=0; i<10; ++i) {
      int shift = i * 7;
      SHIFT[i] = shift;
      BigInteger aboveMaskBig = BigInteger.valueOf(~0x7FL).shiftLeft(shift);
      long aboveMask = aboveMaskBig.and(BigInteger.valueOf(0xFFFFFFFFFFFFFFFFL)).longValue();
      long belowMask = ~aboveMaskBig.shiftRight(7).and(BigInteger.valueOf(0xFFFFFFFFFFFFFFFFL)).longValue();
      ABOVE_MASKS_64[shift] = aboveMask;
      BELOW_MASKS_64[shift] = belowMask;
    }
    for(int i=0; i<5; ++i) {
      int shift = i * 7;
      long aboveMaskLong = ((long) (~0x7F)) << shift;
      int aboveMask = (int) aboveMaskLong;
      int belowMask = ~(int) ((aboveMaskLong >>> 7) & 0xFFFFFFFFL);
      ABOVE_MASKS_32[shift] = aboveMask;
      BELOW_MASKS_32[shift] = belowMask;
    }
    for(int i = 0, maxLeadingZeros = 3, numBytes = 5; numBytes > 0 && maxLeadingZeros < 32; --numBytes, maxLeadingZeros += 7) {
      for (; i <= maxLeadingZeros; ++i) {
        LEADING_ZEROS_TO_BYTES_32[i] = (byte) numBytes;
      }
    }
    LEADING_ZEROS_TO_BYTES_32[32] = 1;
    for(int i = 0, maxLeadingZeros = 0, numBytes = 10; numBytes > 0 && maxLeadingZeros < 64; --numBytes, maxLeadingZeros += 7) {
      for (; i <= maxLeadingZeros; ++i) {
        LEADING_ZEROS_TO_BYTES_64[i] = (byte) numBytes;
      }
    }
    LEADING_ZEROS_TO_BYTES_64[64] = 1;
    System.out.println("done.");
  }

  private void writeUInt32NoTagCalcUnsafe(int value) throws IOException {
    final byte size = Utils.computeUInt32SizeNoTag(value);
    if (position - size < offsetMinusOne) {
      throw new OutOfSpaceException();
    }

    long pos = ARRAY_BASE_OFFSET + position;
    position -= size;
    int sign = 0;
    switch (size) {
      case 5:
        UNSAFE.putByte(buffer, pos--, (byte) (value >>> 28));
        sign = 0x80;
      case 4:
        UNSAFE.putByte(buffer, pos--, (byte) (((value >>> 21) & 0x7F) | sign));
        sign = 0x80;
      case 3:
        UNSAFE.putByte(buffer, pos--, (byte) (((value >>> 14) & 0x7F) | sign));
        sign = 0x80;
      case 2:
        UNSAFE.putByte(buffer, pos--, (byte) (((value >>> 7) & 0x7F) | sign));
        sign = 0x80;
      case 1:
        UNSAFE.putByte(buffer, pos, (byte) ((value & 0x7F) | sign));
    }
  }

  private void writeUInt32NoTagCalcSafe(int value) throws IOException {
    final byte size = Utils.computeUInt32SizeNoTag(value);
    if (position - size < offsetMinusOne) {
      throw new OutOfSpaceException();
    }
    int sign = 0;
    switch (size) {
      case 5:
        buffer[position--] = (byte) (value >>> 28);
        sign = 0x80;
      case 4:
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | sign);
        sign = 0x80;
      case 3:
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | sign);
        sign = 0x80;
      case 2:
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | sign);
        sign = 0x80;
      case 1:
        buffer[position--] = (byte) ((value & 0x7F) | sign);
    }
  }

  final void writeUInt32NoTagCalc(int value) throws IOException {
    if(HAS_UNSAFE_ARRAY_OPERATIONS) {
      writeUInt32NoTagCalcUnsafe(value);
    } else {
      writeUInt32NoTagCalcSafe(value);
    }
  }

  final void writeUInt32NoTagClzIndex(int value) throws IOException {
    int sign = 0;
    switch(LEADING_ZEROS_TO_BYTES_32[Integer.numberOfLeadingZeros(value)]) {
      case 5:
        buffer[position--] = (byte) (value >>> 28);
        sign = 0x80;
      case 4:
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | sign);
        sign = 0x80;
      case 3:
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | sign);
        sign = 0x80;
      case 2:
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | sign);
        sign = 0x80;
      case 1:
        buffer[position--] = (byte) ((value & 0x7F) | sign);
    }
  }
  final void writeUInt32NoTagClzDiv(int value) throws IOException {
    int sign = 0;
    switch((((32 - Integer.numberOfLeadingZeros(value)) - 1) / 7) + 1) {
      case 5:
        buffer[position--] = (byte) (value >>> 28);
        sign = 0x80;
      case 4:
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | sign);
        sign = 0x80;
      case 3:
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | sign);
        sign = 0x80;
      case 2:
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | sign);
        sign = 0x80;
      case 1:
        buffer[position--] = (byte) ((value & 0x7F) | sign);
    }
  }
  @Override
  public final void writeUInt32NoTag(int value) throws IOException {
    writeUInt32NoTagCalc(value);
  }

  public final void writeFixed32NoTag(int value) throws IOException {
    if (position - 3 < offset) {
      throw new OutOfSpaceException(
              new IndexOutOfBoundsException(
                      String.format("Pos: %d, offset: %d, len: %d", position, offset, 4)));
    }

    buffer[position--] = (byte) ((value >> 24) & 0xFF);
    buffer[position--] = (byte) ((value >> 16) & 0xFF);
    buffer[position--] = (byte) ((value >> 8) & 0xFF);
    buffer[position--] = (byte) (value & 0xFF);
  }

  final void writeUInt64NoTagCalc(long value) throws IOException {
    //int sign = 0;
    //position -= Utils.computeUInt64SizeNoTag(value);
    int size = Utils.computeUInt64SizeNoTag(value);
    if (HAS_UNSAFE_ARRAY_OPERATIONS) {
      position -= size;
      if (position < offsetMinusOne) {
        throw new OutOfSpaceException();
      }
      long pos = ARRAY_BASE_OFFSET + position + 1;
      while (true) {
        if (size-- == 1) {
          UNSAFE.putByte(buffer, pos, (byte) value);
          return;
        } else {
          UNSAFE.putByte(buffer, pos++, (byte) (((int) value & 0x7F) | 0x80));
          value >>>= 7;
        }
      }
    } else {
      position -= size;
      if (position < offsetMinusOne) {
        throw new OutOfSpaceException();
      }
      int pos = position + 1;
      while (true) {
        if (size-- == 1) {
          buffer[pos] = (byte) value;
          return;
        } else {
          buffer[pos++] = (byte) (((int) value & 0x7F) | 0x80);
          value >>>= 7;
        }
      }
    }
    /*int pos = position + 1;
    while (true) {
      if ((value & ~0x7FL) == 0) {
        buffer[pos] = (byte) value;
        return;
      } else {
        buffer[pos++] = (byte) (((int) value & 0x7F) | 0x80);
        value >>>= 7;
      }
    }*/
    /*switch (Utils.computeUInt64SizeNoTag(value)) {
      case 1:
        buffer[position--] = (byte) ((value & 0x7F));
        break;
      case 2:
        buffer[position--] = (byte) (((value >>> 7) & 0x7F));
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 3:
        buffer[position--] = (byte) (((value >>> 14) & 0x7F));
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 4:
        buffer[position--] = (byte) (((value >>> 21) & 0x7F));
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 5:
        buffer[position--] = (byte) (((value >>> 28) & 0x7F));
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 6:
        buffer[position--] = (byte) (((value >>> 35) & 0x7F));
        buffer[position--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 7:
        buffer[position--] = (byte) (((value >>> 42) & 0x7F));
        buffer[position--] = (byte) (((value >>> 35) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 8:
        buffer[position--] = (byte) (((value >>> 49) & 0x7F));
        buffer[position--] = (byte) (((value >>> 42) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 35) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 9:
        buffer[position--] = (byte) (((value >>> 56) & 0x7F));
        buffer[position--] = (byte) (((value >>> 49) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 42) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 35) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 10:
        buffer[position--] = (byte) ((value >>> 63));
        buffer[position--] = (byte) (((value >>> 56) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 49) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 42) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 35) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
    }*/
    /*switch (Utils.computeUInt64SizeNoTag(value)) {
      case 10:
        buffer[position--] = (byte) ((value >>> 63));
        buffer[position--] = (byte) (((value >>> 56) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 49) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 42) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 35) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 9:
        buffer[position--] = (byte) (((value >>> 56) & 0x7F));
        buffer[position--] = (byte) (((value >>> 49) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 42) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 35) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 8:
        buffer[position--] = (byte) (((value >>> 49) & 0x7F));
        buffer[position--] = (byte) (((value >>> 42) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 35) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 7:
        buffer[position--] = (byte) (((value >>> 42) & 0x7F));
        buffer[position--] = (byte) (((value >>> 35) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 6:
        buffer[position--] = (byte) (((value >>> 35) & 0x7F));
        buffer[position--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 5:
        buffer[position--] = (byte) (((value >>> 28) & 0x7F));
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 4:
        buffer[position--] = (byte) (((value >>> 21) & 0x7F));
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 3:
        buffer[position--] = (byte) (((value >>> 14) & 0x7F));
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 2:
        buffer[position--] = (byte) (((value >>> 7) & 0x7F));
        buffer[position--] = (byte) ((value & 0x7F) | 0x80);
        break;
      case 1:
        buffer[position--] = (byte) ((value & 0x7F));
    }*/
  }

  final void writeUInt64NoTagClzIndex(long value) throws IOException {
    int sign = 0;
    switch (LEADING_ZEROS_TO_BYTES_64[Long.numberOfLeadingZeros(value)]) {
      case 10:
        buffer[position--] = (byte) ((value >>> 63) | sign);
        sign = 0x80;
      case 9:
        buffer[position--] = (byte) (((value >>> 56) & 0x7F) | sign);
        sign = 0x80;
      case 8:
        buffer[position--] = (byte) (((value >>> 49) & 0x7F) | sign);
        sign = 0x80;
      case 7:
        buffer[position--] = (byte) (((value >>> 42) & 0x7F) | sign);
        sign = 0x80;
      case 6:
        buffer[position--] = (byte) (((value >>> 35) & 0x7F) | sign);
        sign = 0x80;
      case 5:
        buffer[position--] = (byte) (((value >>> 28) & 0x7F) | sign);
        sign = 0x80;
      case 4:
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | sign);
        sign = 0x80;
      case 3:
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | sign);
        sign = 0x80;
      case 2:
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | sign);
        sign = 0x80;
      case 1:
        buffer[position--] = (byte) ((value & 0x7F) | sign);
    }
  }

  final void writeUInt64NoTagClzDiv(long value) throws IOException {
    int sign = 0;
    switch ((((64 - Long.numberOfLeadingZeros(value)) - 1) / 7) + 1) {
      case 10:
        buffer[position--] = (byte) ((value >>> 63) | sign);
        sign = 0x80;
      case 9:
        buffer[position--] = (byte) (((value >>> 56) & 0x7F) | sign);
        sign = 0x80;
      case 8:
        buffer[position--] = (byte) (((value >>> 49) & 0x7F) | sign);
        sign = 0x80;
      case 7:
        buffer[position--] = (byte) (((value >>> 42) & 0x7F) | sign);
        sign = 0x80;
      case 6:
        buffer[position--] = (byte) (((value >>> 35) & 0x7F) | sign);
        sign = 0x80;
      case 5:
        buffer[position--] = (byte) (((value >>> 28) & 0x7F) | sign);
        sign = 0x80;
      case 4:
        buffer[position--] = (byte) (((value >>> 21) & 0x7F) | sign);
        sign = 0x80;
      case 3:
        buffer[position--] = (byte) (((value >>> 14) & 0x7F) | sign);
        sign = 0x80;
      case 2:
        buffer[position--] = (byte) (((value >>> 7) & 0x7F) | sign);
        sign = 0x80;
      case 1:
        buffer[position--] = (byte) ((value & 0x7F) | sign);
    }
  }

  @Override
  public final void writeUInt64NoTag(long value) throws IOException {
    /*if ((value & ~0x7FL) == 0) {
      //System.err.println("1-byte");
      buffer[position--] = (byte) value;
      return;
    }
    if ((value & (~0x7FL << 7)) == 0) {
      //System.err.println("2-byte");
      buffer[position--] = (byte) (value >>> 7);
      buffer[position--] = (byte) ((value & 0x7F) | 0x80);
      return;
    }
    if ((value & (~0x7FL << 14)) == 0) {
      //System.err.println("3-byte");
      buffer[position--] = (byte) (value >>> 14);
      buffer[position--] = (byte) (((value >>> 7) & 0x7FL) | 0x80);
      buffer[position--] = (byte) ((value & 0x7FL) | 0x80);
      return;
    }
    writeUInt64NoTagSlow(value);
  }

  private void writeUInt64NoTagSlow(long value) throws IOException {*/
    writeUInt64NoTagCalc(value);
  }

  public final void writeFixed64NoTag(long value) throws IOException {
    if (position - 7 < offset) {
      throw new OutOfSpaceException(
              new IndexOutOfBoundsException(
                      String.format("Pos: %d, offset: %d, len: %d", position, offset, 8)));
    }

    buffer[position--] = (byte) ((int) (value >> 56) & 0xFF);
    buffer[position--] = (byte) ((int) (value >> 48) & 0xFF);
    buffer[position--] = (byte) ((int) (value >> 40) & 0xFF);
    buffer[position--] = (byte) ((int) (value >> 32) & 0xFF);
    buffer[position--] = (byte) ((int) (value >> 24) & 0xFF);
    buffer[position--] = (byte) ((int) (value >> 16) & 0xFF);
    buffer[position--] = (byte) ((int) (value >> 8) & 0xFF);
    buffer[position--] = (byte) ((int) (value) & 0xFF);
  }

  public final void write(byte[] value, int offset, int length) throws IOException {
    final int startPos = (position - length) + 1;
    if (startPos < this.offset) {
      throw new OutOfSpaceException(
              new IndexOutOfBoundsException(
                      String.format("Pos: %d, offset: %d, len: %d", position, offset, length)));
    }
    System.arraycopy(value, offset, buffer, startPos, length);
    position = startPos - 1;
  }

  public final void writeLazy(byte[] value, int offset, int length) throws IOException {
    write(value, offset, length);
  }

  // TODO(nmittler): do in reverse.
  public final void write(ByteBuffer value) throws IOException {
    final int length = value.remaining();
    try {
      value.get(buffer, position, length);
      position += length;
    } catch (IndexOutOfBoundsException e) {
      throw new OutOfSpaceException(
              new IndexOutOfBoundsException(
                      String.format("Pos: %d, limit: %d, len: %d", position, limit, length)));
    }
  }

  public final void writeLazy(ByteBuffer value) throws IOException {
    write(value);
  }

  public final void writeStringNoTag(String value) throws IOException {
    final int oldPosition = position;
    try {
      position = Utf8.encodeReverse(value, buffer, offset, spaceLeft());
      int length = oldPosition - position;
      writeUInt32NoTag(length);
    } catch (Utf8.UnpairedSurrogateException e) {
      // Roll back the change - we fall back to inefficient path.
      position = oldPosition;

      inefficientWriteStringNoTag(value);
    } catch (IndexOutOfBoundsException e) {
      throw new OutOfSpaceException(e);
    }
  }

  public void flush() {
    // Do nothing.
  }

  public final int spaceLeft() {
    return position - offsetMinusOne;
  }

  @Override
  public final int getTotalBytesWritten() {
    return (limit - position) - 1;
  }

  private void inefficientWriteStringNoTag(String value)
          throws IOException {

    // Unfortunately there does not appear to be any way to tell Java to encode
    // UTF-8 directly into our buffer, so we have to let it create its own byte
    // array and then copy.
    final byte[] bytes = value.getBytes(Utf8.UTF_8);
    try {
      writeLazy(bytes, 0, bytes.length);
      writeUInt32NoTag(bytes.length);
    } catch (IndexOutOfBoundsException e) {
      throw new OutOfSpaceException(e);
    }
  }
}
