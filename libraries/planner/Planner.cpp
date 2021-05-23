#include "Planner.h"
#include <Arduino.h>

inline int SPEED_DRIVE_LOW = 30;
inline int SPEED_DRIVE_HIGH = 40;
inline int SPEED_ROTATE_LOW = 20;
inline int SPEED_ROTATE_HIGH = 40;

inline int PROXIMITY_SHORT = 100;
inline int PROXIMITY_LONG = 200;


Planner::Planner(Driver* pDriver, SensorHandler* pSensorHandler)
{
    _pDriver = pDriver;
    _pSensorHandler = pSensorHandler;
    _state = MainState::S_IDLE;
    _event = Event::NONE;
    _pattern = Pattern::FORWARD;

    _patternForwardState = PatternForwardState::S_BEGIN;
    _patternCloseProximityState = PatternCloseProximityState::S_BEGIN;
    _patternOutsideBoundaryState = PatternOutsideBoundaryState::S_BEGIN;
}

void Planner::start()
{
    if (_state == MainState::S_IDLE) {
        _changeState(MainState::S_AUTONOMOUS);
    }
}

void Planner::stop()
{
    if (_state == MainState::S_AUTONOMOUS) {
        _changeState(MainState::S_IDLE);
    }
}

void Planner::manualDrive(float targetSpeed)
{
    if (_state == MainState::S_IDLE || _state == MainState::S_MANUAL) {
        _pDriver->drive(targetSpeed);
        _changeState(MainState::S_MANUAL);
    }
}

void Planner::manualRotate(float targetSpeed)
{
    if (_state == MainState::S_IDLE || _state == MainState::S_MANUAL) {
        _pDriver->rotate(targetSpeed);
        _changeState(MainState::S_MANUAL);
    }
}

void Planner::manualStop()
{
    if (_state == MainState::S_MANUAL) {
        _pDriver->stop();
        _changeState(MainState::S_IDLE);
    }
}

int Planner::getState()
{
    return int(_state);
}

// NOT WORKING
int Planner::getEvent()
{
    delay(1000);
    int event = int(_event);
    _setEvent(Event::NONE);
    return event;
}

void Planner::loop()
{
    _stateHandler();
}

void Planner::_stateHandler()
{
    switch (_state) {
        case MainState::S_IDLE:
            break;

        case MainState::S_AUTONOMOUS:
            _patternHandler();
            break;
        
        case MainState::S_MANUAL:
            if (_pSensorHandler->getProximity() < PROXIMITY_SHORT && _pDriver->getDirection() == Driver::Direction::FORWARD) {
                _setEvent(Event::CLOSE_PROXIMITY);
                _pDriver->stop();
                _changeState(MainState::S_IDLE);
            }
            break;

        default:
            break;
    }
}

void Planner::_setEvent(Event newEvent)
{
    _event = newEvent;
}

void Planner::_changeState(MainState newState)
{
    switch (_state) {
        case MainState::S_IDLE:
            switch (newState) {
                case MainState::S_AUTONOMOUS:
                    _changePattern(Pattern::FORWARD);
                    _state = newState;
                    break;
                
                case MainState::S_MANUAL:
                    _state = newState;
                    break;
                
                default:
                    break;
            }
            break;
        
        case MainState::S_AUTONOMOUS:
            switch (newState) {
                case MainState::S_IDLE:
                    _pDriver->stop();
                    _state = newState;
                    break;
                
                default:
                    break;
            }
            break;
        
        case MainState::S_MANUAL:
            switch (newState) {
                case MainState::S_IDLE:
                    _state = newState;
                    break;
                
                default:
                    break;
            }
            break;
        
        default:
            break;
    }
}

void Planner::_patternHandler()
{
    switch (_pattern) {
        case Pattern::FORWARD:
            _patternForwardHandler();
            break;
        
        case Pattern::CLOSE_PROXIMITY:
            _patternCloseProximityHandler();
            break;
        
        case Pattern::OUTSIDE_BOUNDARY:
            _patternOutsideBoundaryHandler();
            break;
        
        default:
            break;
    }
}

void Planner::_changePattern(Pattern newPattern)
{
    switch (newPattern) {
        case Pattern::FORWARD:
            _patternForwardState = PatternForwardState::S_BEGIN;
            _pattern = newPattern;
            break;
        
        case Pattern::CLOSE_PROXIMITY:
            _setEvent(Event::CLOSE_PROXIMITY);
            _patternCloseProximityState = PatternCloseProximityState::S_BEGIN;
            _pattern = newPattern;
            break;
        
        case Pattern::OUTSIDE_BOUNDARY:
            _setEvent(Event::OUTSIDE_BOUNDARY);
            _patternOutsideBoundaryState = PatternOutsideBoundaryState::S_BEGIN;
            _pattern = newPattern;
            break;
        
        default:
            break;
    }
}

