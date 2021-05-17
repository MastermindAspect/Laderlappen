#include "Driver.h"

enum TestState { S_DRIVE, S_DRIVE_STOP, S_DRIVE_END, S_ROTATE, S_ROTATE_STOP, S_ROTATE_END, S_DRIVE_DISTANCE, S_DRIVE_DISTANCE_STOP, S_DRIVE_DISTANCE_END, S_ROTATE_DEGREES, S_ROTATE_DEGREES_STOP, S_ROTATE_DEGREES_END, S_END };

Driver driver;

unsigned long printTimer = 0;
unsigned long printWait  = 1000;

unsigned long stateTimer = 0;
unsigned long stateWait = 0;

TestState state = S_DRIVE;

void setup() {
  // put your setup code here, to run once:
  
  Serial.begin(115200);

  while (true) {
    if (Serial.available() && Serial.read() == 's') {
      // Start test
      break;
    }
  }
}

void loop() {

  switch (state) {
    case S_DRIVE:
      driver.drive(50);
      state = S_DRIVE_STOP;
      stateTimer = millis();
      stateWait = 2000;
      break;
      
    case S_DRIVE_STOP:
      if (millis() - stateTimer > stateWait) {
        driver.stop();
        state = S_DRIVE_END;
        stateTimer = millis();
        stateWait = 500;
      }
      break;

    case S_DRIVE_END:
      if (millis() - stateTimer > stateWait) {
        state = S_ROTATE;
      }
      break;

    case S_ROTATE:
      driver.rotate(30);
      state = S_ROTATE_STOP;
      stateTimer = millis();
      stateWait = 2000;
      break;
      
    case S_ROTATE_STOP:
      if (millis() - stateTimer > stateWait) {
        driver.stop();
        state = S_ROTATE_END;
        stateTimer = millis();
        stateWait = 500;
      }
      break;

    case S_ROTATE_END:
      if (millis() - stateTimer > stateWait) {
        state = S_DRIVE_DISTANCE;
      }
      break;

    case S_DRIVE_DISTANCE:
      driver.drive(300, 50);
      state = S_DRIVE_DISTANCE_STOP;
      break;
      
    case S_DRIVE_DISTANCE_STOP:
      if (driver.getState() == Driver::MainState::S_IDLE) {
        state = S_DRIVE_DISTANCE_END;
        stateTimer = millis();
        stateWait = 500;
      }
      break;

    case S_DRIVE_DISTANCE_END:
      if (millis() - stateTimer > stateWait) {
        state = S_ROTATE_DEGREES;
      }
      break;

    case S_ROTATE_DEGREES:
      driver.rotate(-450, 30);
      state = S_ROTATE_DEGREES_STOP;
      stateTimer = millis();
      stateWait = 1000;
      break;
      
    case S_ROTATE_DEGREES_STOP:
      if (driver.getState() == Driver::MainState::S_IDLE) {
        state = S_ROTATE_DEGREES_END;
        stateTimer = millis();
        stateWait = 500;
      }
      break;

    case S_ROTATE_DEGREES_END:
      if (millis() - stateTimer > stateWait) {
        state = S_END;
      }
      break;

    case S_END:
      break;
  }


  // Print information about the mower
  if (millis() - printTimer > printWait) {
    printTimer = millis();
    Serial.println("----");
    Serial.print("Main State: ");
    int state = driver.getState();
    switch (state) {
      case Driver::MainState::S_IDLE:
        Serial.println("S_IDLE");
        break;
      case Driver::MainState::S_DRIVING:
        Serial.println("S_DRIVING");
        break;
      case Driver::MainState::S_DRIVING_DISTANCE:
        Serial.println("S_DRIVING_DISTANCE");
        break;
      case Driver::MainState::S_ROTATING:
        Serial.println("S_ROTATING");
        break;
      case Driver::MainState::S_ROTATING_DEGREES:
        Serial.println("S_ROTATING_DEGREES");
        break;
      case Driver::MainState::S_STOPPING:
        Serial.println("S_STOPPING");
        break;
      default:
        Serial.println("INVALID STATE");
        break;
    }
    Serial.print("Position X: ");
    Serial.println(driver.getPositionX());
    Serial.print("Position Y: ");
    Serial.println(driver.getPositionY());
    Serial.print("Rotation: ");
    Serial.println(driver.getRotation());
    Serial.println("----");
    Serial.println("");
  }

  driver.loop();
}
