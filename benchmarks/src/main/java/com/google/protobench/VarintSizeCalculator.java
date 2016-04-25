package com.google.protobench;

public enum VarintSizeCalculator {
  DEFAULT(new Strategy() {
    @Override
    byte calcUInt32Size(int value) {
      return Utils.computeUInt32SizeNoTag(value);
    }

    @Override
    byte calcUInt64Size(long value) {
      return Utils.computeUInt64SizeNoTag(value);
    }
  }),
  CLZ_INDEX(new Strategy() {
    @Override
    byte calcUInt32Size(int value) {
      return Utils.computeUInt32SizeNoTagClzIndex(value);
    }

    @Override
    byte calcUInt64Size(long value) {
      return Utils.computeUInt64SizeNoTagClzIndex(value);
    }
  }),
  CLZ_DIV(new Strategy() {
    @Override
    byte calcUInt32Size(int value) {
      return Utils.computeUInt32SizeNoTagClzDiv(value);
    }

    @Override
    byte calcUInt64Size(long value) {
      return Utils.computeUInt64SizeNoTagClzDiv(value);
    }
  });

  private final Strategy strategy;

  VarintSizeCalculator(Strategy strategy) {
    this.strategy = strategy;
  }

  byte calcUInt32Size(int value) {
    return strategy.calcUInt32Size(value);
  }

  byte calcUInt64Size(long value) {
    return strategy.calcUInt64Size(value);
  }

  private static abstract class Strategy {
    abstract byte calcUInt32Size(int value);

    abstract byte calcUInt64Size(long value);
  }
}
