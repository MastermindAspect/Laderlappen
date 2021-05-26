#ifndef PLANNER_H
#define PLANNER_H

#include "Driver.h"
#include "SensorHandler.h"


class Planner
{
    public:
        enum class MainState : int {S_IDLE, S_AUTONOMOUS, S_MANUAL};
        enum class Event : int {NONE, CLOSE_PROXIMITY, OUTSIDE_BOUNDARY};

        /*
         * Description:
         *   Creates a Planner object. Only one Planner object should be created. Starts in state S_IDLE.
         *   The Driver object should not be used outside the Planner object.
         * 
         * Parameters:
         *   pDriver        - Pointer to a Driver object. Used to drive the robot.
         *   pSensorHandler - Pointer to a SensorHandler object. Used to sense the robots environment.
         * 
         * Returns:
         *   A Planner object.
         */
        Planner(Driver* pDriver, SensorHandler* pSensorHandler);


        /*
         * Description:
         *   Starts autonomous driving. Changes Planner's state to S_AUTONOMOUS.
         *   Can only be called if Planner is in the state S_IDLE.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   Nothing
         */
        void start();

        /*
         * Description:
         *   Stops autonomous driving. Changes Planner's state to S_IDLE.
         *   Can only be called if Planner is in the state S_AUTONOMOUS.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   Nothing
         */
        void stop();


        /*
         * Description:
         *   Starts manual driving in a straight line with a specified speed. Changes Planner's state to S_MANUAL.
         *   Can only be called if Planner is in the state S_IDLE (or S_MANUAL, if already set to drive in a straight line).
         *   The robot will be stopped if it needs to avoid collision.
         * 
         * Parameters:
         *   targetSpeed - Specifies the target speed of the robot.
         * 
         * Returns:
         *   Nothing
         */
        void manualDrive(float targetSpeed);

        /*
         * Description:
         *   Starts manual rotating on the spot with a specified speed. Changes Planner's state to S_MANUAL.
         *   Can only be called if Planner is in the state S_IDLE (or S_MANUAL, if already set to rotate on the spot).
         * 
         * Parameters:
         *   targetSpeed - Specifies the target speed of the robot.
         * 
         * Returns:
         *   Nothing
         */
        void manualRotate(float targetSpeed);

        /*
         * Description:
         *   Stops manual driving and rotating. Changes Planner's state to S_IDLE.
         *   Can only be called if Planner is in the state S_MANUAL.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   Nothing
         */
        void manualStop();


        /*
         * Description:
         *   Gets the state of Planner.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   A value from the MainState enumeration.
         */
        int getState();


        /*
         * Description:
         *   Gets the latest new event (eg. OUTSIDE_BOUNDARY).
         *   Resets the latest event to NONE when called.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   A value from the Event enumeration.
         */
        int getEvent();


        /*
         * Description:
         *   Updates important internal values. Must be run as frequently as possible.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   Nothing
         */
        void loop();

    private:
        enum class Pattern : int {FORWARD, CLOSE_PROXIMITY, OUTSIDE_BOUNDARY};

        enum class PatternForwardState : int {S_BEGIN, S_FORWARD, S_FORWARD_WAIT};
        enum class PatternCloseProximityState : int {S_BEGIN, S_STOP, S_STOP_WAIT, S_REVERSE, S_REVERSE_WAIT, S_TURN, S_TURN_WAIT};
        enum class PatternOutsideBoundaryState : int {S_BEGIN, S_STOP, S_STOP_WAIT, S_MOVE_INSIDE_BOUNDARY, S_MOVE_INSIDE_BOUNDARY_WAIT, S_TURN, S_TURN_WAIT};

        PatternForwardState _patternForwardState;
        PatternCloseProximityState _patternCloseProximityState;
        PatternOutsideBoundaryState _patternOutsideBoundaryState;

        Driver* _pDriver;
        SensorHandler* _pSensorHandler;

        MainState _state;
        Event _event;
        Pattern _pattern;

        void _stateHandler();
        void _changeState(MainState newState);

        void _patternHandler();
        void _changePattern(Pattern newPattern);

        void _patternForwardHandler();
        void _patternCloseProximityHandler();
        void _patternOutsideBoundaryHandler();
};

#endif
