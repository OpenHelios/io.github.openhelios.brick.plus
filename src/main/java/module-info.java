@org.jspecify.annotations.NullMarked //
module io.github.openhelios.brick.plus {
  // static means only available at compile time
  requires static org.jspecify;

  requires org.slf4j;
  requires org.freedesktop.dbus;
  requires transitive bluez.dbus;

  exports io.github.openhelios.brick.plus;
  exports io.github.openhelios.brick.plus.liebherr42100;
  exports io.github.openhelios.brick.plus.minitruck;
  exports io.github.openhelios.brick.plus.volvo42114;

}
