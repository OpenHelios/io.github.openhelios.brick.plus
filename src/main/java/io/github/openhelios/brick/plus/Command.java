package io.github.openhelios.brick.plus;

import java.util.ArrayList;
import java.util.List;

/**
 * @see https://github.com/JorgePe/BOOSTreveng/blob/master/Motors.md
 */
public class Command {

  /**
   * @param port The port, where the motor is connected, i.e. A, B, C or D.
   * @param speed The speed between -100 and 100.
   * @return The created byte array for the command with the given parameters to set the motor speed.
   */
  public static byte[] motorSpeed(final Port port, final int speed) {
    return motorSpeed(port, speed, Brake.BRAKE);
  }

  /**
   * @param port The port, where the motor is connected, i.e. A, B, C or D.
   * @param speed The speed between -100 and 100.
   * @param brake The brake type.
   * @return The created byte array for the command with the given parameters to set the motor speed.
   */
  public static byte[] motorSpeed(final Port port, final int speed, final Brake brake) {
    final byte maxPower = 100;
    return create(new byte[] { (byte) 0x81, port.id, 0x11, 1, mapSpeed(speed), maxPower, brake.value, 3 });
  }

  /**
   * @param speed The speed between -100 and 100.
   * @return The related speed defined by the given one:
   *         <ul>
   *         <li>[-100, -1] maps to [-128, -1]</li>
   *         <li>0 maps to 127 and</li>
   *         <li>[1, 100] maps to [1, 126]</li>
   *         </ul>
   *         .
   */
  private static byte mapSpeed(final int speed) {
    if (0 == speed) {
      return 127; // brake
    }
    if (0 < speed) {
      return (byte) Math.round(1.26f * speed);
    }
    return (byte) Math.round(1.28f * speed);
  }

  /**
   * @param port The port, where the motor is connected, i.e. A, B, C or D.
   * @param speed The speed between -100 and 100.
   * @param brake The brake type.
   * @return The created byte array for the command with the given parameters to set the motor speed.
   */
  public static byte[] motorMoveByAngle(final Port port, final int angle) {
    // UInt32 Little Endian
    final byte value1 = (byte) (angle & 0xff);
    final byte value2 = 0; // (byte) (angle & 0xff00);
    final byte value3 = 0; // (byte) (angle & 0xff0000);
    final byte value4 = 0; // (byte) (angle & 0x7f000000);
    final byte dutyCyle = 75;
    return create(
        new byte[] { (byte) 0x81, port.id, 0x11, 0xb, value1, value2, value3, value4, dutyCyle, 0x64, 0x7f, 0x03 });
  }

  /**
   * @param port The port, where the motor is connected, i.e. A, B, C or D.
   * @param agle The absolute angle in degree.
   * @return The created byte array for the command.
   */
  public static byte[] setInitialAbsoluteAngle(final Port port, final int angle) {
    // UInt32 Little Endian
    final byte value1 = (byte) (angle & 0xff);
    final byte value2 = (byte) (angle & 0xff00);
    final byte value3 = (byte) (angle & 0xff0000);
    final byte value4 = 0; // (byte) (angle & 0x7f000000);
    return create(new byte[] { (byte) 0x81, port.id, 0x11, 0x51, 0x02, value1, value2, value3, value4 });
  }

  /**
   * @param port The port, where the motor is connected, i.e. A, B, C or D.
   * @param agle The absolute angle in degree.
   * @return The created byte array for the command.
   */
  public static byte[] setAbsoluteAngle(final Port port, final int angle) {
    // UInt32 Little Endian
    final byte value1 = (byte) (angle & 0xff);
    final byte value2 = (byte) (angle & 0xff00);
    final byte value3 = (byte) (angle & 0xff0000);
    final byte value4 = 0; // (byte) (angle & 0x7f000000);
    final byte dutyCyle = 25;
    return create(
        new byte[] { (byte) 0x81, port.id, 0x11, 0xd, value1, value2, value3, value4, dutyCyle, 0x64, 127, 0x03 });
  }

  public static byte[] activatePropertyUpdate(final PropertyType propertyType) {
    return propertyUpdate(propertyType, PropertyOperation.ENABLE_UPDATES_DOWNSTREAM);
  }

  public static byte[] requestPropertyUpdate(final PropertyType propertyType) {
    return propertyUpdate(propertyType, PropertyOperation.REQUEST_UPDATE_DOWNSTREAM);
  }

  public static byte[] deactivatePropertyUpdate(final PropertyType propertyType) {
    return propertyUpdate(propertyType, PropertyOperation.DISABLE_UPDATES_DOWNSTREAM);
  }

  public static byte[] propertyUpdate(final PropertyType propertyType, final PropertyOperation operation) {
    return create(new byte[] { 0x01, propertyType.value, operation.value });
  }

  public static List<byte[]> requestPortInformation(final Port port) {
    final List<byte[]> result = new ArrayList<>();
    // lock port information buffer
    result.add(create(new byte[] { 0x42, port.id, 0x02 }));
    // input format for absolute angle buffer
    result.add(create(new byte[] { 0x41, port.id, 0x03, 0x02, 0x00, 0x00, 0x00, 0x01 }));
    // input format for relative angle buffer
    result.add(create(new byte[] { 0x41, port.id, 0x02, 0x02, 0x00, 0x00, 0x00, 0x01 }));
    // set mode and data buffer
    result.add(create(new byte[] { 0x42, port.id, 0x01, 0x00, 0x30, 0x20 }));
    // unlock and enable buffer
    result.add(create(new byte[] { 0x42, port.id, 0x03 }));
    return result;
  }

  /**
   * @param bytes The real data bytes.
   * @return The real data bytes with two leading bytes for the data length.
   */
  private static byte[] create(final byte[] bytes) {
    final int size = bytes.length + 2;
    final byte[] result = new byte[bytes.length + 2];
    for (int i = 0; i < bytes.length; i++) {
      result[i + 2] = bytes[i];
    }
    result[0] = (byte) size;
    return result;
  }

}
