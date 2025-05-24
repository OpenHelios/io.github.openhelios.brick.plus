package io.github.openhelios.brick.plus;

import java.util.Collections;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;

import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattService;

public class MotorService extends AbstractService {

  public static final String UUID = "00001623-1212-efde-1623-785feabcd123";

  public MotorService(final BluetoothGattService gattService) {
    super(gattService, UUID);
  }

  /**
   * @param port The port, where the motor is connected, i.e. A, B, C or D.
   * @param speed The speed between -100 and 100.
   */
  public void setSpeed(final Port port, final int speed) {
    try {
      characteristic.writeValue(Command.motorSpeed(port, speed), Collections.emptyMap());
    } catch (final DBusException | DBusExecutionException e) {
      throw new IllegalStateException("error setting speed " + speed + " at port " + port + " for " + getInfo(), e);
    }
  }

  /**
   * @param port The port, where the motor is connected, i.e. A, B, C or D.
   * @param speed The speed between -100 and 100.
   */
  public void moveByAngle(final Port port, final int angle) {
    try {
      characteristic.writeValue(Command.motorMoveByAngle(port, angle), Collections.emptyMap());
    } catch (final DBusException | DBusExecutionException e) {
      throw new IllegalStateException("error move motor by angle " + angle + " at port " + port + " for " + getInfo(),
          e);
    }
  }

  /**
   * @param port The port, where the motor is connected, i.e. A, B, C or D.
   * @param speed The speed between -100 and 100.
   */
  public void setInitialAbsoluteAngle(final Port port, final int angle) {
    try {
      characteristic.writeValue(Command.setInitialAbsoluteAngle(port, angle), Collections.emptyMap());
    } catch (final DBusException | DBusExecutionException e) {
      throw new IllegalStateException("error move motor by angle " + angle + " at port " + port + " for " + getInfo(),
          e);
    }
  }

  /**
   * @param port The port, where the motor is connected, i.e. A, B, C or D.
   * @param speed The speed between -100 and 100.
   */
  public void setAbsoluteAngle(final Port port, final int angle) {
    try {
      characteristic.writeValue(Command.setAbsoluteAngle(port, angle), Collections.emptyMap());
    } catch (final DBusException | DBusExecutionException e) {
      throw new IllegalStateException("error move motor by angle " + angle + " at port " + port + " for " + getInfo(),
          e);
    }
  }

}
