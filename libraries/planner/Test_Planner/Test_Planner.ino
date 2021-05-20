
#include "Driver.h"
#include "SensorHandler.h"
#include "Planner.h"

unsigned long previousMillis = 0;
unsigned long wait = 200;

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

  if (Serial.available() && Serial.read() == 's') {
    Planner::MainState state = Planner::MainState(planner.getState());
    switch (state) {
      case Planner::MainState::S_IDLE:
        planner.start();
        break;
      case Planner::MainState::S_RUNNING:
        planner.stop();
        break;
      default:
        break;
    }
  }
}
