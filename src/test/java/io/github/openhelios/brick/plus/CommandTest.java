package io.github.openhelios.brick.plus;

import org.junit.Assert;
import org.junit.Test;

public class CommandTest {

  @Test
  public void testMapSpeed() {
    final int index = 6;
    Assert.assertEquals(126, Command.motorSpeed(Port.A, 100)[index]);
    Assert.assertEquals(127, Command.motorSpeed(Port.A, 00)[index]);
    Assert.assertEquals(255, Command.motorSpeed(Port.A, -1)[index] & 0xff);
    Assert.assertEquals(128, Command.motorSpeed(Port.A, -100)[index] & 0xff);
  }

  public void testSetAbsoluteAngle() {
    final int index = 6;
    Assert.assertEquals(0, Command.setAbsoluteAngle(Port.A, 0)[index]);
    Assert.assertEquals(1, Command.setAbsoluteAngle(Port.A, 1)[index]);
    Assert.assertEquals(100, Byte.toUnsignedInt(Command.setAbsoluteAngle(Port.A, 100)[index]));
    Assert.assertEquals(129, Byte.toUnsignedInt(Command.setAbsoluteAngle(Port.A, 129)[index]));
  }

}
