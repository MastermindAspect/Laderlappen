
#include "Driver.h"
#include "SensorHandler.h"
#include "Planner.h"

unsigned long previousMillis = 0;
unsigned long wait = 1000;

Driver driver;
SensorHandler sensorHandler;
Planner planner(&driver, &sensorHandler);

void setup() {
  Serial.begin(115200);
}

void loop() {
  sensorHandler.loop();
  driver.loop();
  planner.loop();

  Planner::MainState state = Planner::MainState(planner.getState());
  Planner::Event event = Planner::Event(planner.getEvent());

  // Print info from planner
  if (millis() - previousMillis > wait) {
    previousMillis = millis();
    Serial.println("----------");
    
    // Print state
    Serial.print("State: ");
    switch (state) {
      case Planner::MainState::S_IDLE:
        Serial.println("S_IDLE");
        break;
      case Planner::MainState::S_AUTONOMOUS:
        Serial.println("S_AUTONOMOUS");
        break;
      case Planner::MainState::S_MANUAL:
        Serial.println("S_MANUAL");
        break;
      default:
        break;
    }
    // Print event
    Serial.print("Event: ");
    switch (event) {
      case Planner::Event::NONE:
        Serial.println("NONE");
        break;
      case Planner::Event::CLOSE_PROXIMITY:
        Serial.println("CLOSE_PROXIMITY");
        break;
      case Planner::Event::OUTSIDE_BOUNDARY:
        Serial.println("OUTSIDE_BOUNDARY");
        break;
      default:
        break;
    }
    Serial.println("----------");
    Serial.println("");
  }

  // Control planner
  if (Serial.available()) {
    char input = Serial.read();

    switch (input) {
      case 'g':
        switch (state) {
          case Planner::MainState::S_IDLE:
            planner.start();
            break;
          case Planner::MainState::S_AUTONOMOUS:
            planner.stop();
            break;
          default:
            break;
        }
        break;
        
      case 'w':
        planner.manualDrive(30);
        break;
          
      case 'x':
        planner.manualDrive(-30);
        break;
        
      case 'a':
        planner.manualRotate(20);
        break;
        
      case 'd':
        planner.manualRotate(-20);
        break;
        
      case 's':
        planner.manualStop();
        break;
    }
  }
}
