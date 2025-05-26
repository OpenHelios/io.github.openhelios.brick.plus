package io.github.openhelios.brick.plus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties.PropertiesChanged;
import org.freedesktop.dbus.types.Variant;

import com.github.hypfvieh.bluetooth.DeviceManager;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothAdapter;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice;

public class ControlPlus implements AutoCloseable {

  private static final DeviceManager DEVICE_MANAGER;

  static {
    try {
      DEVICE_MANAGER = DeviceManager.createInstance(false);
    } catch (final DBusException e) {
      throw new IllegalStateException(e);
    }
  }

  private int tryMax = 60;

  private BluetoothAdapter controller;

  private final List<TechnicHub> hubs = new ArrayList<>();

  private static BluetoothAdapter getAdapter() throws FailedConnectionException {
    final List<BluetoothAdapter> adapters = DEVICE_MANAGER.getAdapters();
    if (adapters.isEmpty()) {
      DEVICE_MANAGER.closeConnection();
      throw new FailedConnectionException(
          "No bluetooth adapter found - try 'systemctl start bluetooth' and verify bluetooth adapter is plugged in");
    }
    return adapters.get(0);
  }

  public static List<BluetoothDevice> findTechnicHubs() throws FailedConnectionException {
    getAdapter();
    return findTechnicHubs(DEVICE_MANAGER.scanForBluetoothDevices(500));
  }

  public List<TechnicHub> getHubs() {
    synchronized (hubs) {
      return new ArrayList<>(hubs);
    }
  }

  public void waitForTechnicHubs(final int expectedHubs) throws FailedConnectionException {
    try {
      controller = getAdapter();
      int tryCount = 0;
      List<BluetoothDevice> hubs = findTechnicHubs(DEVICE_MANAGER.scanForBluetoothDevices(500));
      while (tryMax > tryCount && hubs.size() < expectedHubs) {
        if (!hubs.isEmpty()) {
          System.out.println("hub " + hubs.get(0).getDbusPath());
        }
        System.out.println("Waiting for Technic Hubs (" + hubs.size() + "/" + expectedHubs + ")...");
        hubs = findTechnicHubs(DEVICE_MANAGER.scanForBluetoothDevices(500));
        tryCount++;
      }
      if (0 > tryMax) {
        throw new FailedConnectionException("cancelled connecting");
      }
      if (tryMax <= tryCount) {
        throw new FailedConnectionException(
            "expected " + expectedHubs + " Technic Hubs, but only " + hubs.size() + " found");
      }
      final List<TechnicHub> tmpHubs = new ArrayList<>();
      for (final BluetoothDevice hub : hubs) {
        tmpHubs.add(new TechnicHub(hub));
      }
      synchronized (hubs) {
        addSigHandler(controller.getDbusConnection());
        this.hubs.clear();
        this.hubs.addAll(tmpHubs);
        for (final TechnicHub hub : this.hubs) {
          hub.activatePropertyUpdate();
        }
      }
    } catch (final FailedConnectionException e) {
      close();
      throw e;
    } catch (final RuntimeException e) {
      close();
      throw new FailedConnectionException(e.getMessage());
    }
  }

  private void addSigHandler(final DBusConnection connection) {
    try {
      connection.addSigHandler(PropertiesChanged.class, this::onPropertiesChanged);
    } catch (final DBusException e) {
      throw new IllegalStateException(e);
    }
  }

  private static List<BluetoothDevice> findTechnicHubs(final List<BluetoothDevice> devices) {
    final List<BluetoothDevice> foundHubs = new ArrayList<>();
    for (final BluetoothDevice device : devices) {
      if (TechnicHub.isTechnicHub(device)) {
        foundHubs.add(device);
      }
    }
    //    final List<BluetoothDevice> foundHubs = devices.stream() //
    //        .filter(TechnicHub::isTechnicHub) //
    //        .collect(Collectors.toList());
    return Collections.unmodifiableList(foundHubs);
  }

  public String getInfo() {
    if (null != controller) {
      return controller.getAddress() + ' ' + controller.getDbusPath();
    }
    return null;
  }

  private void onPropertiesChanged(final PropertiesChanged s) {
    final Variant<?> value = s.getPropertiesChanged().get("Value");
    if (null != value) {
      @SuppressWarnings("unchecked")
      final List<Byte> data = (List<Byte>) value.getValue();
      final String path = s.getPath();
      //System.out.println( //
      //    System.currentTimeMillis() + " handler: " + AbstractService.toHex(data) + " from " + path);
      synchronized (hubs) {
        for (final TechnicHub hub : hubs) {
          if (path.startsWith(hub.device.getDbusPath())) {
            hub.onPropertyChanged(data);
            break;
          }
        }
      }
    } else {
      final Map<String, Variant<?>> changedProperties = s.getPropertiesChanged();
      System.out.println(System.currentTimeMillis() + " handler: " + s.getFlags() + " " + changedProperties);
    }
  }

  public void cancel() {
    tryMax = -1;
  }

  public void stopMotors() {
    synchronized (hubs) {
      for (final TechnicHub technicHub : hubs) {
        technicHub.stopMotors();
      }
    }
  }

  @Override
  public void close() {
    tryMax = 0;
    if (null != controller) {
      System.out.println("PowerUp.close()");
      synchronized (hubs) {
        for (final TechnicHub hub : hubs) {
          hub.close();
        }
      }
      final DBusConnection connection = controller.getDbusConnection();
      try {
        connection.close();
      } catch (final IOException e) {
        // ignore
      } finally {
        controller = null;
      }
    }
  }

}
