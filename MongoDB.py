import pymongo
from datetime import datetime


class MongoDB:
    def __init__(self):
        self.time = str(datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
        self.client = pymongo.MongoClient("mongodb://localhost:27017/")
        self.db = self.client["batman"]
        self.ongoingId = None
        self.mowerDataCollection = self.db["mowerData"]


    def uploadPositionData(self, x, y, collision=False, onLine=True):
        jsonData = {"x": x, "y": y, "collision": collision, "onLine": onLine}
        if self.ongoingId:
            x = self.mowerDataCollection.update_one({"_id":self.ongoingId}, {"$push": {"path": jsonData}})
        else:
            x = self.mowerDataCollection.insert_one({"time": self.time, "path":[jsonData]})
            self.ongoingId = x.inserted_id


    def printDic(self):
        for i in self.mowerDataCollection.find():
            print(i)
    def dropCollection(self):
        self.mowerDataCollection.drop()

    def startNewSession(self):
        self.ongoingId = None

    def testDatabase(self):
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
    