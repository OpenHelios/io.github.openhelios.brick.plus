package io.github.openhelios.brick.plus.liebherr42100;

import java.util.List;

import io.github.openhelios.brick.plus.ControlPlus;
import io.github.openhelios.brick.plus.FailedConnectionException;
import io.github.openhelios.brick.plus.Motor;
import io.github.openhelios.brick.plus.TechnicHub;

public class Liebherr42100 implements AutoCloseable {

  private final ILiebherr42100ConnectListener connectlistener;

  private final ControlPlus powerUp = new ControlPlus();

  private TechnicHub hubMain;

  private TechnicHub hubArm;

  private ILiebherr42100PositionListener positionListener;

  /**
   * Synchronous constructor.
   *
   * @throws FailedConnectionException
   */
  public Liebherr42100() throws FailedConnectionException {
    this.connectlistener = null;
    connectTechnicHubsInternal();
  }

  /**
   * Asynchronous constructor.
   *
   * @param listener The listener.
   */
  public Liebherr42100(final ILiebherr42100ConnectListener listener) {
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
      powerUp.waitForTechnicHubs(2);
      final List<TechnicHub> hubs = powerUp.getHubs();
      for (final TechnicHub hub : hubs) {
        hub.setListener(this::onPositionChanged);
        if (3 == hub.countMotors()) {
          hubMain = hub;
        }
        if (4 == hub.countMotors()) {
          hubArm = hub;
        }
      }
      if (null == hubMain || null == hubArm) {
        throw new FailedConnectionException("expected two Technic Hubs with 3 and 4 motors, but found only " + hubs);
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

  public void setListener(final ILiebherr42100PositionListener moveListener) {
    this.positionListener = moveListener;
    if (null != hubMain) {
      hubMain.setListener(this::onPositionChanged);
    }
    if (null != hubArm) {
      hubArm.setListener(this::onPositionChanged);
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

  public void setLeftSpeed(final int speed) {
    if (null != hubMain) {
      hubMain.motorA().setSpeed(speed);
    }
  }

  public void setRightSpeed(final int speed) {
    if (null != hubMain) {
      hubMain.motorB().setSpeed(speed);
    }
  }

  public void setRotationSpeed(final int speed) {
    if (null != hubMain) {
      hubMain.motorD().setSpeed(speed);
    }
  }

  public void setArmZSpeed(final int speed) {
    if (null != hubArm) {
      hubArm.motorA().setSpeed(speed);
    }
  }

  public void setArmXSpeed(final int speed) {
    if (null != hubArm) {
      hubArm.motorB().setSpeed(speed);
    }
  }

  public void setShovelMoverSpeed(final int speed) {
    if (null != hubArm) {
      hubArm.motorC().setSpeed(speed);
    }
  }

  public void setShovelOpenerSpeed(final int speed) {
    if (null != hubArm) {
      hubArm.motorD().setSpeed(speed);
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
    if (null != hubArm) {
      sb.append(hubArm.getExtendedInfo());
    }
    return sb.toString();
  }

}
