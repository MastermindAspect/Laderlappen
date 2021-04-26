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
        self.time = datetime.now()

    def uploadPositionData(self,x,y):
        jsonData = {"x": x, "y": y}
        if self.ongoingId:
                self.db.collection(u"mowerPositionData").document(u"{}".format(self.ongoingId)).collection(u"path").document().set(jsonData)
        else:
            self.db.collection(u"mowerPositionData").document(u"{}".format(self.time)).collection(u"path").document().set(jsonData)
            self.ongoingId = self.time

    def uploadUltrasonicData(self,data):
        jsonData = {"value": data}
        if self.ongoingId:
                self.db.collection(u"mowerPositionData").document(u"{}".format(self.ongoingId)).collection(u"ultraSonicData").document().set(jsonData)
        else:
            self.db.collection(u"mowerPositionData").document(u"{}".format(self.time)).collection(u"ultraSonicData").document().set(jsonData)
            self.ongoingId = self.time

    def uploadLineFollowerData(self,data):
        jsonData = {"value": data}
        if self.ongoingId:
                self.db.collection(u"mowerPositionData").document(u"{}".format(self.ongoingId)).collection(u"lineFollwingData").document().set(jsonData)
        else:
            self.db.collection(u"mowerPositionData").document(u"{}".format(self.time)).collection(u"lineFollowingData").document().set(jsonData)
            self.ongoingId = self.time

    def startNewSession(self):
        self.time = None
        

if __name__ == "__main__":
    _firestore = Firestore()
    _firestore.uploadPositionData(3,3)
    _firestore.uploadPositionData(4,4)
    _firestore.uploadLineFollowerData(3)
    _firestore.uploadUltrasonicData(5)
