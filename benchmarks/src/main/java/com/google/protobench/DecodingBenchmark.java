package com.google.protobench;

import com.google.protobuf.ByteString;

import io.protostuff.ByteArrayInput;
import io.protostuff.ProtobufIOUtil;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import benchmark.protobuf.UnittestProto;
import benchmark.protostuff.NestedTestAllTypes;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

@State(Scope.Benchmark)
@Fork(1)
public class DecodingBenchmark {

  public enum Impl {
    PROTOBUF,
    PROTOSTUFF
  }

  @Param
  private Impl impl;

  private byte[] bytes;

  @Setup
  public void setUp() throws Exception {
    InputStream input = getClass().getResourceAsStream("/com/google/protobench/test_proto.bin");
    bytes = ByteString.readFrom(input).toByteArray();
  }

  @Benchmark
  public void decode() throws Exception {
    switch(impl) {
      case PROTOBUF: {
        UnittestProto.NestedTestAllTypes.parseFrom(bytes);
        break;
      }
      case PROTOSTUFF: {
        NestedTestAllTypes obj = new NestedTestAllTypes();
        ProtobufIOUtil.mergeFrom(bytes, obj, obj.cachedSchema());
        break;
      }
    }
  }

  public static void main(String[] args) throws Exception {
    DecodingBenchmark bm = new DecodingBenchmark();
    bm.impl = Impl.PROTOBUF;
    bm.setUp();

    while (true) {
      bm.decode();
    }
  }
}
