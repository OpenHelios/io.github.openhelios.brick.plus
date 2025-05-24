package io.github.openhelios.brick.plus.liebherr42100;

import io.github.openhelios.brick.plus.Motor;
import io.github.openhelios.brick.plus.TechnicHub;

public interface ILiebherr42100PositionListener {

  void onPositionChanged(Liebherr42100 liebherr42100, TechnicHub technicHub, Motor motor);

}
