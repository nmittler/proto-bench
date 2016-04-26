package com.google.protobench;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.google.protobench.TestMessage.SerializedSizeManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.Arrays;

@RunWith(JUnit4.class)
public class EncoderEquivalenceTest {
  private static final int STRING_LENGTH = 5;
  private static final int NUM_REPEATED_FIELDS = 5;
  private static final int TREE_HEIGHT = 2;
  private static final int BRANCHING_FACTOR = 2;

  @Test
  public void messagesShouldBeEquivalent() throws IOException {
    int numMessages = Utils.calcNodesInTree(BRANCHING_FACTOR, TREE_HEIGHT);
    SerializedSizeManager sizeManager = new SerializedSizeManager(numMessages);
    TestMessage message = TestMessage.newRandomInstance(0, STRING_LENGTH, NUM_REPEATED_FIELDS,
            TREE_HEIGHT, BRANCHING_FACTOR, sizeManager);

    assertArrayEquals(writeMessageForward(message), writeMessageReverse(message));
  }

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

  private byte[] writeMessageForward(TestMessage message) throws IOException {
    byte[] bytes = new byte[1024 * 1024];
    ForwardEncoder encoder = new ForwardEncoder(bytes, 0, bytes.length);
    encoder.encodeMessageNoTag(message);
    return Arrays.copyOfRange(bytes, 0, encoder.getTotalBytesWritten());
  }

  private byte[] writeMessageReverse(TestMessage message) throws IOException {
    byte[] bytes = new byte[1024 * 1024];
    ReverseEncoder encoder = new ReverseEncoder(bytes, 0, bytes.length);
    encoder.encodeMessageNoTag(message);

    int startIx = bytes.length - encoder.getTotalBytesWritten();
    return Arrays.copyOfRange(bytes, startIx, bytes.length);
  }

  private void verifyUInt32Equivalence(byte numBytes, int value) throws Exception {
    byte[] forwardBytes = writeUInt32Forward(value);
    byte[] reverseBytes = writeUInt32Reverse(value);
    String message = "numBytes=" + numBytes + ", value=" + value;
    assertEquals(message, numBytes, forwardBytes.length);
    assertEquals(message, numBytes, reverseBytes.length);
    assertArrayEquals(message, forwardBytes, reverseBytes);
  }

  private void verifyUInt64Equivalence(int numBytes, long value) throws Exception {
    byte[] forwardBytes = writeUInt64Forward(value);
    byte[] reverseBytes = writeUInt64Reverse(value);
    String message = "numBytes=" + numBytes + ", value=" + value;
    assertEquals(message, numBytes, forwardBytes.length);
    assertEquals(message, numBytes, reverseBytes.length);
    assertArrayEquals(message, forwardBytes, reverseBytes);
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
