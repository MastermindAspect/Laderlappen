#ifndef USBCOMMUNICATOR_H
#define USBCOMMUNICATOR_H
#include <arduino.h>
#include<SoftwareSerial.h>
#include<ArduinoQueue.h>

#define STANDARD_BAUD 9600
#define STANDARD_TIMEOUT 0.1

#define END_INDICATOR_SEND '>'

#define END_INDICATOR_READUNTIL '>'
#define END_INDICATOR_READCOMPLETE '<'

#define COMPLETED_INDEX 0
#define REST_INDEX 1

#define MAX_QUEUE 50
#define MAX_SIZE 0

class Usbcommunicator{
    private:
      uint32_t _baudrate;
      uint8_t _messageQueSize;
      String _completedMessage = "";
      String _currentMessage = "";
      ArduinoQueue<String> * _messageQue;
      HardwareSerial* _serial;    
    public:
        Usbcommunicator(HardwareSerial& serial, uint8_t messageQueSize = MAX_QUEUE, uint8_t timeOut = STANDARD_TIMEOUT)
          : _messageQueSize(messageQueSize){
             _messageQue = new ArduinoQueue<String>(messageQueSize);
             _serial = &serial;
             _serial->setTimeout(timeOut);
             }
        
        ~Usbcommunicator(){delete[] _messageQue;}
        
        void begin(uint32_t baud = STANDARD_BAUD);
        int getSizeOfQue();
        void readInSlice();
        bool read();
        void readUntil();
        String tryGetMessage();
        String readGetTry();
        String readGetUntil();
        void send(String message);
};

#endif // USBCOMMUNICATOR_H
