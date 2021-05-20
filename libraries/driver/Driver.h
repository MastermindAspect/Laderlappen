#ifndef DRIVER_H
#define DRIVER_H

#include <MeEncoderOnBoard.h>

class Driver
{
    public:
        enum MainState : int {S_IDLE, S_DRIVING, S_DRIVING_DISTANCE, S_ROTATING, S_ROTATING_DEGREES, S_STOPPING};
        enum Direction : int {LEFT, RIGHT, FORWARD, REVERSE};
        
        Driver();

        void drive(float speed);
        void drive(long distance, float speed);
        void rotate(float speed);
        void rotate(float degrees, float speed);
        void stop();

        int getState();
        int getDirection();

        long getPositionX();
        long getPositionY();
        float getRotation();
        void resetPosition();

        void loop();

    private:
        static MeEncoderOnBoard _encoderMotorLeft;
        static MeEncoderOnBoard _encoderMotorRight;

        MainState _state;

        int _direction;

        long _targetDistance;
        float _targetSpeed;
        float _targetRotation;

        long _positionX;
        long _positionY;
        float _rotation;

        long _previousPositionX;
        long _previousPositionY;
        float _previousRotation;

        void _stateHandler();
        void _changeState(MainState newState);

        void _updatePosition();
        
        void static _IsrEncoderLeft();
        void static _IsrEncoderRight();
};

#endif