# BrickPlus

Unofficial Java Library for LEGO Control+ using DBus on Linux.

It is inspired by libraries like https://github.com/sharpbrick/powered-up/

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
* Gradle or Maven
* Eclipse, IntelliJ or an other IDE of your choice
* JDK >= 21

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

## Start Liebherr 42100

1. turn light on of both bricks
2. run io.github.openhelios.brick.plus.liebherr42100.Main.java

## Start Volvo 42114

1. turn light on of brick in cabine of Volvo
2. run io.github.openhelios.brick.plus.volvo42114.Main.java
