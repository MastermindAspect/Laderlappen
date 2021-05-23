import pymongo
from datetime import datetime


class MongoDB:
    def __init__(self):
        #initializing the class by creating a database named "batman" 
        #and a collection in the database named "mowerData"
        self.time = str(datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
        self.client = pymongo.MongoClient("mongodb://localhost:27017/")
        self.db = self.client["batman"]
        self.ongoingId = None
        self.mowerDataCollection = self.db["mowerData"]

    
    def uploadPositionData(self, x, y, collision=False, onLine=True):
        #this function will add the position data into the "mowerData" collection
        #the received data will be presented in json format before being inserted
        #if ongoingId is "None" a new document with new ongoingId is inserted to the collection
        #if ongoingId has a value then the document will be updated with new data
        jsonData = {"x": x, "y": y, "collision": collision, "onLine": onLine}
        if self.ongoingId:
            x = self.mowerDataCollection.update_one({"_id":self.ongoingId}, {"$push": {"path": jsonData}})
        else:
            x = self.mowerDataCollection.insert_one({"time": self.time, "path":[jsonData]})
            self.ongoingId = x.inserted_id
        self.printDic()


    def printDic(self):
        #printing the documents stored in "mowerData" collection
        for i in self.mowerDataCollection.find():
            print(i)
    def dropCollection(self):
        #remove the "mowerData" collection
        self.mowerDataCollection.drop()


    def setSessionTime(self, time = ''):
        #set time to specific time
        self.time = time
    
    def dataExists(self):
        #checking if the collection has any data
        print(self.mowerDataCollection.find().count() > 0)
        return self.mowerDataCollection.find().count() > 0

    def getAllData(self):
        #return stored data
        return self.mowerDataCollection.find()[0]
    def closeSession(self):
        #dropping the collection and resetting the ongoingId
        self.dropCollection()
        self.ongoingId = None
    
    def startNewSession(self):
        #start a new session and setting time to current time and ongoingId to None
        self.ongoingId = None
        self.time = str(datetime.now().strftime("%Y-%m-%d %H:%M:%S"))

    def testDatabase(self):
        #running some tests
        _mongo_db.uploadPositionData(1, 2)
        _mongo_db.uploadPositionData(3, 2)
        _mongo_db.uploadPositionData(2, 51)
        _mongo_db.startNewSession()
        _mongo_db.uploadPositionData(500, 12)
        _mongo_db.uploadPositionData(35124, 132)
        _mongo_db.uploadPositionData(2123, 51231)
        _mongo_db.printDic()



if __name__ == "__main__":
    _mongo_db = MongoDB()
    _mongo_db.testDatabase()
    #_mongo_db.dropCollection()
    