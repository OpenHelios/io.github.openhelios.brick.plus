package io.github.openhelios.brick.plus;

public enum PropertyType {

  //	ADVERTISING_NAME = 0x01,
  //	BUTTON = 0x02,
  FIRMWARE_VERSION(0x03),
  //	HW_VERSION = 0x04,
  //	RSSI = 0x05,

  BATTERY_VOLTAGE(0x06),

  //	BATTERY_TYPE = 0x07,
  //	MANUFACTURER_NAME = 0x08,
  //	RADIO_FIRMWARE_VERSION = 0x09,
  //	LEGO_WIRELESS_PROTOCOL_VERSION = 0x0A,
  //	SYSTEM_TYPE_ID = 0x0B,
  //	HW_NETWORK_ID = 0x0C,
  //	PRIMARY_MAC_ADDRESS = 0x0D,
  //	SECONDARY_MAC_ADDRESS = 0x0E,
  //	HARDWARE_NETWORK_FAMILY = 0x0F,

  ;

  public final byte value;

  PropertyType(final int value) {
    this.value = (byte) value;
  }

}
