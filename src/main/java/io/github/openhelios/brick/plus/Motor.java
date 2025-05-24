package io.github.openhelios.brick.plus;

import java.util.List;

public class Motor {

  public final Port port;

  public final MotorType type;

  private int absolutePosition;

  private int relativePosition;

  private final MotorService motorService;

  private IMotorListener listener;

  public Motor(final Port port, final MotorType type, final MotorService motorService) {
    this.port = port;
    this.type = type;
    this.motorService = motorService;
  }

  public Port getPort() {
    return port;
  }

  public MotorType getType() {
    return type;
  }

  /**
   * @param speed The speed between -100 and 100.
   */
  public void setSpeed(final int speed) {
    motorService.setSpeed(port, speed);
  }

  /**
   * @param angle The angle in degree.
   */
  public void moveByAngle(final int angle) {
    motorService.moveByAngle(port, angle);
  }

  /**
   * @param angle The absolute angle in degree.
   */
  public void setInitialAbsoluteAngle(final int angle) {
    motorService.setInitialAbsoluteAngle(port, angle);
  }

  /**
   * @param angle The absolute angle in degree.
   */
  public void setAbsoluteAngle(final int angle) {
    motorService.setAbsoluteAngle(port, angle);
  }

  protected void onMotorPortValueCombined(final List<Byte> data) {
    final byte modeMask = data.get(5);
    int i = 6;
    if (0 != (modeMask & 0x01)) {
      relativePosition = Byte.toUnsignedInt(data.get(i++)) + (data.get(i++) << 8);
    }
    if (0 != (modeMask & 0x02)) {
      if (i + 4 <= data.size()) {
        absolutePosition = Byte.toUnsignedInt(data.get(i++)) //
            + (Byte.toUnsignedInt(data.get(i++)) << 8) //
            + (Byte.toUnsignedInt(data.get(i++)) << 16) //
            + (data.get(i++) << 32);
      } else if (i + 2 < data.size()) {
        absolutePosition = Byte.toUnsignedInt(data.get(i++)) //
            + (data.get(i++) << 8);
      } else {
        absolutePosition = data.get(i);
      }
    }
    if (null != listener) {
      listener.onPositionChanged(this);
    }
  }

  /**
   * @return The absolute motor position in degree between -180 and 179.
   */
  public int getAbsolutePosition() {
    return absolutePosition;
  }

  /**
   * @return The relative motor position in degree between -2^31-1 and 2^31-1.
   */
  public int getRelativePosition() {
    return relativePosition;
  }

  public void setListener(final IMotorListener listener) {
    this.listener = listener;
  }

}
