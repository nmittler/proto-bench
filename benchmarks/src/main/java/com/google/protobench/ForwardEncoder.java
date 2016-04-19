package com.google.protobench;

import static com.google.protobench.UnsafeUtil.ARRAY_BASE_OFFSET;
import static com.google.protobench.UnsafeUtil.HAS_UNSAFE_ARRAY_OPERATIONS;
import static com.google.protobench.UnsafeUtil.UNSAFE;
import static com.google.protobench.WireFormat.MAX_VARINT_SIZE;

import java.io.IOException;
import java.nio.ByteBuffer;

final class ForwardEncoder implements Encoder {
  private final byte[] buffer;
  private final int offset;
  private final int limit;
  private int position;

  ForwardEncoder(byte[] buffer, int offset, int length) {
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
    reset();
  }

  @Override
  public void encodeMessage(int fieldNumber, TestMessage message) throws IOException {
    writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    encodeMessageNoTag(message);
  }

  @Override
  public void encodeMessageNoTag(TestMessage message) throws IOException {
    writeUInt32NoTag(message.getSerializedSize());

    writeUInt32(3, message.optionalInt);
    writeUInt64(4, message.optionalLong);
    writeFloat(11, message.optionalFloat);
    writeDouble(12, message.optionalDouble);
    writeBool(13, message.optionalBoolean);
    writeString(14, message.optionalString);
    writeByteArray(15, message.optionalBytes);

    if (message.children != null) {
      for (int ix = 0; ix < message.children.length; ++ix) {
        encodeMessage(18, message.children[ix]);
      }
    }

    if (message.repeatedInt != null) {
      for (int ix = 0; ix < message.repeatedInt.length; ++ix) {
        writeUInt32(33, message.repeatedInt[ix]);
      }
    }
    if (message.repeatedLong != null) {
      for (int ix = 0; ix < message.repeatedLong.length; ++ix) {
        writeUInt64(34, message.repeatedLong[ix]);
      }
    }
    if (message.repeatedFloat != null) {
      for (int ix = 0; ix < message.repeatedFloat.length; ++ix) {
        writeFloat(41, message.repeatedFloat[ix]);
      }
    }
    if (message.repeatedDouble != null) {
      for (int ix = 0; ix < message.repeatedDouble.length; ++ix) {
        writeDouble(42, message.repeatedDouble[ix]);
      }
    }
    if (message.repeatedBoolean != null) {
      for (int ix = 0; ix < message.repeatedBoolean.length; ++ix) {
        writeBool(43, message.repeatedBoolean[ix]);
      }
    }
    if (message.repeatedString != null) {
      for (int ix = 0; ix < message.repeatedString.length; ++ix) {
        writeString(44, message.repeatedString[ix]);
      }
    }
    if (message.repeatedBytes != null) {
      for (int ix = 0; ix < message.repeatedBytes.length; ++ix) {
        writeByteArray(45, message.repeatedBytes[ix]);
      }
    }
  }

  @Override
  public void reset() {
    position = offset;
  }

  public final void writeTag(final int fieldNumber, final int wireType) throws IOException {
    writeUInt32NoTag(WireFormat.makeTag(fieldNumber, wireType));
  }

  public final void writeInt32(final int fieldNumber, final int value) throws IOException {
    writeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
    writeInt32NoTag(value);
  }

  public final void writeUInt32(final int fieldNumber, final int value) throws IOException {
    writeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
    writeUInt32NoTag(value);
  }

  public final void writeFixed32(final int fieldNumber, final int value) throws IOException {
    writeTag(fieldNumber, WireFormat.WIRETYPE_FIXED32);
    writeFixed32NoTag(value);
  }

  public final void writeUInt64(final int fieldNumber, final long value) throws IOException {
    writeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
    writeUInt64NoTag(value);
  }

  public final void writeFixed64(final int fieldNumber, final long value) throws IOException {
    writeTag(fieldNumber, WireFormat.WIRETYPE_FIXED64);
    writeFixed64NoTag(value);
  }

  public final void writeBool(final int fieldNumber, final boolean value) throws IOException {
    writeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
    write((byte) (value ? 1 : 0));
  }

