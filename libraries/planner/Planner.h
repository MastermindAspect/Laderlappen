#ifndef PLANNER_H
#define PLANNER_H

#include <arduino.h>
#include "Driver.h"
#include "SensorHandler.h"


class Planner
{
    public:
        enum class MainState : int {S_IDLE, S_AUTONOMOUS, S_MANUAL};
        enum class Event : int {NONE, CLOSE_PROXIMITY, OUTSIDE_BOUNDARY};

        Planner(Driver* pDriver, SensorHandler* pSensorHandler);

        void start();
        void stop();

        void manualDrive(float targetSpeed);
        void manualRotate(float targetSpeed);
        void manualStop();

        int getState();

        // NOT WORKING
        int getEvent();

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

        void _setEvent(Event newEvent);

        void _stateHandler();
        void _changeState(MainState newState);

        void _patternHandler();
        void _changePattern(Pattern newPattern);

        void _patternForwardHandler();
        void _patternCloseProximityHandler();
        void _patternOutsideBoundaryHandler();
};

#endif
