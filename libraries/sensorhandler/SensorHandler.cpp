#include "SensorHandler.h"

SensorHandler::SensorHandler(int ultrasonicPort, int lineFollowerPort)
{
	//Initiate Sensors
	_ultrasonicSensor = MeUltrasonicSensor(ultrasonicPort);
	_lineSensor = MeLineFollower(lineFollowerPort);
	
	//Set attributes to initial values
	_proximityValue = 0;
	_leftAndRightLineSensorValue = BOTH_LINE_SENSOR_FALSE;
}

int SensorHandler::getProximity()
{
	return _proximityValue;
}

int SensorHandler::getBoundary()
{
	return _leftAndRightLineSensorValue;
}

void SensorHandler::loop()
{
	_proximityValue = int(_ultrasonicSensor.distanceCm() * 10);
	_leftAndRightLineSensorValue = _lineSensor.readSensors();
}
