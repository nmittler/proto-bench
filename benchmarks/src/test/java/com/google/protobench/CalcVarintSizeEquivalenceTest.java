package com.google.protobench;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CalcVarintSizeEquivalenceTest {

  @Test
  public void writeUInt32ShouldBeEquivalent() throws Exception {
    for(int i = 0; i<4; ++i) {
      for (VarintInput input : VarintInput.get32BitValues()) {
        int value = input.nextIntValue();
        byte expected = VarintSizeCalculator.DEFAULT.calcUInt32Size(value);
        Assert.assertEquals(expected, VarintSizeCalculator.CLZ_INDEX.calcUInt32Size(value));
        Assert.assertEquals(expected, VarintSizeCalculator.CLZ_DIV.calcUInt32Size(value));
      }
    }
  }

  @Test
  public void writeUInt64ShouldBeEquivalent() throws Exception {
    for(int i = 0; i<4; ++i) {
      for (VarintInput input : VarintInput.get64BitValues()) {
        long value = input.nextLongValue();
        byte expected = VarintSizeCalculator.DEFAULT.calcUInt64Size(value);
        Assert.assertEquals(expected, VarintSizeCalculator.CLZ_INDEX.calcUInt64Size(value));
        Assert.assertEquals(expected, VarintSizeCalculator.CLZ_DIV.calcUInt64Size(value));
      }
    }
  }
}
