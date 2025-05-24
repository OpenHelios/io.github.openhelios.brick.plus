package io.github.openhelios.brick.plus;

/**
 * Listener to be notified for a changed motor position on a Technic Hub.
 */
public interface ITechnicHubPositionChangedListener {

  /**
   * @param technicHub The Technic Hub.
   * @param motor The motor with the changed position.
   * @see Motor#getAbsolutePosition()
   * @see Motor#getRelativePosition()
   */
  void onPositionChanged(TechnicHub technicHub, Motor motor);

}
