from threading import local
from DataHandler import DataHandler

from usbcommunicator import UsbCommunicator
from wifiClient import WebSocket
from protocolhandler import ProtocolHandler
try:
    import thread
except ImportError:
    import _thread as thread


#Defines
TO_ARDUINO = "00"
TO_RASPBERRY = "01"
TO_BACKEND = "02"
TO_APP = "03"
HEAD_EVENT = "10"
HEAD_POSITION_X = "11"
HEAD_POSITION_Y = "12"
HEAD_ULTRASONIC_DATA = "13"
HEAD_LINE_FOLLOWER_DATA = "14"
HEAD_DRIVE_STATE = "15"
HEAD_DRIVE_COMMAND = "16"
BODY_COLLISION = "20"
BODY_NO_COLLITION = "47"
BODY_ON_THE_LINE = "21"
BODY_AUTOMATIC_DRIVING_ON = "22"
BODY_MANUAL_DRIVING_ON = "23"
BODY_INITIALIZED = "50"



usbUno = UsbCommunicator(baudRate = 115200, portNumber = 0)
protocol = ProtocolHandler()
dataHandler = DataHandler()
wifiClient = WebSocket()
latestSessionTimeStamp = None

#globals
g_previousMsgCollition = False


if __name__ == "__main__":
    thread.start_new_thread(wifiClient.startSocket, ())

    #send message to arduino when raspberry is redy
    protocol.packageTo(TO_ARDUINO)
    protocol.packageFrom(TO_RASPBERRY)
    protocol.packageHeadAndBody(HEAD_EVENT, BODY_INITIALIZED) 
    usbUno.send(protocol.getPackage())
    protocol.reset()

    while True:

        #If message is received from app, give that message to the arduino
        if wifiClient.getIfMessageReceived():
            print("message from app:")
            msg = wifiClient.getMessage()
            print(type(msg))
            print(msg)
            if len(msg) < 10:
                usbUno.send(msg)

    # 	#if disconnected from the app, set arduino in auto driving mode.	
        if not wifiClient.connected():
            protocol.packageTo(TO_ARDUINO)
            protocol.packageFrom(TO_RASPBERRY)
            protocol.packageHeadAndBody(HEAD_DRIVE_STATE, BODY_AUTOMATIC_DRIVING_ON)
            usbUno.send(protocol.getPackage())
            protocol.reset()

        #If a message is received via serial, check the message and give it to the right recipients. 
        message = usbUno.readGetTry()
        if message != "" and protocol.unpackage(message):
            print("message from arduino:")
            print(message)
            protocol.unpackage(message)

            to = protocol.getTo()
            if to == TO_ARDUINO:
                print(message)
                #pass
            elif to == TO_RASPBERRY:
                pass
            elif to == TO_BACKEND:
                collision = False
                onTheLine = False
                dict = protocol.getDataDict()
                if dict[HEAD_EVENT] == BODY_COLLISION:
                    collision = True
                if dict[HEAD_EVENT] == BODY_ON_THE_LINE:
                    onTheLine = True
                
                dataHandler.storeMowerPath(dict[HEAD_POSITION_X], dict[HEAD_POSITION_Y], collision, onTheLine)
                protocol.reset()
                
                # If a collition message received, send a collition message to the app
                if collision:
                    g_previousMsgCollition = True
                    protocol.packageTo(TO_APP)
                    protocol.packageFrom(TO_ARDUINO)
                    protocol.packageHeadAndBody(HEAD_EVENT, BODY_COLLISION)
                    wifiClient.sendMessage(protocol.getPackage() + '>')
                    protocol.reset()
                
                # If a collition message was received on the previous message, and not the current message
                # then a message is sent to the app saying that there no longer is a collition
                if g_previousMsgCollition and not collision:
                    g_previousMsgCollition = False
                    protocol.packageTo(TO_APP)
                    protocol.packageFrom(TO_ARDUINO)
                    protocol.packageHeadAndBody(HEAD_EVENT, BODY_COLLISION)
                    wifiClient.sendMessage(protocol.getPackage() + '>')
                    protocol.reset()

                
                
            #elif to == TO_APP:
            #	pass

        



