#ifndef STATEMACHINEHANDLER_H
#define STATEMACHINEHANDLER_H
#include <arduino.h>
#include "usbcommunicator.h"
#include "protocolhandler.h"
#include "Planner.h"
#include "Driver.h"
#include "SensorHandler.h"

class StateMachineHandler{
    private:
      enum mainStates         : int {s_receive, s_execute, s_send, s_check} _mainStates;
      enum executeStates      : int {s_planner = 0x04, s_arduino = 0x00} _executeStates;
      enum subExecuteStates   : int {s_event = 0x10, s_driveState = 0x15, s_driveCommand = 0x16};
      enum eventStates        : int {s_initialized = 0x50};
      enum driveCommandStates : int {s_automatic = 0x22, s_manual = 0x23};

      enum driveStates        : int {up_pressed = 0x30, right_pressed = 0x31, down_pressed = 0x32, left_pressed = 0x33,
                                     up_released = 0x40, right_released = 0x41, down_released = 0x42, left_released = 0x43
                                     };

      Protocolhandler protoHandlerIn;
      Protocolhandler protoHandlerOut;
      
      bool _gotMessage  = false;
      bool _isAwake     = false;

      String _message        = "";
      uint32_t _sendDelay    = 1000;
      uint32_t _lastSendTime = 0;

      Usbcommunicator * _arduinoUSB;
      SensorHandler * _sensorHandler;
      Driver * _driver;
      Planner * _planner;

      
    public:
        StateMachineHandler(Usbcommunicator& arduinoUSB, SensorHandler& sensorHandler,
                            Driver& driver, Planner& planner){
          _arduinoUSB = &arduinoUSB;
          _sensorHandler = &sensorHandler;
          _driver = &driver;
          _planner = &planner;
          _mainStates    = s_receive;
          _executeStates = s_planner;
        }
        
        void loop();
};

#endif // STATEMACHINEHANDLER_H
