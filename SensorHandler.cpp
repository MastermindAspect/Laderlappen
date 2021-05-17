#include "SensorHandler.h"

SensorHandler::SensorHandler(int ultrasonicPort, int lineFollowerPort)
{
	//Initiate Sensors
  _ultrasonicSensor = MeUltrasonicSensor(ultrasonicPort);
	_lineSensor = MeLineFollower(lineFollowerPort);
	
	//Set attributes to initial values
  _proximity = 0;
	_leftAndRightLineSensor = BOTH_LINE_SENSOR_FALSE;
}

int SensorHandler::getProximity()
{
  return _proximity;
}

int SensorHandler::getLinePerecept()
{
  return _leftAndRightLineSensor;
}

void SensorHandler::loop()
{
  _proximity = int(_ultrasonicSensor.distanceCm() * 10);
	_leftAndRightLineSensor = _lineSensor.readSensors();
}
