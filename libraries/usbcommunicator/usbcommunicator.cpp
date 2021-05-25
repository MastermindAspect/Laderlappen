#include "usbcommunicator.h"

#define END_INDICATOR_NOT_FOUND -1

void Usbcommunicator::begin(uint32_t baud){
  _baudrate = baud;
  _serial->begin(_baudrate);
  _serial->flush();
}

void Usbcommunicator::readInSlice(){
  if(Serial.available()){
     String messageSlice = Serial.readStringUntil(END_INDICATOR_READUNTIL);
     _currentMessage = _currentMessage + messageSlice;
  }
}

int Usbcommunicator::getSizeOfQue(){
  return _messageQue->itemCount();
}

bool Usbcommunicator::read(){
  readInSlice();
  int endIndex = _currentMessage.indexOf(END_INDICATOR_READCOMPLETE);
  if(endIndex !=  END_INDICATOR_NOT_FOUND){
    _currentMessage.remove(endIndex);
    _completedMessage = _currentMessage;
    _messageQue->enqueue(_completedMessage);
    _currentMessage = "";
    return true; 
  }
  return false;
}

void Usbcommunicator::readUntil(){
  while(Usbcommunicator::read() != true){}
}

String Usbcommunicator::tryGetMessage(){
  if(_messageQue->isEmpty()){
    return "";
  }
  return _messageQue->dequeue();
}
String Usbcommunicator::readGetTry(){
  Usbcommunicator::read();
  return tryGetMessage();
}
String Usbcommunicator::readGetUntil(){
  readUntil();
  return tryGetMessage();
}
void Usbcommunicator::send(String message){
  if(message != ""){
    Serial.print(message + END_INDICATOR_SEND);
  }
}
