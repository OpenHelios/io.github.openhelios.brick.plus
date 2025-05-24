# BrickPlus

Unofficial Java Library for LEGO Control+ using DBus on Linux.

## Preconditions

* Bluetooth

## Compatible Library Versions

```
bluez                                   | Java | Linux Kernel
-------------------------------------------------------------
com.github.hypfvieh:dbus-java-bom:5.0.0 | 17   | 6.6.7
```

## Install and Compile

### Install

Install
* Gradle
* Eclipse
* JDK >= 17

### Generate Eclipse Project Files

* execute `gradle cleanEclipse eclipse`

### Import Eclipse Project

* start Eclipse
* File->Import...->General Project

## Linux

Precondition: start bluetooth service

```
$ sudo systemctl start bluetooth
```

## Start Volvo

1. turn the light on the cabine of the Volvo car
2. run io.github.openhelios.brick.plus.volvo42114.Main.java
