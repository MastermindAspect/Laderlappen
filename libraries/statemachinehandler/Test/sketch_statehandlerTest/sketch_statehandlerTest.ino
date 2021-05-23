//Include libraries
#include <SoftwareSerial.h>

#include "usbcommunicator.h"
#include "StateMachineHandler.h"

//Initiate objects
Usbcommunicator arduinoUSB(Serial);
StateMachineHandler stateMachine(arduinoUSB);
                            //awake
String testMessages[] = {"01001050><", "01041522><", "01041523><"
                         , "01041630><", "01041640><", "01041631><"
                         , "01041641><", "01041632><", "01041642><"
                         , "01041633><", "01041643><"};
int index = 0;

void setup() {
  // put your setup code here, to run once:
  arduinoUSB.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  
  stateMachine.loop();
}
