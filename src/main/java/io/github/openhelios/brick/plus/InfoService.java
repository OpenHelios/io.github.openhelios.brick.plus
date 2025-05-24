package io.github.openhelios.brick.plus;

import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattService;

public class InfoService extends AbstractService {

  public static final String UUID = "00001801-0000-1000-8000-00805f9b34fb";

  public InfoService(final BluetoothGattService gattService) {
    super(gattService, UUID);
  }

}
