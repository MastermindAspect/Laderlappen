#include "StateMachineHandler.h"

#define FOWARD_SPEED 35
#define BACKWARD_SPEED -35
#define LEFT_ROTATION_SPEED 25
#define RIGHT_ROTATION_SPEED -25

void StateMachineHandler::loop(){
  switch(_mainStates){
    case s_receive:
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
      //Execute received command
      if(_gotMessage){
        //Converting string to int for switch case.. //TODO add functions in protocol that returns int.
        uint8_t headTop = strtoul(protoHandlerIn.dequeueHeads().c_str(), 0, 16);
        uint8_t bodyTop = strtoul(protoHandlerIn.dequeueBodys().c_str(), 0, 16);
        uint8_t packageTo = strtoul(protoHandlerIn.getTo().c_str(), 0, 16);
        switch(packageTo){
          case executeStates::s_arduino:
            switch(headTop){
              case subExecuteStates::s_event:
                switch(bodyTop){
                  case eventStates::s_initialized:
                    _isAwake = true;
                    _planner->start();
                    break;
                }
                break;
            }
            break;
          case executeStates::s_planner:
            switch(headTop){
              case subExecuteStates::s_driveState:
                switch(bodyTop){
                  case driveCommandStates::s_automatic: //Automatic driving turned on
                    _planner->start();
                    break;
                  case driveCommandStates::s_manual: //Manual driving turned on
                    _planner->stop();
                    break;
                }
                break;
              case subExecuteStates::s_driveCommand:
                switch(bodyTop){
                  case driveStates::up_pressed:
                    _planner->manualDrive(FOWARD_SPEED);
                    break;
                  case driveStates::right_pressed:
                    _planner->manualRotate(RIGHT_ROTATION_SPEED);
                    break;
                  case driveStates::down_pressed:
                    _planner->manualDrive(BACKWARD_SPEED);
                    break;
                  case driveStates::left_pressed:
                    _planner->manualRotate(LEFT_ROTATION_SPEED);
                    break;
                  case driveStates::up_released:
                    _planner->stop();
                    break;
                  case driveStates::right_released:
                    _planner->stop();
                    break;
                  case driveStates::down_released:
                    _planner->stop();
                    break;
                  case driveStates::left_released:
                    _planner->stop();
                    break;
                }
                break;
            }
            break;
        }
      }      
      if(_isAwake){
        _mainStates = s_send; 
      }
      else{
        _mainStates = s_receive;
      }
      break;
    case s_send:
      if(millis() - _lastSendTime > _sendDelay){
        protoHandlerOut.reset();
        protoHandlerOut.packageFrom(0x00);
        protoHandlerOut.packageTo(0x01); //To all
        protoHandlerOut.packageHeadAndBody(0x11, _driver.getPositionX()); //Position X
        protoHandlerOut.packageHeadAndBody(0x12, _driver.getPositionY()); //Position Y
        switch(_planner.getNewEvent()){
          case 0:
            //no event
            //protoHandlerOut.packageHeadAndBody(0x10,0x47);
            //protoHandlerOut.packageHeadAndBody(0x10,0x??); not on the line?
            break;
          case 1:
            //colision
            protoHandlerOut.packageHeadAndBody(0x10,0x20);
            break;
          case 2:
            //on the line
            protoHandlerOut.packageHeadAndBody(0x10,0x21);
            break;
        }
        _arduinoUSB->send(protoHandlerOut.getPackage());
        _lastSendTime = millis();
      }
      _mainStates = s_check;
      break;
    case s_check:
      uint16_t numberOfHeadsLeft = protoHandlerIn.getSizeOfHeads();
      if(numberOfHeadsLeft != 0){ //If true then we have more stuff to execute
        _mainStates = s_execute;
      }
      _mainStates = s_receive;
      break;
  }
}