  public void writeFloat(int fieldNumber, float value) throws IOException {
    writeFixed32(fieldNumber, Float.floatToRawIntBits(value));
  }

  public void writeDouble(int fieldNumber, double value) throws IOException {
    writeFixed64(fieldNumber, Double.doubleToRawLongBits(value));
  }

  public final void writeString(final int fieldNumber, final String value) throws IOException {
    writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    writeStringNoTag(value);
  }

  public final void writeByteArray(final int fieldNumber, final byte[] value) throws IOException {
    writeByteArray(fieldNumber, value, 0, value.length);
  }

  public final void writeByteArray(
          final int fieldNumber, final byte[] value, final int offset, final int length)
          throws IOException {
    writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    writeByteArrayNoTag(value, offset, length);
  }

  public final void writeByteBuffer(final int fieldNumber, final ByteBuffer value)
          throws IOException {
    writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    writeUInt32NoTag(value.capacity());
    writeRawBytes(value);
  }

  public final void writeByteArrayNoTag(final byte[] value, int offset, int length)
          throws IOException {
    writeUInt32NoTag(length);
    write(value, offset, length);
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
    try {
      buffer[position++] = value;
    } catch (IndexOutOfBoundsException e) {
      throw new OutOfSpaceException(new IndexOutOfBoundsException(
              String.format("Pos: %d, limit: %d, len: %d", position, limit, 1)));
    }
  }

  public final void writeInt32NoTag(int value) throws IOException {
    if (value >= 0) {
      writeUInt32NoTag(value);
    } else {
      // Must sign-extend.
      writeUInt64NoTag(value);
    }
  }

  @Override
  public final void writeUInt32NoTag(int value) throws IOException {
    /*if (HAS_UNSAFE_ARRAY_OPERATIONS && spaceLeft() >= MAX_VARINT_SIZE) {
      long pos = ARRAY_BASE_OFFSET + position;
      while (true) {
        if ((value & ~0x7F) == 0) {
          UNSAFE.putByte(buffer, pos, (byte) value);
          position++;
          return;
        } else {
          UNSAFE.putByte(buffer, pos++, (byte) ((value & 0x7F) | 0x80));
          position++;
          value >>>= 7;
        }
      }
    } else {*/
      try {
        while (true) {
          if ((value & ~0x7F) == 0) {
            buffer[position++] = (byte) value;
            return;
          } else {
            buffer[position++] = (byte) ((value & 0x7F) | 0x80);
            value >>>= 7;
          }
        }
      } catch (IndexOutOfBoundsException e) {
        throw new OutOfSpaceException(
                new IndexOutOfBoundsException(
                        String.format("Pos: %d, limit: %d, len: %d", position, limit, 1)));
      }
    //}
  }

  public final void writeFixed32NoTag(int value) throws IOException {
    try {
      buffer[position++] = (byte) (value & 0xFF);
      buffer[position++] = (byte) ((value >> 8) & 0xFF);
      buffer[position++] = (byte) ((value >> 16) & 0xFF);
      buffer[position++] = (byte) ((value >> 24) & 0xFF);
    } catch (IndexOutOfBoundsException e) {
      throw new OutOfSpaceException(
              new IndexOutOfBoundsException(
                      String.format("Pos: %d, limit: %d, len: %d", position, limit, 1)));
    }
  }

  @Override
  public final void writeUInt64NoTag(long value) throws IOException {
    /*if (HAS_UNSAFE_ARRAY_OPERATIONS && spaceLeft() >= MAX_VARINT_SIZE) {
      long pos = ARRAY_BASE_OFFSET + position;
      while (true) {
        if ((value & ~0x7FL) == 0) {
          UNSAFE.putByte(buffer, pos, (byte) value);
          position++;
          return;
        } else {
          UNSAFE.putByte(buffer, pos++, (byte) (((int) value & 0x7F) | 0x80));
          position++;
          value >>>= 7;
        }
      }
    } else {*/
      try {
        while (true) {
          if ((value & ~0x7FL) == 0) {
            buffer[position++] = (byte) value;
            return;
          } else {
            buffer[position++] = (byte) (((int) value & 0x7F) | 0x80);
            value >>>= 7;
          }
        }
      } catch (IndexOutOfBoundsException e) {
        throw new OutOfSpaceException(
                new IndexOutOfBoundsException(
                        String.format("Pos: %d, limit: %d, len: %d", position, limit, 1)));
      }
   // }
  }

