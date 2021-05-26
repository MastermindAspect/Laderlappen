#ifndef SensorHandler_H
#define SensorHandler_H

#include <Arduino.h>
#include "MeUltrasonicSensor.h"
#include "MeLineFollower.h"

class SensorHandler
{
	public:
		enum lineSensorState {LEFT_LINE_SENSOR_TRUE = S1_IN_S2_OUT, RIGHT_LINE_SENSOR_TRUE = S1_OUT_S2_IN, BOTH_LINE_SENSOR_TRUE = S1_IN_S2_IN, BOTH_LINE_SENSOR_FALSE = S1_OUT_S2_OUT}_lineSensorState;
		
		/*
         * Description:
         *   Creates a SensorHandler object.
         * 
         * Parameters:
         *   ultrasonicPort   - The port of the ultrasonic sensor (default = 7).
         *   lineFollowerPort - The port of the line follower sensor (default = 6).
         * 
         * Returns:
         *   A SensorHandler object.
         */
		SensorHandler(int ultrasonicPort = PORT_7, int lineFollowerPort = PORT_6);


		/*
         * Description:
         *   Gets the proximity to objects in front of the robot from the ultrasonic sensor.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   Proximity to objects in front of the robot (millimeters, between 30 and 4000).
         */
		int getProximity();

		/*
         * Description:
         *   Gets the information from the line follower sensor to detect if it is on the boundary line.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   A value from the lineSensorState enumeration.
		 *   For example, LEFT_LINE_SENSOR_TRUE means that only the left sensor is on the boundary line.
         */
		int getBoundary();


		/*
         * Description:
         *   Updates sensor values. Must be run frequently for correct operation.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   Nothing
         */
		void loop();

	private:
		MeUltrasonicSensor _ultrasonicSensor;
		MeLineFollower _lineSensor;

		int _proximityValue;
		int _leftAndRightLineSensorValue;
};

#endif
