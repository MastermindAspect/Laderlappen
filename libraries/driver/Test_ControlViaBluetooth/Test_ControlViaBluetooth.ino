#include "Driver.h"

Driver driver;

unsigned long printTimer = 0;
unsigned long printWait  = 1000;

void setup() {
  // put your setup code here, to run once:
  
  Serial.begin(115200);
}

void loop() {
  // put your main code here, to run repeatedly:

  if (Serial.available()) {
    char input = Serial.read();

    switch (input) {
      case 'w':
        // driver.drive(50);
        driver.drive(200,50);
        break;
      case 'x':
        // driver.drive(-50);
        driver.drive(-200,50);
        break;
      case 'a':
        // driver.rotate(30);
        driver.rotate(90,30);
        break;
      case 'd':
        // driver.rotate(-30);
        driver.rotate(-90,30);
        break;
      case 's':
        driver.stop();
        break;
      case 'r':
        driver.resetPosition();
        break;
    }
  }

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
