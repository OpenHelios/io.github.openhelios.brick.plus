package io.github.openhelios.brick.plus;

public enum Port {

  A, // left top

  B, // right top

  C, // left bottom

  D, // right bottom

  ;

  public final byte id;

  Port() {
    id = (byte) ordinal();
  }

  public static Port valueOf(final byte id) {
    return values()[id];
  }

}
