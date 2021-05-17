#include "SensorHandler.h"

SensorHandler arduinoSensors;
void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  // This loop is a crappy clear screen.
  for(int i = 0; i <= 40; i++){
      Serial.println(" ");
  }
  arduinoSensors.loop();
  Serial.print("Ultrasonic: ");
  Serial.println(arduinoSensors.getProximity());
  Serial.print("Line: ");
  switch(arduinoSensors.getLinePerecept()){
    case(LEFT_LINE_SENSOR_TRUE):
      Serial.println("LEFT_LINE_SENSOR_TRUE");
    break;
    case(RIGHT_LINE_SENSOR_TRUE):
      Serial.println("RIGHT_LINE_SENSOR_TRUE");
    break;
    case(BOTH_LINE_SENSOR_TRUE):
      Serial.println("BOTH_LINE_SENSOR_TRUE");
    break;
    case(BOTH_LINE_SENSOR_FALSE):
      Serial.println("BOTH_LINE_SENSOR_FALSE");
    break;
    default:
      Serial.println("ERROR");
    break;
  }
  delay(500);
}
