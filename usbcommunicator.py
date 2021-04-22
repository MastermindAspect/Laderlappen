import serial
import queue

STANDARD_BAUD = 9600
STANDARD_TIMEOUT = 1
STANDARD_ADRESS = "/dev/ttyACM"
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
        self._serial = self.tryConnectTo(baudRate, timeOut, baudRate, portNumber)
        self._completedMessage = ""
        self._currentMessage = ""
        self._messageQue = queue.Queue(maxQue)
        
    def tryConnectTo(self, baudRate, timeOut, portNumber):
        attemptPort = portNumber
        while true:
            if attemptPort > LAST_PORT:
                raise Exception("Port was not found")
            try:
                serialConnection = serial.Serial(f"{/dev/ttyACM}{attemptPort}", baudRate, timeOut)
                break
            except:
                #Port was not readable, OS might have placed device on diffrent port. So try another.
                attemptPort = attemptPort + 1
        serialConnection.flush()
        return serialConnection

    def readInSlice(self):
        try:
            if self._serial.in_waiting > 0:
                messageSlice = self._serial.read(self._serial.inWaiting())
                self._currentMessage = self._currentMessage + messageSlice
        except:
            print("Can't read slice from port")

    def read(self):
        self.readInSlice()
        if END_INDICATOR_READ in self._currentMessage:
            tempMessageList = self._currentMessage.split(END_INDICATOR_READ)
            self._completedMessage = tempMessageList[COMPLETED_INDEX]
            #If there is another message, then keep it.
            self._currentMessage = tempMessageList[REST_INDEX]
            self._messageQue.put(self._completedMessage)
            return True
        return False

    def readUntil(self):
        while self.read() != True   
        
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

    def send(message = STANDARD_MESSAGE):
        self.write(f"{message}{END_INDICATOR_SEND}")
