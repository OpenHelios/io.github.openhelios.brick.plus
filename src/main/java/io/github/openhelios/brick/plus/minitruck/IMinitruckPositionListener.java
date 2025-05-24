package io.github.openhelios.brick.plus.minitruck;

import io.github.openhelios.brick.plus.Motor;
import io.github.openhelios.brick.plus.TechnicHub;

public interface IMinitruckPositionListener {

  void onPositionChanged(MiniTruck miniTruck, TechnicHub technicHub, Motor motor);

}
