package io.github.openhelios.brick.plus;

public enum MotorType {

  /**
   * Large motor LEGO 22169 (6214085) with integrated sensor.
   */
  L(0x2e),

  /**
   * Extra large motor LEGO 88014 (6214088) with integrated sensor.
   */
  XL(0x2f),

  /**
   * Large angular motor LEGO 45602 with integrated sensor +/- 1°.
   *
   * @see https://education.lego.com/en-us/products/lego-technic-large-angular-motor/45602
   */
  LA(0x4c),

  ;

  public final byte value;

  MotorType(final int value) {
    this.value = (byte) value;
  }

  public static MotorType valueOf(final byte value) {
    for (final MotorType type : values()) {
      if (value == type.value) {
        return type;
      }
    }
    throw new IllegalStateException("unknown motor type " + value);
  }

}
