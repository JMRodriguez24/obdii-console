obdii-console
=============

Simple console to interact with obdii through serial port!

Run
===============

```sh
$ lein npm install
$ lein cljsbuild once
$ node run.js
prompt: serial port name:   COM2
prompt: baud rate:    9600
> 
```
Type commands at the prompt to query.

Commands
=================

* _open_: new connecton to serial port
* _close_: end connection to serial port
* _exit_ or _quit_:  exit application

Any PID from http://en.wikipedia.org/wiki/OBD-II_PIDs

For example:
```sh
# 010C is the PID for RPM
> 010C
010C

41 0C 31 BA

# ((A*256)+B)/4 = 3183
```
So the RPM at that instant was 3183.
