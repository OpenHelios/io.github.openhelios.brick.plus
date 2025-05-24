package io.github.openhelios.brick.plus.volvo42114.control;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;

import io.github.openhelios.brick.plus.Motor;
import io.github.openhelios.brick.plus.TechnicHub;
import io.github.openhelios.brick.plus.sdl.controller.ControllerAxis;
import io.github.openhelios.brick.plus.sdl.controller.ControllerButton;
import io.github.openhelios.brick.plus.volvo42114.IVolvo42114ConnectListener;
import io.github.openhelios.brick.plus.volvo42114.IVolvo42114PositionListener;
import io.github.openhelios.brick.plus.volvo42114.Volvo42114;

public class Main extends ApplicationAdapter
    implements IVolvo42114ConnectListener, IVolvo42114PositionListener, ControllerListener {

  public static void main(final String[] args) throws InterruptedException {
    new Main().start();
  }

  private final Volvo42114 volvo42114 = new Volvo42114(this);

  private Volvo42114State state = Volvo42114State.WAIT_FOR_VOLVO42114_AND_CONTROLLER;

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
  public void onConnected(final Volvo42114 liebherr) {
    synchronized (this) {
      System.out.println(liebherr);
      switch (state) {
        case WAIT_FOR_VOLVO42114_AND_CONTROLLER:
          state = Volvo42114State.WAIT_FOR_CONTROLLER;
          break;
        case WAIT_FOR_VOLVO42114:
          state = Volvo42114State.DRIVING;
          break;
        default:
          throw new IllegalStateException("expected WAIT_FOR_*Volvo42114*, but was " + state);
      }
      liebherr.setListener(this);
    }
  }

  @Override
  public void onConnectFailed(final Volvo42114 volvo42114, final Throwable e) {
    synchronized (this) {
      System.err.println(e.getMessage());
      state = Volvo42114State.EXIT;
    }
  }

  @Override
  public void connected(final Controller controller) {
    System.out.println("Connected: " + controller.getName());
    synchronized (this) {
      switch (state) {
        case WAIT_FOR_VOLVO42114_AND_CONTROLLER:
          state = Volvo42114State.WAIT_FOR_VOLVO42114;
          break;
        case WAIT_FOR_CONTROLLER:
          state = Volvo42114State.DRIVING;
          break;
        default:
          throw new IllegalStateException("expected WAIT_FOR_*CONTROLLER*, but was " + state);
      }
    }
  }

  @Override
  public void disconnected(final Controller controller) {
    synchronized (this) {
      System.out.println("Disconnected " + controller);
      switch (state) {
        case WAIT_FOR_VOLVO42114:
          state = Volvo42114State.WAIT_FOR_VOLVO42114_AND_CONTROLLER;
          break;
        case DRIVING:
          volvo42114.stopMotors();
          state = Volvo42114State.WAIT_FOR_CONTROLLER;
          break;
        case EXIT:
          return;
        default:
          break;
      }
    }
  }

  @Override
  public void onPositionChanged(final Volvo42114 Volvo42114, final TechnicHub technicHub, final Motor motor) {
    System.out.println("hub " + technicHub.countMotors() + " a=" + motor.getAbsolutePosition() + "° r="
        + motor.getRelativePosition() + "°");
  }

  @Override
  public boolean buttonDown(final Controller controller, final int buttonCode) {
    if (ControllerButton.X.code == buttonCode) {
      System.out.println("X pressed");
      state = Volvo42114State.EXIT;
      volvo42114.cancel();
      volvo42114.close();
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
    if (Volvo42114State.DRIVING.equals(state)) {
      final int speed = Math.round(value * 100);
      if (ControllerAxis.R_X.code == axisCode) {
        volvo42114.setStearingSpeed(speed);
        return true;
      } else if (ControllerAxis.L_Y.code == axisCode) {
        volvo42114.setDrivingOrDumpingSpeed(speed);
        return true;
      } else if (ControllerAxis.R_Y.code == axisCode) {
        volvo42114.setGearSpeed(speed);
        return true;
      }
    }
    return false;
  }

}
