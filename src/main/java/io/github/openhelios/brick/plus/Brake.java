package io.github.openhelios.brick.plus;

public enum Brake {

  BRAKE,

  FLOAT,

  HOLD,

  ;

  public final byte value;

  Brake() {
    value = (byte) ordinal();
  }

}
