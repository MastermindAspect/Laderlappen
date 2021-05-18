#ifndef PROTOCOLHANDLER_H
#define PROTOCOLHANDLER_H
#include <arduino.h>
#include<ArduinoQueue.h>

#define STANDARD_MESSAGE "00011024" 

//Event
#define POSITION_X 0x11
#define POSITION_Y 0x12
#define ULTRASONIC 0x13
#define LINE_FOLLOWER 0x14
#define DATA_HEADERS_FROM 0x11
#define DATA_HEADERS_TO 0x14

//Body
#define MAX_INDIVIDIUAL_DATA 0xFF

//Size
#define MAX_QUEUE_ENTIRE_PROTOCOL 32 // 2 are reserved for _from & _to
#define MAX_QUEUE_HEADS (MAX_QUEUE_ENTIRE_PROTOCOL-2) // = 15
#define MAX_QUEUE_BODYS (MAX_QUEUE_ENTIRE_PROTOCOL-2) // = 15

class Protocolhandler{
    private:
      uint8_t _version;
      String _from = "";
      String _to = "";
      String _package = "";
      
      ArduinoQueue<String> * _heads;
      ArduinoQueue<String> * _bodys;
      ArduinoQueue<String> * _entireProtocol;
      
      uint8_t _headsSize;
      uint8_t _bodysSize;
      uint8_t _entireProtocolSize;
  
    public:
        Protocolhandler(uint8_t entireProtocolSize = MAX_QUEUE_ENTIRE_PROTOCOL, uint8_t headsSize = MAX_QUEUE_HEADS, uint8_t bodysSize = MAX_QUEUE_BODYS)
          : _entireProtocolSize(entireProtocolSize), _headsSize(headsSize), _bodysSize(bodysSize){
             _entireProtocol = new ArduinoQueue<String>(entireProtocolSize);
             _heads = new ArduinoQueue<String>(headsSize);
             _bodys = new ArduinoQueue<String>(bodysSize);
             }
        
        ~Protocolhandler(){delete[] _entireProtocol; delete[] _heads; delete[] _bodys;}
        
        bool unpackage(String protocolString);
        void reset();

        String getFrom(){return _from;}
        String getTo(){return _to;}
        String getPackage();

        String hexToString(uint8_t hex);
        
        ArduinoQueue<String>* getHeads(){return _heads;}
        ArduinoQueue<String>* getBodys(){return _bodys;}
        
        uint8_t getSizeOfProtocol(){return _entireProtocol->itemCount();}
        uint8_t getSizeOfHeads(){return _heads->itemCount();}
        uint8_t getSizeOfBodys(){return _bodys->itemCount();}

        void packageFrom(uint8_t hexFrom);
        void packageTo(uint8_t hexTo);
        void packageHeadAndBody(uint8_t head, uint16_t body);

        void appendToAll(String head, String body);
        
        void readyPackage();
        bool attemptReadyPackage();
};

#endif // PROTOCOLHANDLER_H
