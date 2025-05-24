package io.github.openhelios.brick.plus;

public enum PropertyOperation {

  SET_DOWNSTREAM,

  ENABLE_UPDATES_DOWNSTREAM,

  DISABLE_UPDATES_DOWNSTREAM,

  RESET_DOWNSTREAM,

  REQUEST_UPDATE_DOWNSTREAM,

  UPDATE_UPSTREAM,

  ;

  public final byte value;

  PropertyOperation() {
    this.value = (byte) (ordinal() + 1);
  }

}
