#ifndef DRIVER_H
#define DRIVER_H

#include <MeEncoderOnBoard.h>

class Driver
{
    public:
        enum MainState : int {S_IDLE, S_DRIVING, S_DRIVING_DISTANCE, S_ROTATING, S_ROTATING_DEGREES, S_STOPPING};
        enum Direction : int {LEFT, RIGHT, FORWARD, REVERSE};
        
        /*
         * Description:
         *   Creates a Driver object. Starts in state S_IDLE. Only one driver object should be created.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   A Driver object.
         */
        Driver();


        /*
         * Description:
         *   Makes the robot drive in a straight line with a specified speed. Changes the state to S_DRIVING.
         *   Can only be called if Driver is in the state S_IDLE or S_DRIVING.
         * 
         * Parameters:
         *   speed - Specifies the speed of the robot. Positive speed -> drive forward, negative speed -> drive backwards.
         * 
         * Returns:
         *   Nothing
         */
        void drive(float speed);

        /*
         * Description:
         *   Makes the robot drive in a straight line a specified distance with a specified speed. Changes the state to S_DRIVING_DISTANCE.
         *   Can only be called if Driver is in the state S_IDLE.
         * 
         * Parameters:
         *   distance - Specifies the distance to move (millimeters). Positive distance -> drive forwards, negative distance -> drive backwards.
         *   speed    - Specifies the speed of the robot (should be positive).
         * 
         * Returns:
         *   Nothing
         */
        void drive(long distance, float speed);

        /*
         * Description:
         *   Makes the robot rotate on the spot with a specified speed. Changes the state to S_ROTATING.
         *   Can only be called if Driver is in the state S_IDLE or S_ROTATING.
         * 
         * Parameters:
         *   speed - Specifies the speed of the robot. Positive speed -> turn left, negative speed -> turn right.
         * 
         * Returns:
         *   Nothing
         */
        void rotate(float speed);

        /*
         * Description:
         *   Makes the robot rotate on the spot a specified angle with a specified speed. Changes the state to S_ROTATING_DEGREES.
         *   Can only be called if Driver is in the state S_IDLE.
         * 
         * Parameters:
         *   degrees - Specifies the angle to turn (degrees). Positive angle -> turn left, negative angle -> turn right.
         *   speed   - Specifies the speed of the robot.
         * 
         * Returns:
         *   Nothing
         */
        void rotate(float degrees, float speed);

        /*
         * Description:
         *   Makes the robot stop. Changes the state to S_STOPPING (and then to S_IDLE).
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
         *   Gets the state of Driver.
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
         *   Gets the direction of travel of Driver (last direction, if Driver is stopped).
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   A value from the Direction enumeration.
         */
        int getDirection();

        /*
         * Description:
         *   Gets the X coordinate of the robot's position.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   The X coordinate (millimeters).
         */
        long getPositionX();

        /*
         * Description:
         *   Gets the Y coordinate of the robot's position.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   The Y coordinate (millimeters).
         */
        long getPositionY();

        /*
         * Description:
         *   Gets the rotation of the robot.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   The robot's rotation (degrees, between 0 and 360).
         */
        float getRotation();

        /*
         * Description:
         *   Resets the position of the robot. (X, Y) = (0, 0), Rotation = 0 degrees.
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   Nothing
         */
        void resetPosition();


        /*
         * Description:
         *   Updates important internal values. Must be run as frequently as possible (max. 200ms interval).
         * 
         * Parameters:
         *   None
         * 
         * Returns:
         *   Nothing
         */
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