from MongoDB import MongoDB
from Firestore import Firestore
import socket
import urllib.request, urllib.error
class DataHandler:

    def __init__(self):
        self.localDb = MongoDB()
        self.firestore = Firestore()


    def testStoreMowerPosition(self):
        self.localDb.closeSession()
        while True:
            valueX = input("Enter X: \n")
            valueY = input("Enter Y: \n")
            self.storeMowerPath(valueX, valueY)


    def deviceHasWifiAvailable(self):
        # this function find out whether wifi is working or not 
        # by pinging a random website and see if response gets back without exception
        # when exception is raised then it means no internet are available
        socket.setdefaulttimeout( 23 )  # timeout in seconds
        url = 'http://google.com/'
        try :
            response =  urllib.request.urlopen( url )
        except urllib.error.URLError as e:
            print ('We failed to reach a server. Reason: {}'.format(str(e.reason))) 
            return False
        else :
            html = response.read()
            print ('got response! Internet is up and running')
            return True
        
    def storeMowerPath(self, x, y, collision = False, onLine=True):
        if(self.deviceHasWifiAvailable()):
            if self.localDb.dataExists(): #local db have data stored that needs to be stored in fb
                sessionData = self.localDb.getAllData()
                #we need to get info of timestamp in the localdb to then create a session with that time stamp in online db
                self.firestore.setNewCollectionTime(sessionData['time'])

                #iterates through all path in local data and then upload each position data to the online db
                for singlePositionInfo in sessionData['path']:
                    self.firestore.uploadPositionData(singlePositionInfo['x'], singlePositionInfo['y'], singlePositionInfo['collision'], singlePositionInfo['onLine'])
                
                #when uploading local data, the last position update is now stored so that it is showin in right order.
                self.firestore.uploadPositionData(x, y, collision, onLine)
                #closing the local session as we have already transfered the data by now
                self.localDb.closeSession()
            else:
                self.firestore.uploadPositionData(x, y, collision, onLine)

        else:
            #we end up being here when no internet connection is available at the moment in the device

            #if session already initialized in firebase and internet cut off but local db is not initialized previously
            if not self.localDb.dataExists() and self.firestore.ongoingId:
                # initialize local db with the timestamp stored in firestore class
                self.localDb.setSessionTime(self.firestore.ongoingId)
            #the block inside this check is executed when localdb is not initialized. Therefore we initialize it 
            elif not self.localDb.ongoingId:
                # intialize a new timestamp because there arent any session started with a timestamp of the current time
                self.localDb.startNewSession()
            self.localDb.uploadPositionData(x, y, collision, onLine)


