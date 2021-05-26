import serial
import queue
import sys
from sys import platform
import time
import math


STANDARD_BAUD = 9600
STANDARD_TIMEOUT = 0.1
STANDARD_ADRESS = "/dev/ttyUSB"
STANDARD_PORT = 0
LAST_PORT = 10

END_INDICATOR_READ = ">"
END_INDICATOR_SEND = "><"
STANDARD_MESSAGE = "01001024"

COMPLETED_INDEX = 0
REST_INDEX = 1

MAX_QUEUE = 0

class UsbCommunicator:
    def __init__(self, baudRate = STANDARD_BAUD, timeOut = STANDARD_TIMEOUT, portNumber = STANDARD_PORT, maxQue = MAX_QUEUE):
        self._serial = self.tryConnectTo(baudRate, timeOut, portNumber)
        self._completedMessage = ""
        self._currentMessage = ""
        self._messageQue = queue.Queue(maxQue)
        self._baudrate = baudRate
        self._sendDelay = 0.241
        self._lastSendTime = time.time() + 1
        
    def tryConnectTo(self, baudRate, timeOut, portNumber):
        attemptPort = portNumber
        notConnected = True
        while notConnected:
            print(attemptPort)
            if attemptPort > LAST_PORT:
                raise Exception("Port was not found")
            try:
                print(f"{self.getUSBPath()}{attemptPort}")
                serialConnection = serial.Serial(port=f"{self.getUSBPath()}{attemptPort}", baudrate=baudRate, timeout=timeOut)
                notConnected = False
            except:
                #Port was not readable, OS might have placed device on diffrent port. So try another.
                print(sys.exc_info()[:2])
                attemptPort = attemptPort + 1
        serialConnection.flush()
        return serialConnection

    def getUSBPath(self):
        if platform == "linux" or platform == "linux2":
            return "/dev/ttyUSB"
        if platform == "win32":
            return "COM"
        raise Exception("OS identification failed in UsbComunicator -> getUSBPath")

    def readInSlice(self):
        try:
            #print("outside")
            if self._serial.in_waiting > 0:
                print("in")
                messageSlice = self._serial.read(self._serial.inWaiting())
                self._currentMessage = self._currentMessage + messageSlice.decode(encoding='UTF-8', errors='strict')
                print(self._currentMessage)
        except:
            print("Can't read slice from port")
            self._serial.flush()

    def read(self):
        self.readInSlice()
        if END_INDICATOR_READ in self._currentMessage:
            tempMessageList = self._currentMessage.split(END_INDICATOR_READ)
            nrOfIdentifiedMsg = len(tempMessageList)
            lastMessage = nrOfIdentifiedMsg - 1
            if self._currentMessage.endswith(tempMessageList[lastMessage]):
                #All messages is complete, so store them.
                for i in tempMessageList:
                    self._messageQue.put(i)
                self._currentMessage = ""
            else:
                for i in range(nrOfIdentifiedMsg - 1):
                    self._messageQue.put(tempMessageList[i])
                    
                self._currentMessage = tempMessageList[lastMessage]
				
            return True
        return False

    def readUntil(self):
        while self.read() != True:
            pass
        
    def tryGetMessage(self):
        if self._messageQue.empty():
            return ""
        return self._messageQue.get()

    def readGetTry(self):
        self.read()
        return self.tryGetMessage()

    def readGetUnitl(self):
        self.readUntil()
        return self.tryGetMessage()

    def send(self, message = STANDARD_MESSAGE):
        if time.time() - self._lastSendTime > self._sendDelay:
            self._serial.write(f"{message}{END_INDICATOR_SEND}".encode('utf-8'))
            self._lastSendTime = time.time()
            print(self._lastSendTime)
            return True
        return False
