package io.github.openhelios.brick.plus;

public enum MessageType {

  BATTERY_INFO(0x01),

  PORT_CONNECTION_INFO(0x04),

  PORT_VALUE_COMBINED_MODE(0x46),

  ;

  public final byte value;

  MessageType(final int value) {
    this.value = (byte) value;
  }

}
