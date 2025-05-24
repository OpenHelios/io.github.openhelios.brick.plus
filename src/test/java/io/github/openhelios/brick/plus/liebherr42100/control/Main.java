package io.github.openhelios.brick.plus.liebherr42100.control;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;

import io.github.openhelios.brick.plus.Motor;
import io.github.openhelios.brick.plus.TechnicHub;
import io.github.openhelios.brick.plus.liebherr42100.ILiebherr42100ConnectListener;
import io.github.openhelios.brick.plus.liebherr42100.ILiebherr42100PositionListener;
import io.github.openhelios.brick.plus.liebherr42100.Liebherr42100;
import io.github.openhelios.brick.plus.sdl.controller.ControllerAxis;
import io.github.openhelios.brick.plus.sdl.controller.ControllerButton;

public class Main extends ApplicationAdapter
    implements ILiebherr42100ConnectListener, ILiebherr42100PositionListener, ControllerListener {

  public static void main(final String[] args) throws InterruptedException {
    new Main().start();
  }

  private final Liebherr42100 liebherr42100 = new Liebherr42100(this);

  private MainState state = MainState.WAIT_FOR_LIEBHERR42100_AND_CONTROLLER;

  private MainState resumeState = MainState.MOVING;

  private void start() throws InterruptedException {
    final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Liebherr42100";
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
  public void onConnected(final Liebherr42100 liebherr) {
    synchronized (this) {
      System.out.println(liebherr);
      switch (state) {
        case WAIT_FOR_LIEBHERR42100_AND_CONTROLLER:
          state = MainState.WAIT_FOR_CONTROLLER;
          System.out.println("Wait for controller...");
          break;
        case WAIT_FOR_LIEBHERR42100:
          state = resumeState;
          System.out.println("Resume " + state);
          break;
        default:
          throw new IllegalStateException("expected WAIT_FOR_*LIEBHERR42100*, but was " + state);
      }
      liebherr.setListener(this);
    }
  }

  @Override
  public void onConnectFailed(final Liebherr42100 liebbherr, final Throwable e) {
    synchronized (this) {
      System.err.println(e.getMessage());
      state = MainState.EXIT;
    }
  }

  @Override
  public void connected(final Controller controller) {
    System.out.println("Connected: " + controller.getName());
    synchronized (this) {
      switch (state) {
        case WAIT_FOR_LIEBHERR42100_AND_CONTROLLER:
          state = MainState.WAIT_FOR_LIEBHERR42100;
          break;
        case WAIT_FOR_CONTROLLER:
          state = resumeState;
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
        case WAIT_FOR_LIEBHERR42100:
          state = MainState.WAIT_FOR_LIEBHERR42100_AND_CONTROLLER;
          break;
        case MOVING:
        case DIGGING:
          liebherr42100.stopMotors();
          resumeState = state;
          state = MainState.WAIT_FOR_CONTROLLER;
          break;
        case EXIT:
          return;
        default:
          break;
      }
    }
  }

  @Override
  public void onPositionChanged(final Liebherr42100 liebherr42100, final TechnicHub technicHub, final Motor motor) {
    System.out.println("hub " + technicHub.countMotors() + " a=" + motor.getAbsolutePosition() + "° r="
        + motor.getRelativePosition() + "°");
  }

  @Override
  public boolean buttonDown(final Controller controller, final int buttonCode) {
    if (ControllerButton.X.code == buttonCode) {
      System.out.println("X pressed");
      state = MainState.EXIT;
      liebherr42100.cancel();
    } else if (ControllerButton.SHARE.code == buttonCode) {
      System.out.println("SHARE pressed");
      if (MainState.MOVING.equals(state)) {
        state = MainState.DIGGING;
        System.out.println("Digging");
      } else if (MainState.DIGGING.equals(state)) {
        state = MainState.MOVING;
        System.out.println("Moving");
      }
    } else if (ControllerButton.L1.code == buttonCode) {
      liebherr42100.setShovelOpenerSpeed(100);
      System.out.println("L1");
    } else if (ControllerButton.R1.code == buttonCode) {
      liebherr42100.setShovelMoverSpeed(100);
      System.out.println("R1");
    } else {
      System.out.println("buttonDown: " + buttonCode);
    }
    return false;
  }

  @Override
  public boolean buttonUp(final Controller controller, final int buttonCode) {
    if (ControllerButton.L1.code == buttonCode) {
        liebherr42100.setShovelOpenerSpeed(0);
    } else if (ControllerButton.R1.code == buttonCode) {
        liebherr42100.setShovelMoverSpeed(0);
    }
    return false;
  }

  @Override
  public boolean axisMoved(final Controller controller, final int axisCode, final float value) {
    if (MainState.MOVING.equals(state)) {
      if (!doMainRotation(axisCode, value)) {
        doDriving(axisCode, value);
      }
    } else if (MainState.DIGGING.equals(state)) {
      if (!doMainRotation(axisCode, value)) {
        doDigging(axisCode, value);
      }
    }
    if (ControllerAxis.L2.code == axisCode) {
      liebherr42100.setShovelOpenerSpeed(-(int) value * 100);
      System.out.println("L2");
    } else if (ControllerAxis.R2.code == axisCode) {
      liebherr42100.setShovelMoverSpeed(-(int) value * 100);
      System.out.println("R2");
    }
    return false;
  }

  private void doDigging(final int axisCode, final float value) {
    if (ControllerAxis.R_Y.code == axisCode) {
      final int speed = Math.round(value * 100);
      liebherr42100.setArmXSpeed(speed);
    } else if (ControllerAxis.L_Y.code == axisCode) {
      final int speed = Math.round(-value * 100);
      liebherr42100.setArmZSpeed(speed);
    }
  }

  private boolean doMainRotation(final int axisCode, final float value) {
    if (ControllerAxis.R_X.code == axisCode) {
      final int speed = Math.round(-value * 100);
      liebherr42100.setRotationSpeed(speed);
      return true;
    }
    return false;
  }

  private boolean doDriving(final int axisCode, final float value) {
    if (ControllerAxis.L_Y.code == axisCode) {
      final int speed = Math.round(-value * 100);
      liebherr42100.setLeftSpeed(speed);
      return true;
    } else if (ControllerAxis.R_Y.code == axisCode) {
      final int speed = Math.round(value * 100);
      liebherr42100.setRightSpeed(speed);
      return true;
    }
    return false;
  }

}
