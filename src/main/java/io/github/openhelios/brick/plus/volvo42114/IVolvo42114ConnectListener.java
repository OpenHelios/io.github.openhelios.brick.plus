package io.github.openhelios.brick.plus.volvo42114;

public interface IVolvo42114ConnectListener {

  void onConnected(Volvo42114 volvo42114);

  void onConnectFailed(Volvo42114 volvo42114, Throwable e);

}
