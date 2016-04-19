package com.google.protobench;

import java.io.IOException;

interface Encoder {

  /**
   * If you create a CodedOutputStream around a simple flat array, you must not attempt to write
   * more bytes than the array has space.  Otherwise, this exception will be thrown.
   */
  class OutOfSpaceException extends IOException {
    private static long serialVersionUID = -6947486886997889499L;

    private static String MESSAGE =
            "CodedOutputStream was writing to a flat byte array and ran out of space.";

    OutOfSpaceException() {
      super(MESSAGE);
    }

    OutOfSpaceException(Throwable cause) {
      super(MESSAGE, cause);
    }
  }

  void encodeMessage(int fieldNumber, TestMessage message) throws IOException;

  void encodeMessageNoTag(TestMessage message) throws IOException;

  void writeUInt32NoTag(int value) throws IOException;
  void writeUInt64NoTag(long value) throws IOException;
  int getTotalBytesWritten();

  void reset();
}
