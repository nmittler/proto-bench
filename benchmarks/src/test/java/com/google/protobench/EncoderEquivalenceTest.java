package com.google.protobench;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class EncoderEquivalenceTest {

  @Test
  public void writeUInt32ShouldBeEquivalent() throws Exception {
    for(VarintInput input : VarintInput.get32BitValues()) {
      verifyUInt32Equivalence(input.getSerializedSize(), input.nextIntValue());
      verifyUInt32Equivalence(input.getSerializedSize(), input.nextIntValue());
      verifyUInt32Equivalence(input.getSerializedSize(), input.nextIntValue());
      verifyUInt32Equivalence(input.getSerializedSize(), input.nextIntValue());
    }
  }

  @Test
  public void writeUInt64ShouldBeEquivalent() throws Exception {
    for(VarintInput input : VarintInput.get64BitValues()) {
      verifyUInt64Equivalence(input.getSerializedSize(), input.nextLongValue());
      verifyUInt64Equivalence(input.getSerializedSize(), input.nextLongValue());
      verifyUInt64Equivalence(input.getSerializedSize(), input.nextLongValue());
      verifyUInt64Equivalence(input.getSerializedSize(), input.nextLongValue());
    }
  }

  private void verifyUInt32Equivalence(byte numBytes, int value) throws Exception {
    byte[] forwardBytes = writeUInt32Forward(value);
    byte[] reverseBytes = writeUInt32Reverse(value);
    String message = "numBytes=" + numBytes + ", value=" + value;
    Assert.assertEquals(message, numBytes, forwardBytes.length);
    Assert.assertEquals(message, numBytes, reverseBytes.length);
    Assert.assertArrayEquals(message, forwardBytes, reverseBytes);
  }

  private void verifyUInt64Equivalence(int numBytes, long value) throws Exception {
    byte[] forwardBytes = writeUInt64Forward(value);
    byte[] reverseBytes = writeUInt64Reverse(value);
    String message = "numBytes=" + numBytes + ", value=" + value;
    Assert.assertEquals(message, numBytes, forwardBytes.length);
    Assert.assertEquals(message, numBytes, reverseBytes.length);
    Assert.assertArrayEquals(message, forwardBytes, reverseBytes);
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
    encoder.writeUInt32NoTag(value);
    int startIx = bytes.length - encoder.getTotalBytesWritten();
    return Arrays.copyOfRange(bytes, startIx, bytes.length);
  }

  private byte[] writeUInt64Reverse(long value) throws Exception {
    byte[] bytes = new byte[100];
    ReverseEncoder encoder = new ReverseEncoder(bytes, 0, bytes.length);
    encoder.writeUInt64NoTag(value);
    int startIx = bytes.length - encoder.getTotalBytesWritten();
    return Arrays.copyOfRange(bytes, startIx, bytes.length);
  }
}
