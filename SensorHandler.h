#ifndef SensorHandler_H
#define SensorHandler_H

#include <Arduino.h>
#include "MeUltrasonicSensor.h"
#include "MeLineFollower.h"

#define LEFT_LINE_SENSOR_TRUE (S1_IN_S2_OUT)
#define RIGHT_LINE_SENSOR_TRUE (S1_OUT_S2_IN)
#define BOTH_LINE_SENSOR_TRUE (S1_IN_S2_IN)
#define BOTH_LINE_SENSOR_FALSE (S1_OUT_S2_OUT)

class SensorHandler
{
    public:
        SensorHandler(int ultrasonicPort = PORT_7, int lineFollowerPort = PORT_6);
        int getProximity();
		    int getLinePerecept();
        void loop();
    private:
        MeUltrasonicSensor _ultrasonicSensor;
		    MeLineFollower _lineSensor;
        int _proximity;
		    int _leftAndRightLineSensor;
};

#endif
