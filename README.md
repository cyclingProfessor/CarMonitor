CarMonitor
==========

# _Brief_

_Description: This is the Android Monitor and Control program for the Bluno based self-driving car.  Of course the BLE communicator is improved/updated.  In particular it uses Post API version 23 security.
The original BLE code and service code is forked from th DFRobot github repository for the Bluno M3.  This is referenced in the appropriate modules.  However this code (reportedly from DFRobot) is actually copied from the Android central website without attribution!

## BLE Details

*DF Robot Service:*
Id: 0000dfb0-0000-1000-8000-00805f9b34fb

*DF Robot Characteristics:*
Name: Serial Port
Id: 0000dfb1-0000-1000-8000-00805f9b34fb
  
Name: Command
Id: 0000dfb2-0000-1000-8000-00805f9b34fb

*DF Robot Descriptors:*
(**same for both characteristics**)
Name: Read
Name: WriteWithoutResponse
Name: Write
Name: Notify

## Project Setup

## License

_GPLv3_

## To DO
Fix the code so that it is a bit (a lot!) prettier.
Move the Scan function to a Menu/Status with auto connect (first Bluno M3).