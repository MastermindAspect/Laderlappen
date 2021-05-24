import websocket
import json
try:
    import thread
except ImportError:
    import _thread as thread
import time

#globals
g_messageReceived = False
g_message = ""
g_connected = False
g_appConnected = True

def on_message(ws, message):
    global g_messageReceived
    global g_message
    global g_connected
    global g_appConnected
    try:
        if message:
            if (message == 'ping'):
                ws.send("ping")
                g_connected = True
            elif (message == "Disconnected"):
                g_connected = False
            elif message == "App Not Connected":
                g_appConnected = False
            elif message == "App Connected":
                g_appConnected = True
            else:
                g_messageReceived = True
                g_message = message
                #print(message)
                #print("Message: {}".format(message))
    except:
        print("Message is empty")
    finally:
        #print("Message: {}".format(message))
        pass
        
            


def on_error(ws, error):
    print(error)


def on_close(ws):
    print("### closed ###")


def on_open(ws):
    def run(*args):
        global g_connected
        ws.send("Raspberry")
        print("connected")
        g_connected = True
    #thread.start_new_thread(run, ())
    run()

class WebSocket:
    def __init__(self):
        self.HOST = "212.25.137.72" #change this to the right HOST adress
        self.PORT = "1337"
        self.ws = websocket.WebSocketApp("ws://{}:{}".format(self.HOST,self.PORT),
                                on_open=on_open,
                                on_message=on_message,
                                on_error=on_error,
                                on_close=on_close)
        
    
    def startSocket(self):
        while True:
            self.ws.run_forever()

    def sendMessage(self, message):
        self.ws.send(message)

    def getIfMessageReceived(self):
        return g_messageReceived
    
    def getMessage(self):
        global g_messageReceived
        global g_message
        msg = g_message
        g_message = ""
        g_messageReceived = False
        return msg

    def connected(self):
        return g_connected
    
    def appConnected(self):
        return g_appConnected
    


#this is used to test the class
#remove if this class is to be used in a seperate file
if __name__ == "__main__":
    _websocket = WebSocket()
    _websocket.startSocket()