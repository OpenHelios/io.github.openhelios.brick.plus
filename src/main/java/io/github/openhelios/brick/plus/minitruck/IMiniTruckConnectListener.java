package io.github.openhelios.brick.plus.minitruck;

public interface IMiniTruckConnectListener {

  void onConnected(MiniTruck miniTruck);

  void onConnectFailed(MiniTruck miniTruck, Throwable e);

}
