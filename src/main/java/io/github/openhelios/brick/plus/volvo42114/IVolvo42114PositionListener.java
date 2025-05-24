package io.github.openhelios.brick.plus.volvo42114;

import io.github.openhelios.brick.plus.Motor;
import io.github.openhelios.brick.plus.TechnicHub;

public interface IVolvo42114PositionListener {

  void onPositionChanged(Volvo42114 volvo42114, TechnicHub technicHub, Motor motor);

}
