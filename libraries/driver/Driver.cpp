#include <Arduino.h>
#include <avr/interrupt.h>
#include <math.h>
#include "Driver.h"

inline const float ROBOT_DEG_TO_MOTOR_DEG = 2.35102;
inline const float MOTOR_DEG_TO_ROBOT_DEG = 1 / 2.35102;

inline const float DISTANCE_MM_TO_MOTOR_DEG = 1.9;
inline const float MOTOR_DEG_TO_DISTANCE_MM = 1 / 1.9;


MeEncoderOnBoard Driver::_encoderMotorLeft(2);
MeEncoderOnBoard Driver::_encoderMotorRight(1);

Driver::Driver()
{
    // Attach interupts for counting how far the motors have moved
    attachInterrupt(Driver::_encoderMotorRight.getIntNum(), _IsrEncoderRight, RISING);
    attachInterrupt(Driver::_encoderMotorLeft.getIntNum(), _IsrEncoderLeft, RISING);

    // Set PWM 8KHz - Black magic
    TCCR1A = _BV(WGM10);
    TCCR1B = _BV(CS11) | _BV(WGM12);
    TCCR2A = _BV(WGM21) | _BV(WGM20);
    TCCR2B = _BV(CS21);
    
    // Black magic
    Driver::_encoderMotorRight.setPulse(9);
    Driver::_encoderMotorLeft.setPulse(9);
    Driver::_encoderMotorRight.setRatio(39.267);
    Driver::_encoderMotorLeft.setRatio(39.267);

    Driver::_encoderMotorRight.setPosPid(1.8, 0, 0.2);
    Driver::_encoderMotorLeft.setPosPid(1.8, 0, 0.2);
    Driver::_encoderMotorRight.setSpeedPid(0.18, 0, 0);
    Driver::_encoderMotorLeft.setSpeedPid(0.18, 0, 0);

    _state = S_IDLE;

    _direction = FORWARD;

    _positionX = 0;
    _positionY = 0;
    _rotation = 0;
    _previousPositionX = _positionX;
    _previousPositionY = _positionY;
    _previousRotation = _rotation;

    _targetDistance = 0;
    _targetSpeed = 0;
    _targetRotation = 0;

    Driver::_encoderMotorLeft.resetPos();
    Driver::_encoderMotorRight.resetPos();
}

void Driver::drive(float speed)
{
    _targetSpeed = speed;
    _changeState(S_DRIVING);
}

void Driver::drive(long distance, float speed)
{
    _targetDistance = distance;
    _targetSpeed = speed;
    _changeState(S_DRIVING_DISTANCE);
}

void Driver::rotate(float speed)
{
    _targetSpeed = speed;
    _changeState(S_ROTATING);
}

void Driver::rotate(float degrees, float speed)
{
    _targetRotation = degrees;
    _targetSpeed = speed;
    _changeState(S_ROTATING_DEGREES);
}

void Driver::stop()
{
    _changeState(S_STOPPING);
}

int Driver::getState()
{
    return _state;
}

int Driver::getDirection()
{
    return _direction;
}

long Driver::getPositionX()
{
    return _positionX;
}

long Driver::getPositionY()
{
    return _positionY;
}

float Driver::getRotation()
{
    return _rotation;
}

void Driver::resetPosition()
{
    if (_state == S_IDLE) {
        _previousPositionX = 0;
        _previousPositionY = 0;
        _previousRotation = 0;
        _positionX = 0;
        _positionY = 0;
        _rotation = 0;
    }
}

void Driver::loop()
{
    _stateHandler();

    Driver::_encoderMotorLeft.loop();
    Driver::_encoderMotorRight.loop();

    _updatePosition();
}



void Driver::_stateHandler()
{
    switch (_state) {
        case S_IDLE:
            break;

        case S_DRIVING:
            break;

        case S_DRIVING_DISTANCE:
            if (Driver::_encoderMotorLeft.isTarPosReached() == true && Driver::_encoderMotorRight.isTarPosReached() == true) {
                _changeState(S_STOPPING);
            }
            break;

        case S_ROTATING:
            break;

        case S_ROTATING_DEGREES:
            if (Driver::_encoderMotorLeft.isTarPosReached() == true && Driver::_encoderMotorRight.isTarPosReached() == true) {
                _changeState(S_STOPPING);
            }
            break;

        case S_STOPPING:
            _updatePosition();
            _previousPositionX = _positionX;
            _previousPositionY = _positionY;
            _previousRotation = _rotation;
            Driver::_encoderMotorRight.resetPos();
            Driver::_encoderMotorLeft.resetPos();
            _changeState(S_IDLE);
            break;

        default:
            break;
    }
}

