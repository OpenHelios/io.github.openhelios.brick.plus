package io.github.openhelios.brick.plus;

/**
 * Listener to be notified for a changed motor position.
 */
public interface IMotorListener {

  /**
   * @param motor The motor with the changed position.
   * @see Motor#getAbsolutePosition()
   */
  void onPositionChanged(Motor motor);

}