void Planner::_patternForwardHandler()
{
    switch (_patternForwardState) {
        case PatternForwardState::S_BEGIN:
            _patternForwardState = PatternForwardState::S_FORWARD;
            break;

        case PatternForwardState::S_FORWARD:
            _pDriver->drive(SPEED_DRIVE_HIGH);
            _patternForwardState = PatternForwardState::S_FORWARD_WAIT;
            break;
        
        case PatternForwardState::S_FORWARD_WAIT:
            if (_pSensorHandler->getProximity() < PROXIMITY_SHORT) {
                _changePattern(Pattern::CLOSE_PROXIMITY);
            }
            if (_pSensorHandler->getBoundary() != SensorHandler::lineSensorState::BOTH_LINE_SENSOR_FALSE) {
                _changePattern(Pattern::OUTSIDE_BOUNDARY);
            }
            break;
        
        default:
            break;
    }
}

void Planner::_patternCloseProximityHandler()
{
    switch (_patternCloseProximityState) {
        case PatternCloseProximityState::S_BEGIN:
            _patternCloseProximityState = PatternCloseProximityState::S_STOP;
            break;

        case PatternCloseProximityState::S_STOP:
            _pDriver->stop();
            _patternCloseProximityState = PatternCloseProximityState::S_STOP_WAIT;
            break;
        
        case PatternCloseProximityState::S_STOP_WAIT:
            if (_pDriver->getState() == Driver::MainState::S_IDLE) {
                _patternCloseProximityState = PatternCloseProximityState::S_REVERSE;
            }
            break;

        case PatternCloseProximityState::S_REVERSE:
            _pDriver->drive(-200, SPEED_DRIVE_LOW);
            _patternCloseProximityState = PatternCloseProximityState::S_REVERSE_WAIT;
            break;

        case PatternCloseProximityState::S_REVERSE_WAIT:
            if (_pSensorHandler->getBoundary() != SensorHandler::lineSensorState::BOTH_LINE_SENSOR_FALSE) {
                _changePattern(Pattern::OUTSIDE_BOUNDARY);
            }
            if (_pDriver->getState() == Driver::MainState::S_IDLE) {
                _patternCloseProximityState = PatternCloseProximityState::S_TURN;
            }
            break;
        
        case PatternCloseProximityState::S_TURN:
            {
                int turnAngle;
                if (random(0, 1+1)) {
                    turnAngle = random(90, 120+1);
                }
                else {
                    turnAngle = random(-120, -90+1);
                }
                _pDriver->rotate(turnAngle, SPEED_ROTATE_HIGH);
            }
            _patternCloseProximityState = PatternCloseProximityState::S_TURN_WAIT;
            break;
        
        case PatternCloseProximityState::S_TURN_WAIT:
            if (_pDriver->getState() == Driver::MainState::S_IDLE) {
                _changePattern(Pattern::FORWARD);
            }
            break;
        
        default:
            break;
    }
}

void Planner::_patternOutsideBoundaryHandler()
{
    static Driver::Direction direction;

    switch (_patternOutsideBoundaryState) {
        case PatternOutsideBoundaryState::S_BEGIN:
            direction = Driver::Direction(_pDriver->getDirection());
            _patternOutsideBoundaryState = PatternOutsideBoundaryState::S_STOP;
            break;
        
        case PatternOutsideBoundaryState::S_STOP:
            _pDriver->stop();
            _patternOutsideBoundaryState = PatternOutsideBoundaryState::S_STOP_WAIT;
            break;
        
        case PatternOutsideBoundaryState::S_STOP_WAIT:
            if (_pDriver->getState() == Driver::MainState::S_IDLE) {
                _patternOutsideBoundaryState = PatternOutsideBoundaryState::S_MOVE_INSIDE_BOUNDARY;
            }
            break;
        
        case PatternOutsideBoundaryState::S_MOVE_INSIDE_BOUNDARY:
            {
                if (direction == Driver::Direction::FORWARD) {
                    _pDriver->drive(-100, SPEED_DRIVE_LOW);
                }
                else if (direction == Driver::Direction::REVERSE) {
                    _pDriver->drive(200, SPEED_DRIVE_LOW);
                }
            }
            _patternOutsideBoundaryState = PatternOutsideBoundaryState::S_MOVE_INSIDE_BOUNDARY_WAIT;
            break;
        
        case PatternOutsideBoundaryState::S_MOVE_INSIDE_BOUNDARY_WAIT:
            if (_pSensorHandler->getProximity() < PROXIMITY_SHORT) {
                _changePattern(Pattern::CLOSE_PROXIMITY);
            }
            if (_pDriver->getState() == Driver::MainState::S_IDLE) {
                _patternOutsideBoundaryState = PatternOutsideBoundaryState::S_TURN;
            }
            break;
        
        case PatternOutsideBoundaryState::S_TURN:
            {
                int turnAngle;
                if (direction == Driver::Direction::FORWARD) {
                    if (random(0, 1+1)) {
                        turnAngle = random(45, 135+1);
                    }
                    else {
                        turnAngle = random(-135, -45+1);
                    }
                }
                else if (direction == Driver::Direction::REVERSE) {
                    turnAngle = random(-60, 60+1);
                }
                _pDriver->rotate(turnAngle, SPEED_ROTATE_HIGH);
            }
            _patternOutsideBoundaryState = PatternOutsideBoundaryState::S_TURN_WAIT;
            break;
        
        case PatternOutsideBoundaryState::S_TURN_WAIT:
            if (_pDriver->getState() == Driver::MainState::S_IDLE) {
                _changePattern(Pattern::FORWARD);
            }
            break;
        
        default:
            break;
    }
}
