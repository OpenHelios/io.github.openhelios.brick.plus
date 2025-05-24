package io.github.openhelios.brick.plus.sdl.controller;

public enum ControllerAxis {

  L_X(0),

  L_Y(1),

  R_X(2),

  R_Y(3),

  L2(4),

  R2(5),

  ;

  public final int code;

  ControllerAxis(final int code) {
    this.code = code;
  }

}
