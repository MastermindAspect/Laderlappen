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
        self.pathString = None 

    def initNewSession(self):
        self.time = str(datetime.now().strftime("%Y-%m-%d %H:%M:%S")) 
        self.ongoingId = None
        self.updatePathString(self.time)

    def uploadPositionData(self,x,y,collision = False, onLine=True):
        jsonData = {"x": x, "y": y, "collision": collision, "onLine": onLine}
        if self.ongoingId:
            self.db.collection(u"mowerData").document(u"{}".format(self.ongoingId)).collection(u"path").document().set(jsonData)
        else:
            self.updatePathString(self.time)
            self.pathString.collection(u"path").document().set(jsonData)
            self.pathString.set({"time":self.time})
            self.ongoingId = self.time

    def updatePathString(self,time):
        self.pathString = self.db.collection(u"mowerData").document(u"{}".format(time))  
    
    def setNewCollectionTime(self,newTime):
        self.time = str(newTime)
        self.ongoingId = None
        self.pathString = self.updatePathString(self.time)
    
    def getCurrentTime(self):
        if self.pathString:
            ref = self.db.collection(u"mowerData")
            query = ref.order_by(u'time', direction=firestore.Query.DESCENDING).limit(1)
            result = query.stream()
            return str(result)
        else:
            return None
    
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
    
