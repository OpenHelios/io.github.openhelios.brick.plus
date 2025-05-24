package io.github.openhelios.brick.plus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.freedesktop.dbus.exceptions.DBusException;

import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattCharacteristic;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattService;

public class TechnicHub implements AutoCloseable {

  private static final String NAME = "Technic Hub";

  public final BluetoothDevice device;

  public final InfoService infoService;

  public final MotorService motorService;

  private final Motor[] motors = new Motor[4];

  private byte batteryInPercent = -1;

  private ITechnicHubPositionChangedListener listener;

  public static boolean isTechnicHub(final BluetoothDevice device) {
    return TechnicHub.NAME.equals(device.getName());
  }

  public TechnicHub(final BluetoothDevice device) {
    this.device = device;
    try {
      System.out.println("Connect with " + device.getDbusPath() + "...");
      device.connect();
      System.out.println(" Get Gatt services...");
      List<BluetoothGattService> services = device.getGattServices();
      System.out.println(" Services: " + services.size());
      while (services.isEmpty()) {
        try {
          Thread.sleep(100);
        } catch (final InterruptedException e) {
          // ignore
        }
        services = device.getGattServices();
        System.out.println(" Services: " + services.size());
      }
      InfoService tmpInfoService = null;
      MotorService tmpMotorService = null;
      for (final BluetoothGattService service : services) {
        final String uuid = service.getUuid();
        if (InfoService.UUID.equals(uuid)) {
          tmpInfoService = new InfoService(service);
        } else if (MotorService.UUID.equals(uuid)) {
          tmpMotorService = new MotorService(service);
        } else {
          System.out.println("Warning: Expected info or motor service, but found " + uuid);
        }
      }
      if (null == tmpMotorService) {
        throw new IllegalStateException("expected motor service, but found only " + services);
      }
      infoService = tmpInfoService;
      motorService = tmpMotorService;
    } catch (final RuntimeException e) {
      close();
      throw e;
    }
  }

  /**
   * Activate property update to get battery and motor position changed information.
   */
  public void activatePropertyUpdate() {
    final BluetoothGattCharacteristic characteristic = motorService.characteristic;
    try {
      characteristic.startNotify();
      characteristic.writeValue(Command.activatePropertyUpdate(PropertyType.BATTERY_VOLTAGE), Collections.emptyMap());
    } catch (final DBusException e) {
      throw new IllegalStateException(e);
    }
  }

  public String getInfo() {
    return device.getDbusPath();
  }

  public String getExtendedInfo() {
    final StringBuffer sb = new StringBuffer();
    sb.append(getInfo());
    appendExtendedInfo(sb);
    return sb.toString();
  }

  private void appendExtendedInfo(final StringBuffer sb) {
    sb.append(' ').append(device.getUuids()[0]).append(' ').append(device.getName());
    sb.append("\n  Motors: " + getMotorTypes());
    sb.append("\n  Battery: ").append(batteryInPercent).append('%');
  }

  public byte getBatteryInPercent() {
    return batteryInPercent;
  }

  public List<MotorType> getMotorTypes() {
    return Arrays.stream(motors) //
        .filter(Objects::nonNull) //
        .map(Motor::getType) //
        .collect(Collectors.toList());
  }

  public int countMotors() {
    int result = 0;
    for (final Motor motor : motors) {
      if (null != motor) {
        result++;
      }
    }
    return result;
  }

  public Motor motorA() {
    return motors[Port.A.id];
  }

  public Motor motorB() {
    return motors[Port.B.id];
  }

  public Motor motorC() {
    return motors[Port.C.id];
  }

  public Motor motorD() {
    return motors[Port.D.id];
  }

  public void stopMotors() {
    synchronized (motors) {
      for (final Motor motor : motors) {
        if (null != motor) {
          motor.setSpeed(0);
        }
      }
    }
  }

  protected void onPropertyChanged(final List<Byte> data) {
    if (2 < data.size()) {
      final byte messageType = data.get(2);
      if (MessageType.PORT_VALUE_COMBINED_MODE.value == messageType) {
        // 0c 00 46 00 00 03 a1 ff 00 00 00 00
        final byte portId = data.get(3);
        final Motor motor = motors[portId];
        if (null != motor) {
          motor.onMotorPortValueCombined(data);
        }
      } else if (MessageType.PORT_CONNECTION_INFO.value == messageType) {
        final byte portId = data.get(3);
        if (0 <= portId && portId <= 3) {
          if (0 == data.get(4)) {
            motors[portId] = null; // disconnected
          } else {
            motors[portId] = new Motor(Port.valueOf(portId), MotorType.valueOf(data.get(5)), motorService);
          }
        }
      } else if (MessageType.BATTERY_INFO.value == messageType) {
        batteryInPercent = data.get(5);
        System.out.println("Battery " + batteryInPercent + '%');
      }
    }
  }

  /**
   * @param listener The motor listener.
   */
  public void setListener(final ITechnicHubPositionChangedListener listener) {
    this.listener = listener;
    final BluetoothGattCharacteristic characteristic = motorService.characteristic;
    for (final Motor motor : motors) {
      if (null != motor) {
        motor.setListener(this::onPositionChanged);
        for (final byte[] cmd : Command.requestPortInformation(motor.port)) {
          try {
            characteristic.writeValue(cmd, Collections.emptyMap());
          } catch (final DBusException e) {
            throw new IllegalStateException(e);
          }
        }
      }
    }
  }

  private void onPositionChanged(final Motor motor) {
    if (null != listener) {
      listener.onPositionChanged(this, motor);
    }
  }

  @Override
  public void close() {
    boolean hasMotor = false;
    for (int i = 0; i < motors.length; i++) {
      final Motor motor = motors[i];
      if (null != motor) {
        motor.setSpeed(0);
        motors[i] = null;
        hasMotor = true;
      }
    }
    if (hasMotor) {
      device.disconnect();
    }
  }

  @Override
  public String toString() {
    return getExtendedInfo();
  }

}
