package io.github.openhelios.brick.plus.volvo42114;

import java.util.List;

import io.github.openhelios.brick.plus.ControlPlus;
import io.github.openhelios.brick.plus.FailedConnectionException;
import io.github.openhelios.brick.plus.Motor;
import io.github.openhelios.brick.plus.TechnicHub;

public class Volvo42114 implements AutoCloseable {

  private final IVolvo42114ConnectListener connectlistener;

  private final ControlPlus powerUp = new ControlPlus();

  private TechnicHub hubMain;

  private IVolvo42114PositionListener positionListener;

  /**
   * Synchronous constructor.
   *
   * @throws FailedConnectionException
   */
  public Volvo42114() throws FailedConnectionException {
    this.connectlistener = null;
    connectTechnicHubsInternal();
  }

  /**
   * Asynchronous constructor.
   *
   * @param listener The listener.
   */
  public Volvo42114(final IVolvo42114ConnectListener listener) {
    this.connectlistener = listener;
    new Thread(this::connectTechnicHubs).start();
  }

  private void connectTechnicHubs() {
    try {
      connectTechnicHubsInternal();
    } catch (final FailedConnectionException | RuntimeException e) {
      // ignore here
    }
  }

  private void connectTechnicHubsInternal() throws FailedConnectionException {
    try {
      powerUp.waitForTechnicHubs(1);
      final List<TechnicHub> hubs = powerUp.getHubs();
      for (final TechnicHub hub : hubs) {
        hub.setListener(this::onPositionChanged);
        if (3 == hub.countMotors()) {
          hubMain = hub;
        }
      }
      if (null == hubMain) {
        throw new FailedConnectionException("expected Technic Hub with 3 motors, but found only " + hubs);
      }
      if (null != connectlistener) {
        connectlistener.onConnected(this);
      }
    } catch (final Throwable e) {
      close();
      if (null != connectlistener) {
        connectlistener.onConnectFailed(this, e);
      }
      throw e;
    }
  }

  public void setListener(final IVolvo42114PositionListener moveListener) {
    this.positionListener = moveListener;
    if (null != hubMain) {
      hubMain.setListener(this::onPositionChanged);
    }
  }

  public void stopMotors() {
    powerUp.stopMotors();
  }

  public void cancel() {
    powerUp.cancel();
  }

  @Override
  public void close() {
    powerUp.close();
  }

  public void setDrivingOrDumpingSpeed(final int speed) {
    if (null != hubMain) {
      hubMain.motorA().setSpeed(speed);
    }
  }

  public void setGearSpeed(final int speed) {
    if (null != hubMain) {
      hubMain.motorB().setSpeed(-speed);
    }
  }

  public void setStearingSpeed(final int speed) {
    if (null != hubMain) {
      hubMain.motorD().setSpeed(speed);
    }
  }

  private void onPositionChanged(final TechnicHub technicHub, final Motor motor) {
    if (null != positionListener) {
      positionListener.onPositionChanged(this, technicHub, motor);
    }
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    if (null != hubMain) {
      sb.append(hubMain.getExtendedInfo()).append('\n');
    }
    return sb.toString();
  }

}
