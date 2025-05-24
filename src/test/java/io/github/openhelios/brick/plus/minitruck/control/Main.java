package io.github.openhelios.brick.plus.minitruck.control;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;

import io.github.openhelios.brick.plus.Motor;
import io.github.openhelios.brick.plus.TechnicHub;
import io.github.openhelios.brick.plus.minitruck.IMiniTruckConnectListener;
import io.github.openhelios.brick.plus.minitruck.IMinitruckPositionListener;
import io.github.openhelios.brick.plus.minitruck.MiniTruck;
import io.github.openhelios.brick.plus.sdl.controller.ControllerAxis;
import io.github.openhelios.brick.plus.sdl.controller.ControllerButton;

public class Main extends ApplicationAdapter
    implements IMiniTruckConnectListener, IMinitruckPositionListener, ControllerListener {

  public static void main(final String[] args) throws InterruptedException {
    new Main().start();
  }

  private final MiniTruck miniTruck = new MiniTruck(this);

  private MiniTruckState state = MiniTruckState.WAIT_FOR_MINI_TRUCK_AND_BLUETOOTH_CONTROLLER;

  private void start() throws InterruptedException {
    final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Volvo42114";
    config.width = 800;
    config.height = 480;
    new LwjglApplication(this, config);
  }

  @Override
  public void create() {
    final Controller controller = Controllers.getCurrent();
    if (null != controller) {
      connected(controller);
    }
    Controllers.addListener(this);
  }

  @Override
  public void onConnected(final MiniTruck liebherr) {
    synchronized (this) {
      System.out.println(liebherr);
      switch (state) {
        case WAIT_FOR_MINI_TRUCK_AND_BLUETOOTH_CONTROLLER:
          state = MiniTruckState.WAIT_FOR_BLUETOOTH_CONTROLLER;
          break;
        case WAIT_FOR_MINI_TRUCK:
          state = MiniTruckState.DRIVING;
          break;
        default:
          throw new IllegalStateException("expected WAIT_FOR_*Volvo42114*, but was " + state);
      }
      liebherr.setListener(this);
    }
  }

  @Override
  public void onConnectFailed(final MiniTruck volvo42114, final Throwable e) {
    synchronized (this) {
      System.err.println(e.getMessage());
      state = MiniTruckState.EXIT;
    }
  }

  @Override
  public void connected(final Controller controller) {
    System.out.println("Connected: " + controller.getName());
    synchronized (this) {
      switch (state) {
        case WAIT_FOR_MINI_TRUCK_AND_BLUETOOTH_CONTROLLER:
          state = MiniTruckState.WAIT_FOR_MINI_TRUCK;
          break;
        case WAIT_FOR_BLUETOOTH_CONTROLLER:
          state = MiniTruckState.DRIVING;
          break;
        default:
          throw new IllegalStateException("expected WAIT_FOR_*, but was " + state);
      }
    }
  }

  @Override
  public void disconnected(final Controller controller) {
    synchronized (this) {
      System.out.println("Disconnected " + controller);
      switch (state) {
        case WAIT_FOR_MINI_TRUCK:
          state = MiniTruckState.WAIT_FOR_MINI_TRUCK_AND_BLUETOOTH_CONTROLLER;
          break;
        case DRIVING:
          miniTruck.stopMotors();
          state = MiniTruckState.WAIT_FOR_BLUETOOTH_CONTROLLER;
          break;
        case EXIT:
          return;
        default:
          break;
      }
    }
  }

  @Override
  public void onPositionChanged(final MiniTruck Volvo42114, final TechnicHub technicHub, final Motor motor) {
    System.out.println("hub " + technicHub.countMotors() + " a=" + motor.getAbsolutePosition() + "° r="
        + motor.getRelativePosition() + "°");
  }

  @Override
  public boolean buttonDown(final Controller controller, final int buttonCode) {
    if (ControllerButton.X.code == buttonCode) {
      System.out.println("X pressed");
      state = MiniTruckState.EXIT;
      miniTruck.cancel();
      miniTruck.close();
    } else {
      System.out.println("buttonDown: " + buttonCode);
    }
    return false;
  }

  @Override
  public boolean buttonUp(final Controller controller, final int buttonCode) {
    return false;
  }

  @Override
  public boolean axisMoved(final Controller controller, final int axisCode, final float value) {
    if (MiniTruckState.DRIVING.equals(state)) {
      final int intValue = Math.round(value * 100);
      if (ControllerAxis.R_X.code == axisCode) {
        miniTruck.setStearingAngle(intValue);
        return true;
      } else if (ControllerAxis.L_Y.code == axisCode) {
        miniTruck.setDrivingSpeed(intValue);
        return true;
      }
    }
    return false;
  }

}
