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
                print(message)
            elif message == "App Not Connected":
                g_appConnected = False
                print(message)
            elif message == "App Connected":
                g_appConnected = True
                print(message)
            else:
                g_messageReceived = True
                g_message = message
    except:
        print("Message is empty")
    finally:
        pass

def on_error(ws, error):
    global g_connected
    print("on_error")
    print(error)
    g_connected = False


def on_close(ws):
    global g_connected
    print("### closed ###")
    g_connected = False


def on_open(ws):
    def run(*args):
        global g_connected
        ws.send("Raspberry")
        print("connected")
        g_connected = True
    run()

class WebSocket:
    def __init__(self):
        self.HOST = "212.25.137.13" #change this to the right HOST adress
        self.PORT = "1337"
        self.ws = websocket.WebSocketApp("ws://{}:{}".format(self.HOST,self.PORT),
                                on_open=on_open,
                                on_message=on_message,
                                on_error=on_error,
                                on_close=on_close)
    
    
    def startSocket(self):
        while True:
            self.ws.run_forever(ping_interval= 5, ping_timeout = 2)

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