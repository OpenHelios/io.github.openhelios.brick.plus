package io.github.openhelios.brick.plus.sdl.controller;

public enum ControllerButton {

  X(0),

  O(1),

  SQUARE(2),

  DELTA(3),

  SHARE(4),

  PS(5),

  OPTIONS(6),

  L1(9),

  R1(10),

  ;

  public final int code;

  ControllerButton(final int code) {
    this.code = code;
  }

}
