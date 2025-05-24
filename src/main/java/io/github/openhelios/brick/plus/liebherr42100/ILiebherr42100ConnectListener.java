package io.github.openhelios.brick.plus.liebherr42100;

public interface ILiebherr42100ConnectListener {

  void onConnected(Liebherr42100 liebbherr);

  void onConnectFailed(Liebherr42100 liebbherr, Throwable e);

}
