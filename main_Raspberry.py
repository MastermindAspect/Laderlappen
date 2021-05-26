from threading import local
from DataHandler import DataHandler

from usbcommunicator import UsbCommunicator
from wifiClient import WebSocket
from protocolhandler import ProtocolHandler
try:
    import thread
except ImportError:
    import _thread as thread
    
from datetime import datetime


#Defines
TO_ARDUINO = "00"
TO_RASPBERRY = "01"
TO_BACKEND = "02"
TO_APP = "03"
TO_ARDUINO_PLANER ="04"
HEAD_EVENT = "10"
HEAD_POSITION_X = "11"
HEAD_POSITION_Y = "12"
HEAD_ULTRASONIC_DATA = "13"
HEAD_LINE_FOLLOWER_DATA = "14"
HEAD_DRIVE_STATE = "15"
HEAD_DRIVE_COMMAND = "16"
BODY_COLLISION = "20"
BODY_NO_COLLISION = "47"
BODY_ON_THE_LINE = "21"
BODY_AUTOMATIC_DRIVING_ON = "22"
BODY_MANUAL_DRIVING_ON = "23"
BODY_INITIALIZED = "50"


# Initializing Modules
usbUno = UsbCommunicator(baudRate = 115200, portNumber = 0)
protocol = ProtocolHandler()
dataHandler = DataHandler()
wifiClient = WebSocket()


if __name__ == "__main__":
    # Start new thread for the socket to run on
    thread.start_new_thread(wifiClient.startSocket, ())
    
    # A counter variable that is used when sending no_collision messages to the app
    counter = 0
    
    # Wait for connection to server, which means internet connection is established and we are ready to go. 
    while not wifiClient.connected():
        pass

    # send message to arduino when raspberry is ready, used only at startup
    protocol.packageTo(TO_ARDUINO)
    protocol.packageFrom(TO_RASPBERRY)
    protocol.packageHeadAndBody(HEAD_EVENT, BODY_INITIALIZED)
    while not usbUno.send(protocol.getPackage()):
        pass
    protocol.reset()
    
    while True:
            
        #If message is received from app, give that message to the arduino
        if wifiClient.getIfMessageReceived():
            msg = wifiClient.getMessage()
            if len(msg) < 10:
                while not usbUno.send(msg):
                    pass
            MowerInAuto = False

        #If a message is received via serial, check the message and give it to the right recipients. 
        message = usbUno.readGetTry()
        if message != "" and protocol.unpackage(message):
            # Increment counter for no_colliton messages
            counter += 1

            protocol.unpackage(message)
            to = protocol.getTo()

            # Implement for future needs
            if to == TO_ARDUINO:
                pass

            # Implement for future needs
            elif to == TO_RASPBERRY:
                pass
                
            elif to == TO_BACKEND:
                collision = False
                onTheLine = False
                dict = protocol.getDataDict()
                try:
                    if dict[HEAD_EVENT] == BODY_COLLISION:
                        collision = True
                    if dict[HEAD_EVENT] == BODY_ON_THE_LINE:
                        onTheLine = True
                except:
                    pass
                dataHandler.storeMowerPath(dict[HEAD_POSITION_X], dict[HEAD_POSITION_Y], collision, onTheLine)
                protocol.reset()
                
                # Check if the needed clients are connected to the server 
                if wifiClient.connected() and wifiClient.appConnected():
            
                    # If a collition message received, send a collition message to the app
                    if collision:
                        counter = 0
                        protocol.packageTo(TO_APP)
                        protocol.packageFrom(TO_ARDUINO)
                        protocol.packageHeadAndBody(HEAD_EVENT, BODY_COLLISION)
                        wifiClient.sendMessage(protocol.getPackage() + '>')
                        protocol.reset()
                    
                    # If a collition message was received on a previous message, and not the current message
                    # then a message is sent to the app saying that there no longer is a collition
                    if counter == 4 and not collision:
                        protocol.packageTo(TO_APP)
                        protocol.packageFrom(TO_ARDUINO)
                        protocol.packageHeadAndBody(HEAD_EVENT, BODY_NO_COLLISION)
                        wifiClient.sendMessage(protocol.getPackage() + '>')
                        protocol.reset()

            # Implement for future needs
            elif to == TO_APP:
                pass
                
        # If disconnected from the app, set arduino in auto driving mode.
        if (not wifiClient.connected() or not wifiClient.appConnected()):
            protocol.packageTo(TO_ARDUINO_PLANER)
            protocol.packageFrom(TO_RASPBERRY)
            protocol.packageHeadAndBody(HEAD_DRIVE_STATE, BODY_AUTOMATIC_DRIVING_ON)
            usbUno.send(protocol.getPackage())
            protocol.reset()

        



