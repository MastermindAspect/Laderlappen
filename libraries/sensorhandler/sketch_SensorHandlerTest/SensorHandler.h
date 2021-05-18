#ifndef SensorHandler_H
#define SensorHandler_H

#include <Arduino.h>
#include "MeUltrasonicSensor.h"
#include "MeLineFollower.h"

class SensorHandler
{
    public:
        enum lineSensorState {LEFT_LINE_SENSOR_TRUE = S1_IN_S2_OUT, RIGHT_LINE_SENSOR_TRUE = S1_OUT_S2_IN, BOTH_LINE_SENSOR_TRUE = S1_IN_S2_IN, BOTH_LINE_SENSOR_FALSE = S1_OUT_S2_OUT}_lineSensorState;
        SensorHandler(int ultrasonicPort = PORT_7, int lineFollowerPort = PORT_6);
        int getProximity();
		int getBoundary();
        void loop();
    private:
        MeUltrasonicSensor _ultrasonicSensor;
		MeLineFollower _lineSensor;
        int _proximityValue;
		int _leftAndRightLineSensorValue;
};

#endif
