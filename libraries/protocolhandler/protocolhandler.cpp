#include "protocolhandler.h"
#define END_INDICATOR_NOT_FOUND -1

bool Protocolhandler::unpackage(String protocolString){
  //reset
  reset();
  _from = protocolString.substring(0, 2);
  _to = protocolString.substring(2, 4);
  uint8_t checkFrom = strtoul(_from.c_str(), 0, 16);
  uint8_t checkTo = strtoul(_to.c_str(), 0, 16);
  if(checkFrom > 0x0F or checkTo > 0x0F or checkFrom == checkTo){
    reset();
    return false;
  }
  _entireProtocol->enqueue(_from);
  _entireProtocol->enqueue(_to);
  String headsAndBodys = protocolString.substring(4);
  uint8_t headsAndBodysLength = headsAndBodys.length();
  if(headsAndBodysLength % 2 != 0 or headsAndBodysLength == 0){
    //uneven heads and body
    reset();
    return false;
  }
  for(int i = 0; i < headsAndBodysLength; i += 4 ){
    String head = headsAndBodys.substring(i, i+2);
    String body = headsAndBodys.substring(i+2, i+4);
    appendToAll(head, body);
    }
  return true;
}

void Protocolhandler::reset(){
  _from = "";
  _to = "";
  _package = "";
  while(!_entireProtocol->isEmpty()){
    if(!(_heads->isEmpty() || _bodys->isEmpty())){
          _heads->dequeue();
          _bodys->dequeue();
    }
    _entireProtocol->dequeue();
  }
  while(!(_heads->isEmpty() || _bodys->isEmpty())){
    _heads->dequeue();
    _bodys->dequeue();
  }
}
String Protocolhandler::getPackage(){
  if(_package == ""){
    readyPackage();
  }
  return _package;
}

String Protocolhandler::hexToString(int32_t hex){
  String s = String(hex, 16);
  s.toUpperCase();
  if(s.length() % 2 != 0){
    s = "0" + s;
  }
  return s;
}

void Protocolhandler::packageFrom(uint8_t hexFrom){
  _from = hexToString(hexFrom);
  _entireProtocol->enqueue(_from);
  
}

void Protocolhandler::packageTo(uint8_t hexTo){
  _to = hexToString(hexTo);
  _entireProtocol->enqueue(_to);
}

void Protocolhandler::packageHeadAndBody(uint8_t head, int32_t bodyOrData){
  String strHead = hexToString(head);
  String strBodyOrData = hexToString(bodyOrData);
  //strBodyOrData can be a single body or more if it is data like an int, ex 1234 which is hex 04D2
  for(int i = 0; i < strBodyOrData.length(); i += 2){
      String strBody = strBodyOrData.substring(i, i+2); //Get the body or one of the sub body in data
      appendToAll(strHead, strBody);
  }
}

void Protocolhandler::appendToAll(String head, String body){
  _heads->enqueue(head);
  _bodys->enqueue(body);
  _entireProtocol->enqueue(head);
  _entireProtocol->enqueue(body);
}

void Protocolhandler::readyPackage(){
  while(!_entireProtocol->isEmpty()){
    _package += _entireProtocol->dequeue();
  }
}

bool Protocolhandler::attemptReadyPackage(){
  if(!_entireProtocol->isEmpty()){
    _package += _entireProtocol->dequeue();
  }
  if(_entireProtocol->isEmpty()){
    return true;
  }
  return false;
}
