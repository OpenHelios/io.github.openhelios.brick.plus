package io.github.openhelios.brick.plus.minitruck;

import java.util.List;

import io.github.openhelios.brick.plus.ControlPlus;
import io.github.openhelios.brick.plus.FailedConnectionException;
import io.github.openhelios.brick.plus.Motor;
import io.github.openhelios.brick.plus.TechnicHub;

public class MiniTruck implements AutoCloseable {

  private static final int INITIAL_ABSOLUTE_POSITION_IN_DEGREE = 180;

  private final IMiniTruckConnectListener connectlistener;

  private final ControlPlus powerUp = new ControlPlus();

  private TechnicHub hubMain;

  private IMinitruckPositionListener positionListener;

  // current angle in degree
  private final int currentAngle = 0;

  /**
   * Synchronous constructor.
   *
   * @throws FailedConnectionException
   */
  public MiniTruck() throws FailedConnectionException {
    this.connectlistener = null;
    connectTechnicHubsInternal();
  }

  /**
   * Asynchronous constructor.
   *
   * @param listener The listener.
   */
  public MiniTruck(final IMiniTruckConnectListener listener) {
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
        if (2 == hub.countMotors()) {
          hubMain = hub;
        }
      }
      if (null == hubMain) {
        throw new FailedConnectionException("expected Technic Hub with 2 motors, but found only " + hubs);
      }
      if (null != connectlistener) {
        connectlistener.onConnected(this);
      }
      hubMain.motorB().setInitialAbsoluteAngle(INITIAL_ABSOLUTE_POSITION_IN_DEGREE);
    } catch (final Throwable e) {
      close();
      if (null != connectlistener) {
        connectlistener.onConnectFailed(this, e);
      }
      throw e;
    }
  }

  public void setListener(final IMinitruckPositionListener moveListener) {
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

  public void setDrivingSpeed(final int speed) {
    if (null != hubMain) {
      hubMain.motorA().setSpeed(-speed);
    }
  }

  /**
   * @param angle The stearing angle in degree.
   */
  public void setStearingAngle(final int angle) {
    if (null != hubMain) {
      hubMain.motorB().setSpeed((int) (angle * .2));
      //      System.out.println("angle=" + angle);
      //      final int newAngle = angle + INITIAL_ABSOLUTE_POSITION_IN_DEGREE;
      //      if (currentAngle != newAngle) {
      //        hubMain.motorB().setAbsoluteAngle(newAngle);
      //        currentAngle = newAngle;
      //      }
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
