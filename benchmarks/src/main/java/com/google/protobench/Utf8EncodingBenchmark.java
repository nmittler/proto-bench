package com.google.protobench;

import com.google.protobench.Utf8Utils.MaxCodePoint;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.nio.ByteBuffer;

@State(Scope.Benchmark)
@Fork(1)
public class Utf8EncodingBenchmark {
  private static final Utf8.Processor safeProcessor = new Utf8.SafeProcessor();
  private static final Utf8.Processor unsafeProcessor = new Utf8.UnsafeProcessor();

  public enum BufferType {
    ARRAY,
    ARRAY_UNSAFE,
    NIO_HEAP,
    NIO_DIRECT,
    NIO_DIRECT_UNSAFE
  }

  public enum Direction {
    FORWARD,
    REVERSE
  }

  @Param
  private BufferType bufferType;

  @Param
  private Direction direction;

  /**
   * The default values of maxCodePoint below provide pretty good performance models of different
   * kinds of common human text.
   *
   * @see MaxCodePoint#decode
   */
  @Param({"0x80", "0x90", "0x800", "0x10000", "0x10ffff"})
  private String maxCodePoint;

  @Param({"100"})
  private int stringCount;

  @Param({"16384"})
  private int charCount;

  private String[] strings;

  private Encoder encoder;

  private int strIx;

  private interface Encoder {
    void encode(String input);
  }

  private final class ArrayEncoder implements Encoder {
    private final byte[] buffer;

    ArrayEncoder(byte[] buffer) {
      this.buffer = buffer;
    }

    @Override
    public void encode(String input) {
      if (direction == Direction.FORWARD) {
        safeProcessor.encodeUtf8(input, buffer, 0, buffer.length);
      } else {
        safeProcessor.encodeUtf8Reverse(input, buffer, 0, buffer.length);
      }
    }
  }

  private final class UnsafeArrayEncoder implements Encoder {
    private final byte[] buffer;

    UnsafeArrayEncoder(byte[] buffer) {
      this.buffer = buffer;
    }

    @Override
    public void encode(String input) {
      if (direction == Direction.FORWARD) {
        unsafeProcessor.encodeUtf8(input, buffer, 0, buffer.length);
      } else {
        unsafeProcessor.encodeUtf8Reverse(input, buffer, 0, buffer.length);
      }
    }
  }

  private final class SafeNioEncoder implements Encoder {
    private final ByteBuffer buffer;

    SafeNioEncoder(ByteBuffer buffer) {
      this.buffer = buffer;
    }

    @Override
    public final void encode(String input) {
      buffer.position(0);
      if (direction == Direction.FORWARD) {
        safeProcessor.encodeUtf8(input, buffer);
      } else {
        safeProcessor.encodeUtf8Reverse(input, buffer);
      }
    }
  }

  private final class UnsafeNioEncoder implements Encoder {
    private final ByteBuffer buffer;

    UnsafeNioEncoder(ByteBuffer buffer) {
      this.buffer = buffer;
    }

    @Override
    public final void encode(String input) {
      buffer.position(0);
      if (direction == Direction.FORWARD) {
        unsafeProcessor.encodeUtf8(input, buffer);
      } else {
        unsafeProcessor.encodeUtf8Reverse(input, buffer);
      }
    }
  }

  @Setup
  public void setUp() {
    strings = Utf8Utils.randomStrings(stringCount, charCount, MaxCodePoint.valueOf(maxCodePoint));
    strIx = 0;

    switch (bufferType) {
      case ARRAY:
        encoder = new ArrayEncoder(new byte[1024 * 1024]);
        break;
      case ARRAY_UNSAFE:
        encoder = new UnsafeArrayEncoder(new byte[1024 * 1024]);
        break;
      case NIO_HEAP:
        encoder = new SafeNioEncoder(ByteBuffer.wrap(new byte[1024 * 1024]));
        break;
      case NIO_DIRECT:
        encoder = new SafeNioEncoder(ByteBuffer.allocateDirect(1024 * 1024));
        break;
      case NIO_DIRECT_UNSAFE:
        encoder = new UnsafeNioEncoder(ByteBuffer.allocateDirect(1024 * 1024));
        break;
    }
  }

  @Benchmark
  public void encode() {
    encoder.encode(strings[strIx++]);
    if (strIx >= strings.length) {
      strIx = 0;
    }
  }
}