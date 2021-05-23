#include "StateMachineHandler.h"

void StateMachineHandler::loop(){
  switch(_mainStates){
    case s_receive:
      /*----------REMOVE-----------*/
      delay(500);
      _arduinoUSB->send("s_receive ");
      /*----------REMOVE-----------*/
      _gotMessage = false;
      _message = _arduinoUSB->readGetTry();
      if(_message != ""){
        if(protoHandlerIn.unpackage(_message)){
          _gotMessage = true;
        }
        //We could possible use this fail to ask for another message.
      }
      _mainStates = s_execute;
      break;
    case s_execute:
      /*----------REMOVE-----------*/
      delay(500);
      _arduinoUSB->send("s_execute ");
      /*----------REMOVE-----------*/
      //Execute received command
      if(_gotMessage){
        //Converting string to int for switch case.. //TODO add functions in protocol that returns int.
        uint8_t headTop = strtoul(protoHandlerIn.dequeueHeads().c_str(), 0, 16);
        uint8_t bodyTop = strtoul(protoHandlerIn.dequeueBodys().c_str(), 0, 16);
        uint8_t packageTo = strtoul(protoHandlerIn.getTo().c_str(), 0, 16);
        switch(packageTo){
          case executeStates::s_arduino:
            /*----------REMOVE-----------*/
            delay(500);
            _arduinoUSB->send("s_arduino ");
            /*----------REMOVE-----------*/
            switch(headTop){
              case subExecuteStates::s_event:
                /*----------REMOVE-----------*/
                delay(500);
                _arduinoUSB->send("s_event ");
                /*----------REMOVE-----------*/
                switch(bodyTop){
                  case eventStates::s_initialized:
                    _isAwake = true;
                    /*----------REMOVE-----------*/
                    delay(500);
                    _arduinoUSB->send("s_initialized ");
                    /*----------REMOVE-----------*/
                    break;
                }
                break;
            }
            break;
          case executeStates::s_planner:
            /*----------REMOVE-----------*/
            delay(500);
            _arduinoUSB->send("s_planner ");
            /*----------REMOVE-----------*/
            switch(headTop){
              case subExecuteStates::s_driveState:
                /*----------REMOVE-----------*/
                delay(500);
                _arduinoUSB->send("s_driveState ");
                /*----------REMOVE-----------*/             
                switch(bodyTop){
                  case driveCommandStates::s_automatic: //Automatic driving turned on
                    /*----------REMOVE-----------*/
                    delay(500);
                    _arduinoUSB->send("s_automatic ");
                    /*----------REMOVE-----------*/
                    break;
                  case driveCommandStates::s_manual: //Manual driving turned on
                    /*----------REMOVE-----------*/
                    delay(500);
                    _arduinoUSB->send("s_manual ");
                    /*----------REMOVE-----------*/
                    break;
                }
                break;
              case subExecuteStates::s_driveCommand:
                /*----------REMOVE-----------*/
                delay(500);
                _arduinoUSB->send("s_driveCommand ");
                /*----------REMOVE-----------*/
                switch(bodyTop){
                  case driveStates::up_pressed:
                    /*----------REMOVE-----------*/
                    delay(500);
                    _arduinoUSB->send("up_pressed ");
                    /*----------REMOVE-----------*/
                    break;
                  case driveStates::right_pressed:
                    /*----------REMOVE-----------*/
                    delay(500);
                    _arduinoUSB->send("right_pressed ");
                    /*----------REMOVE-----------*/
                    break;
                  case driveStates::down_pressed:
                    /*----------REMOVE-----------*/
                    delay(500);
                    _arduinoUSB->send("down_pressed ");
                    /*----------REMOVE-----------*/
                    break;
                  case driveStates::left_pressed:
                    /*----------REMOVE-----------*/
                    delay(500);
                    _arduinoUSB->send("left_pressed ");
                    /*----------REMOVE-----------*/
                    break;
                  case driveStates::up_released:
                    /*----------REMOVE-----------*/
                    delay(500);
                    _arduinoUSB->send("up_released ");
                    /*----------REMOVE-----------*/
                    break;
                  case driveStates::right_released:
                    /*----------REMOVE-----------*/
                    delay(500);
                    _arduinoUSB->send("right_released ");
                    /*----------REMOVE-----------*/
                    break;
                  case driveStates::down_released:
                    /*----------REMOVE-----------*/
                    delay(500);
                    _arduinoUSB->send("down_released ");
                    /*----------REMOVE-----------*/
                    break;
                  case driveStates::left_released:
                    /*----------REMOVE-----------*/
                    delay(500);
                    _arduinoUSB->send("left_released ");
                    /*----------REMOVE-----------*/
                    break;
                }
                break;
            }
            break;
        }
      }
      //Execute other stuff (loop...)
      
      if(_isAwake){
        _mainStates = s_send; 
      }
      else{
        _mainStates = s_receive;
      }
      break;
    case s_send:
      /*----------REMOVE-----------*/
      delay(500);
      _arduinoUSB->send("s_send ");
      /*----------REMOVE-----------*/
//      if(millis() - _lastSendTime > _sendDelay){
//        protoHandlerOut.reset();
//        protoHandlerOut.packageFrom(0x00);
//        protoHandlerOut.packageTo(0x01); //To all
//        //protoHandlerOut.packageHeadAndBody( head, body)
//        protoHandlerOut.packageHeadAndBody(0x10, 0x24)
//        _arduinoUSB->send(protoHandlerOut.getPackage());
//        _lastSendTime = millis();
//      }
      _mainStates = s_check;
      break;
    case s_check:
      /*----------REMOVE-----------*/
      delay(500);
      _arduinoUSB->send("s_check ");
      /*----------REMOVE-----------*/
      uint16_t numberOfHeadsLeft = protoHandlerIn.getSizeOfHeads();
      if(numberOfHeadsLeft != 0){ //If true then we have more stuff to execute
        _mainStates = s_execute;
      }
      _mainStates = s_receive;
      break;
  }
}
