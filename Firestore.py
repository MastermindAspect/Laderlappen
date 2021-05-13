import firebase_admin 
from firebase_admin import credentials
from firebase_admin import firestore
from datetime import datetime

class Firestore:

    def __init__(self):
        self.db = None
        self.cred = credentials.Certificate("./batmanCredentials.json") 
        self.default = firebase_admin.initialize_app(self.cred)
        self.batmanApp = firebase_admin.initialize_app(self.cred, name="batman")
        self.db = firestore.client()
        self.ongoingId = None
        self.time = str(datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
        self.pathString = self.db.collection(u"mowerData").document(u"{}".format(self.time))

    def initNewSession(self):
        self.pathString.set({"time":self.time})

    def uploadPositionData(self,x,y,collision = False, onLine=True):
        jsonData = {"x": x, "y": y, "collision": collision, "onLine": onLine}
        if self.ongoingId:
                self.db.collection(u"mowerData").document(u"{}".format(self.ongoingId)).collection(u"path").document().set(jsonData)
        else:
            self.initNewSession()
            self.pathString.collection(u"path").document().set(jsonData)
            self.ongoingId = self.time

    def uploadUltrasonicData(self,data):
        jsonData = {"value": data}
        if self.ongoingId:
                self.db.collection(u"mowerData").document(u"{}".format(self.ongoingId)).collection(u"ultraSonicData").document().set(jsonData)
        else:
            self.db.collection(u"mowerData").document(u"{}".format(self.time)).collection(u"ultraSonicData").document().set(jsonData)
            self.ongoingId = self.time

    def uploadLineFollowerData(self,data):
        jsonData = {"value": data}
        if self.ongoingId:
                self.db.collection(u"mowerData").document(u"{}".format(self.ongoingId)).collection(u"lineFollwingData").document().set(jsonData)
        else:
            self.db.collection(u"mowerData").document(u"{}".format(self.time)).collection(u"lineFollowingData").document().set(jsonData)
            self.ongoingId = self.time

    def startNewSession(self):
        self.ongoingId = None
        
    def testFIrestore(self):
        self.uploadPositionData(4,4)
        self.uploadPositionData(4,6)
        self.uploadPositionData(3,4)
        self.uploadLineFollowerData(3)
        self.uploadUltrasonicData(5)

if __name__ == "__main__":
    _firestore = Firestore()
    _firestore.testFIrestore()
