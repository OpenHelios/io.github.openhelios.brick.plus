package io.github.openhelios.brick.plus;

import java.util.List;

import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattCharacteristic;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattService;

public abstract class AbstractService {

  public final BluetoothGattService gattService;

  public final BluetoothGattCharacteristic characteristic;

  public AbstractService(final BluetoothGattService gattService, final String uuid) {
    this.gattService = gattService;
    if (!uuid.equals(gattService.getUuid())) {
      throw new IllegalStateException("expected motor service" + uuid + ", but is " + gattService.getUuid());
    }
    final List<BluetoothGattCharacteristic> characteristics = gattService.getGattCharacteristics();
    if (1 != characteristics.size()) {
      throw new IllegalStateException(
          "expected exactly one characteristic, but found " + characteristics.size() + ": " + characteristics);
    }
    characteristic = characteristics.get(0);
    final List<String> flags = characteristic.getFlags();
    System.out.println("  " + characteristic.getDbusPath() + " " + flags);
    //		byte[] value;
    //		try {
    //			value = characteristic.readValue(Collections.emptyMap());
    //		} catch (final DBusException e) {
    //			throw new IllegalStateException(e);
    //		}
    //		System.out.println("  Value=" + InfoService.toHex(value));
  }

  public String getInfo() {
    return characteristic.getDbusPath();
  }

  public static String toHex(final byte[] data) {
    final StringBuilder sb = new StringBuilder(data.length * 2);
    for (final byte b : data) {
      sb.append(String.format("%02X", b));
    }
    return sb.toString();
  }

}