void Driver::_changeState(MainState newState)
{
    switch (_state) {
        case S_IDLE:
            switch (newState) {
                case S_DRIVING:
                    Driver::_encoderMotorLeft.runSpeed(_targetSpeed);
                    Driver::_encoderMotorRight.runSpeed(-_targetSpeed);
                    if (_targetSpeed >= 0) {
                        _direction = FORWARD;
                    }
                    else {
                        _direction = REVERSE;
                    }
                    _state = newState;
                    break;
                
                case S_DRIVING_DISTANCE:
                    if (_targetSpeed > 0) {
                        Driver::_encoderMotorLeft.move(_targetDistance * DISTANCE_MM_TO_MOTOR_DEG, _targetSpeed);
                        Driver::_encoderMotorRight.move(-_targetDistance * DISTANCE_MM_TO_MOTOR_DEG, _targetSpeed);
                        if (_targetDistance >= 0) {
                            _direction = FORWARD;
                        }
                        else {
                            _direction = REVERSE;
                        }
                        _state = newState;
                    }
                    break;
                
                case S_ROTATING:
                    Driver::_encoderMotorLeft.runSpeed(-_targetSpeed);
                    Driver::_encoderMotorRight.runSpeed(-_targetSpeed);
                    if (_targetSpeed >= 0) {
                        _direction = LEFT;
                    }
                    else {
                        _direction = RIGHT;
                    }
                    _state = newState;
                    break;

                case S_ROTATING_DEGREES:
                    if (_targetSpeed > 0) {
                        Driver::_encoderMotorLeft.move(-_targetRotation * ROBOT_DEG_TO_MOTOR_DEG, _targetSpeed);
                        Driver::_encoderMotorRight.move(-_targetRotation * ROBOT_DEG_TO_MOTOR_DEG, _targetSpeed);
                        if (_targetRotation >= 0) {
                            _direction = LEFT;
                        }
                        else {
                            _direction = RIGHT;
                        }
                        _state = newState;
                    }
                    break;

                default:
                    break;
            }
            break;

        case S_DRIVING:
            switch (newState) {
                case S_DRIVING:
                    Driver::_encoderMotorLeft.runSpeed(_targetSpeed);
                    Driver::_encoderMotorRight.runSpeed(-_targetSpeed);
                    if (_targetSpeed >= 0) {
                        _direction = FORWARD;
                    }
                    else {
                        _direction = REVERSE;
                    }
                    _state = newState;
                    break;

                case S_STOPPING:
                    _state = newState;
                    break;
            }
            break;

        case S_DRIVING_DISTANCE:
            switch (newState) {
                case S_STOPPING:
                    _state = newState;
                    break;
            }
            break;

        case S_ROTATING:
            switch (newState) {
                case S_ROTATING:
                    Driver::_encoderMotorLeft.runSpeed(-_targetSpeed);
                    Driver::_encoderMotorRight.runSpeed(-_targetSpeed);
                    if (_targetSpeed >= 0) {
                        _direction = LEFT;
                    }
                    else {
                        _direction = RIGHT;
                    }
                    _state = newState;
                    break;
                    
                case S_STOPPING:
                    _state = newState;
                    break;
            }
            break;

        case S_ROTATING_DEGREES:
            switch (newState) {
                case S_STOPPING:
                    _state = newState;
                    break;
            }
            break;

        case S_STOPPING:
            switch (newState) {
                case S_IDLE:
                    _state = newState;
                    break;
            }
            break;

        default:
            break;
    }
}

void Driver::_updatePosition()
{
    switch (_state)
    {
        case S_DRIVING:
            // Same calculation as for S_DRIVING_DISTANCE

        case S_DRIVING_DISTANCE:
            {
                long distanceSinceLastStop = Driver::_encoderMotorLeft.getCurPos() * MOTOR_DEG_TO_DISTANCE_MM;
                _positionX = _previousPositionX + distanceSinceLastStop * cos(_rotation * (M_PI/180.0));
                _positionY = _previousPositionY + distanceSinceLastStop * sin(_rotation * (M_PI/180.0));
            }
            break;

        case S_ROTATING:
            // Same calculation as for S_ROTATING_DEGREES

        case S_ROTATING_DEGREES:
            {
                float rotationSinceLastStop = ((-Driver::_encoderMotorLeft.getCurPos() - Driver::_encoderMotorRight.getCurPos()) / 2.0) * MOTOR_DEG_TO_ROBOT_DEG;
                _rotation = _previousRotation + rotationSinceLastStop;

                if (_rotation >= 360) {
                    _rotation -= (int(_rotation) / 360) * 360;
                }
                else if (_rotation < 0) {
                    _rotation -= (int(_rotation) / 360) * 360 - 360;
                }
            }
            break;

        default:
            break;
    }
}

void Driver::_IsrEncoderLeft()
{
    if (digitalRead(Driver::_encoderMotorLeft.getPortB()) == 0)
    {
        Driver::_encoderMotorLeft.pulsePosMinus();
    }
    else
    {
        Driver::_encoderMotorLeft.pulsePosPlus();
    }
}

void Driver::_IsrEncoderRight()
{
    if (digitalRead(Driver::_encoderMotorRight.getPortB()) == 0)
    {
        Driver::_encoderMotorRight.pulsePosMinus();
    }
    else
    {
        Driver::_encoderMotorRight.pulsePosPlus();
    }
}