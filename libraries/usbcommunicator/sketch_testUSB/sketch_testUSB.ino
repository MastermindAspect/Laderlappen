#include <SoftwareSerial.h>
#include "usbcommunicator.h"
#include "protocolhandler.h"
Usbcommunicator arduinoUSB(Serial);
Protocolhandler protoHandlerIn;
Protocolhandler protoHandlerOut;

unsigned int i = 0;
void setup() {
  // put your setup code here, to run once:
  arduinoUSB.begin(115200);
}
unsigned int gTime = 0;
unsigned int timeDelay = 500;

void loop() {
  String msg = arduinoUSB.readGetTry();
  if(msg != ""){
    if(protoHandlerIn.unpackage(msg)){
      protoHandlerIn.readyPackage();
      arduinoUSB.send(protoHandlerIn.getPackage());
    }
  }
  else{
      protoHandlerOut.reset();
      protoHandlerOut.packageFrom(0x00);
      protoHandlerOut.packageTo(0x01);
      protoHandlerOut.packageHeadAndBody(0x13, -1);
      delay(500); //recomended to use delay..
      if(millis() - gTime > timeDelay{
        arduinoUSB.send(protoHandlerOut.getPackage());
        gTime = millis();  
      }
      
  }
}