  public final void writeFixed64NoTag(long value) throws IOException {
    try {
      buffer[position++] = (byte) ((int) (value) & 0xFF);
      buffer[position++] = (byte) ((int) (value >> 8) & 0xFF);
      buffer[position++] = (byte) ((int) (value >> 16) & 0xFF);
      buffer[position++] = (byte) ((int) (value >> 24) & 0xFF);
      buffer[position++] = (byte) ((int) (value >> 32) & 0xFF);
      buffer[position++] = (byte) ((int) (value >> 40) & 0xFF);
      buffer[position++] = (byte) ((int) (value >> 48) & 0xFF);
      buffer[position++] = (byte) ((int) (value >> 56) & 0xFF);
    } catch (IndexOutOfBoundsException e) {
      throw new OutOfSpaceException(
              new IndexOutOfBoundsException(
                      String.format("Pos: %d, limit: %d, len: %d", position, limit, 1)));
    }
  }

  public final void write(byte[] value, int offset, int length) throws IOException {
    try {
      System.arraycopy(value, offset, buffer, position, length);
      position += length;
    } catch (IndexOutOfBoundsException e) {
      throw new OutOfSpaceException(
              new IndexOutOfBoundsException(
                      String.format("Pos: %d, limit: %d, len: %d", position, limit, length)));
    }
  }

  public final void writeLazy(byte[] value, int offset, int length) throws IOException {
    write(value, offset, length);
  }

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
      // UTF-8 byte length of the string is at least its UTF-16 code unit length (value.length()),
      // and at most 3 times of it. We take advantage of this in both branches below.
      final int maxLength = value.length() * Utf8.MAX_BYTES_PER_CHAR;
      final int maxLengthVarIntSize = Utils.computeUInt32SizeNoTag(maxLength);
      final int minLengthVarIntSize = Utils.computeUInt32SizeNoTag(value.length());
      if (minLengthVarIntSize == maxLengthVarIntSize) {
        position = oldPosition + minLengthVarIntSize;
        int newPosition = Utf8.encode(value, buffer, position, spaceLeft());
        // Since this class is stateful and tracks the position, we rewind and store the state,
        // prepend the length, then reset it back to the end of the string.
        position = oldPosition;
        int length = newPosition - oldPosition - minLengthVarIntSize;
        writeUInt32NoTag(length);
        position = newPosition;
      } else {
        int length = Utf8.encodedLength(value);
        writeUInt32NoTag(length);
        position = Utf8.encode(value, buffer, position, spaceLeft());
      }
    } catch (Utf8.UnpairedSurrogateException e) {
      // Roll back the change - we fall back to inefficient path.
      position = oldPosition;

      // TODO(nathanmittler): We should throw an IOException here instead.
      inefficientWriteStringNoTag(value);
    } catch (IndexOutOfBoundsException e) {
      throw new OutOfSpaceException(e);
    }
  }

  public void flush() {
    // Do nothing.
  }

  public final int spaceLeft() {
    return limit - position;
  }

  @Override
  public final int getTotalBytesWritten() {
    return position - offset;
  }

  private void inefficientWriteStringNoTag(String value)
          throws IOException {

    // Unfortunately there does not appear to be any way to tell Java to encode
    // UTF-8 directly into our buffer, so we have to let it create its own byte
    // array and then copy.
    // TODO(dweis): Consider using nio Charset methods instead.
    final byte[] bytes = value.getBytes(Utf8.UTF_8);
    try {
      writeUInt32NoTag(bytes.length);
      writeLazy(bytes, 0, bytes.length);
    } catch (IndexOutOfBoundsException e) {
      throw new OutOfSpaceException(e);
    }
  }
}
