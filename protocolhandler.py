import numpy as np

#Event
POSITION_X = 0x11
POSITION_Y = 0x12
ULTRASONIC = 0x13
LINE_FOLLOWER = 0x14
DATA_HEADERS_FROM = 0x11
DATA_HEADERS_TO = 0x14 + 1



#Body
MAX_INDIVIDIUAL_DATA = 0xFF

STANDARD_MESSAGE = "01001024"
 
class ProtocolHandler:
    def __init__(self, version = 1):
        self._version = version
        self._from = ""
        self._to = ""
        self._heads = []
        self._bodys = []
        self._entireProtocol = []
        self._dataDict = {}
        self._package = ""

    def unpackage(self, protocolString):
        self.reset()
        try:
            self._from = (protocolString[:2])
            valOfFrom = bytes.fromhex(self._from)[0]
            self._to = (protocolString[2:4])
            valOfTo = bytes.fromhex(self._to)[0]
            if valOfFrom > 0x0F or valOfTo > 0x0F:
                print(f"ERROR: From: {self._from}\tTo: {self._to}")
                raise ValueError('recieved from or to is bad')
            self._entireProtocol.extend([self._from, self._to])

            #Creates a list containing list of the pairs head and body. [[h1,b1][h2,b2]...]
            headAndBodyList = [[x+y] for x, y in zip(*[iter(protocolString[4:])]*2)]
            self._entireProtocol.extend(headAndBodyList)

            #Creates individual head and body lists
            self._heads = [j for i in [h for i, h in enumerate(headAndBodyList) if not i%2] for j in i]
            self._bodys = [j for i in [b for i, b in enumerate(headAndBodyList[1:]) if not i%2] for j in i]
            if len(self._heads) != len(self._bodys):
                raise ValueError('recieved heads or bodys is bad, not same size')

            #Create data dictionary for retrieving data for reciver
            bodyToConvertInKeyList = []
            for x in range(len(self._heads)):
                head = self._heads[x]
                body = self._bodys[x]
                if not bytes.fromhex(head)[0] in range(DATA_HEADERS_FROM, DATA_HEADERS_TO):
                    self._dataDict[head] = body
                else:
                    #Its a head that might have a split up body if data over 255.
                    try:
                        #See if that head exist and add next body after the existing one.
                        self._dataDict[head] = self._dataDict[head] + body
                    except:
                        #else its first time occurance, then add this new head & body
                        self._dataDict[head] = body
                        bodyToConvertInKeyList.append(head)
            for y in bodyToConvertInKeyList:
                #Convert the hex into signed long
                self._dataDict[y] = self.__twosComplementConvertion(self._dataDict[y], 32)
            
            #Safe unpacking
            return True
        except:
            print("In protocolHandler, unpackage attempt failed")
            self.reset()
            #Did not package saftely
            return False

    def reset(self):
        self._from = ""
        self._to = ""
        self._heads = []
        self._bodys = []
        self._entireProtocol = []
        self._dataDict = {}
        self._package = ""
        
    def __twosComplementConvertion(self, strHex, bits):
        value = int(strHex, 16)
        if value & (1 << (bits - 1)):
            value -= 1 << bits
        return value

    def getFrom(self):
        return self._from

    def getTo(self):
        return self._to

    def getHeadAndBodyAtIdx(self, x):
        return [self._heads[x], self._bodys[x]]

    def getHeadAndBody(self):
        return self._heads, self._bodys

    def getDataDict(self):
        return self._dataDict
    
    def getPackage(self):
        if not self._package:
            self.readyPackage()
        return self._package

    def __str__(self):
        return self.getPackage()
        
    def packageFrom(self, hexFrom):
        self._from = hexFrom

    def packageTo(self, hexTo):
        self._to = hexTo

    def packageHeadAndBody(self, stringHead, xBody = ""):
        if not xBody:
            raise ValueError("Missing arguments")
        
        valHead = bytes.fromhex(stringHead)[0]
        if type(xBody) is int:
            valBody = xBody
        
        if valHead in range(DATA_HEADERS_FROM, DATA_HEADERS_TO) and valBody > MAX_INDIVIDIUAL_DATA:
            #Value sent to device is larger than max(255), so divide and find rest val.
            #Might need update as this is lazy method where it can lose data from truncation
            nrOfMultipleOfMax = int(valBody/MAX_INDIVIDIUAL_DATA)
            arrBody = [str(hex(MAX_INDIVIDIUAL_DATA))[-2:].upper()] * nrOfMultipleOfMax
            restVal = valBody - MAX_INDIVIDIUAL_DATA*nrOfMultipleOfMax
            if restVal > 0:
                arrBody.append(str(hex(int(valBody - MAX_INDIVIDIUAL_DATA*nrOfMultipleOfMax)))[-2:].upper())
            arrHead = [stringHead]*len(arrBody)
            print(arrBody)
            self.packageHeadArrAndBodyArrAdd(arrHead, arrBody)
        elif type(xBody) is int:
            self._heads.append(stringHead)
            self._bodys.append(str(hex(xBody))[-2:].upper())
        elif type(xBody) is str:
            self._heads.append(stringHead)
            self._bodys.append(xBody)
        else:
            raise ValueError('Body was bad')
        
    def packageHeadArrAndBodyArrAdd(self, valHeadArr, valBodyArr):
        if len(self._heads) is not len(self._bodys):
            raise ValueError('recieved heads or bodys is not same size')
        self._heads = self._heads + valHeadArr
        self._bodys = self._bodys + valBodyArr

    def packageHeadArrAndBodyArr(self, valHeadArr, valBodyArr):
        if len(self._heads) is not len(self._bodys):
            raise ValueError('recieved heads or bodys is not same size')
        self._heads = valHeadArr
        self._bodys = valBodyArr

    def readyPackage(self):
        try:
            self._package = self._package + self._from
            self._package = self._package + self._to
            for x in range(len(self._heads)):
                self._package = self._package + self._heads[x] + self._bodys[x]
        except:
            print("Package could not become ready")

