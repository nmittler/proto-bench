package com.google.protobench;

import static com.google.protobench.UnsafeUtil.ARRAY_BASE_OFFSET;
import static com.google.protobench.UnsafeUtil.HAS_UNSAFE_ARRAY_OPERATIONS;
import static com.google.protobench.UnsafeUtil.UNSAFE;

import java.io.IOException;
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
    int serializedSize = prevPos - position;
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

  private void writeUInt32NoTagUnsafe(int value) throws IOException {
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

  private void writeUInt32NoTagSafe(int value) throws IOException {
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

  @Override
  public final void writeUInt32NoTag(int value) throws IOException {
    if(HAS_UNSAFE_ARRAY_OPERATIONS) {
      writeUInt32NoTagUnsafe(value);
    } else {
      writeUInt32NoTagSafe(value);
    }
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

  private void writeUInt64NoTagUnsafe(long value) throws IOException {
    int size = Utils.computeUInt64SizeNoTag(value);
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
  }

  private void writeUInt64NoTagSafe(long value) throws IOException {
    int size = Utils.computeUInt64SizeNoTag(value);
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

  @Override
  public final void writeUInt64NoTag(long value) throws IOException {
    if (HAS_UNSAFE_ARRAY_OPERATIONS) {
      writeUInt64NoTagUnsafe(value);
    } else {
      writeUInt64NoTagSafe(value);
    }
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

  public final void writeStringNoTag(String value) throws IOException {
    final int oldPosition = position;
    try {
      int length = Utf8.encodeReverse(value, buffer, offset, spaceLeft());
      position -= length;
      writeUInt32NoTag(length);
    } catch (Utf8.UnpairedSurrogateException e) {
      // Roll back the change - we fall back to inefficient path.
      position = oldPosition;

      inefficientWriteStringNoTag(value);
    } catch (IndexOutOfBoundsException e) {
      throw new OutOfSpaceException(e);
    }
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
