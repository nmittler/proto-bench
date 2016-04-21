package com.google.protobench;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

public class EncoderEquivalenceTest {
  private static Random RANDOM = new Random();

  @Test
  public void writeUInt32ShouldBeEquivalent() throws Exception {
    for(int numBytes = 5; numBytes > 0; --numBytes) {
      writeUInt32ShouldBeEquivalent(numBytes);
    }
  }

  @Test
  public void writeUInt64ShouldBeEquivalent() throws Exception {
    for(int numBytes = 10; numBytes > 0; --numBytes) {
      writeUInt64ShouldBeEquivalent(numBytes);
    }
  }
  private void writeUInt32ShouldBeEquivalent(int numBytes) throws Exception {
    for(int i = 0; i < 100; ++i) {
      int value = randomUInt32(numBytes);
      verifyUInt32Equivalence(numBytes, value);
    }
  }

  private void writeUInt64ShouldBeEquivalent(int numBytes) throws Exception {
    for(int i = 0; i < 100; ++i) {
      long value = randomUInt64(numBytes);
      verifyUInt64Equivalence(numBytes, value);
    }
  }

  private void verifyUInt32Equivalence(int numBytes, int value) throws Exception {
    byte[] forwardBytes = writeUInt32Forward(value);
    byte[] reverseBytes = writeUInt32Reverse(value);
    Assert.assertEquals(numBytes, forwardBytes.length);
    Assert.assertEquals(numBytes, reverseBytes.length);
    Assert.assertArrayEquals("numBytes=" + numBytes + ", value" + value + "", forwardBytes, reverseBytes);
  }

  private void verifyUInt64Equivalence(int numBytes, long value) throws Exception {
    byte[] forwardBytes = writeUInt64Forward(value);
    byte[] reverseBytes = writeUInt64Reverse(value);
    Assert.assertEquals(numBytes, forwardBytes.length);
    Assert.assertEquals(numBytes, reverseBytes.length);
    Assert.assertArrayEquals("numBytes=" + numBytes + ", value" + value + "", forwardBytes, reverseBytes);
  }

  private byte[] writeUInt32Forward(int value) throws Exception {
    byte[] bytes = new byte[100];
    ForwardEncoder encoder = new ForwardEncoder(bytes, 0, bytes.length);
    encoder.writeUInt32NoTag(value);
    int endIx = encoder.getTotalBytesWritten();
    return Arrays.copyOfRange(bytes, 0, endIx);
  }

  private byte[] writeUInt64Forward(long value) throws Exception {
    byte[] bytes = new byte[100];
    ForwardEncoder encoder = new ForwardEncoder(bytes, 0, bytes.length);
    encoder.writeUInt64NoTag(value);
    int endIx = encoder.getTotalBytesWritten();
    return Arrays.copyOfRange(bytes, 0, endIx);
  }

  private byte[] writeUInt32Reverse(int value) throws Exception {
    byte[] bytes = new byte[100];
    ReverseEncoder encoder = new ReverseEncoder(bytes, 0, bytes.length);
    encoder.writeUInt32NoTagClzDiv(value);
    int startIx = bytes.length - encoder.getTotalBytesWritten();
    return Arrays.copyOfRange(bytes, startIx, bytes.length);
  }

  private byte[] writeUInt64Reverse(long value) throws Exception {
    byte[] bytes = new byte[100];
    ReverseEncoder encoder = new ReverseEncoder(bytes, 0, bytes.length);
    encoder.writeUInt64NoTagClzDiv(value);
    int startIx = bytes.length - encoder.getTotalBytesWritten();
    return Arrays.copyOfRange(bytes, startIx, bytes.length);
  }

  private int randomUInt32(int numBytes) {
    int value;
    if (numBytes == 5) {
      value = -RANDOM.nextInt(Integer.MAX_VALUE) - 1;
    } else {
      int msb = 1 << ((numBytes * 7) - 1);
      int max = (msb - 1) | msb;
      int min = msb >>> 6;
      int range = max - min;
      int nextInt = RANDOM.nextInt(range);
      value = nextInt + min;
    }
    return value;
  }

  private long randomUInt64(final int numBytes) {
    long value;
    if (numBytes == 10) {
      // Negative values.
      value = -RANDOM.nextInt(Integer.MAX_VALUE) - 1;
    } else {
      long msb = 1L << ((numBytes * 7) - 1);
      long max = (msb - 1) | msb;
      long min = msb >>> 6;
      long range = max - min;
      long nextLong = Math.abs(RANDOM.nextLong());
      value = (nextLong % range) + min;
    }
    return value;
  }
}
