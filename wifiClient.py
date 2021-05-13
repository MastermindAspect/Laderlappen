import websocket
import json
try:
    import thread
except ImportError:
    import _thread as thread
import time

def on_message(ws, message):
        try:
            if message:
                if (message == 'ping'):
                    ws.send("ping")
                elif (message == "disconnected"):
                    #Send data to arduino indicating that we are now
                    #disconnected from the App meaning we go AutoDrive
                    pass
        except:
            print("Message is empty")
        finally:
            print("Message: {}".format(message))


def on_error(ws, error):
    print(error)


def on_close(ws):
    print("### closed ###")


def on_open(ws):
    def run(*args):
        ws.send("Raspberry")
    thread.start_new_thread(run, ())

class WebSocket:
    def __init__(self):
        self.HOST = "192.168.0.93" #change this to the right HOST adress
        self.PORT = "1337"
        self.ws = websocket.WebSocketApp("ws://{}:{}".format(self.HOST,self.PORT),
                                on_open=on_open,
                                on_message=on_message,
                                on_error=on_error,
                                on_close=on_close)

    
    def startSocket(self):
        self.ws.run_forever()


#this is used to test the class
#remove if this class is to be used in a seperate file
if __name__ == "__main__":
    _websocket = WebSocket()
    _websocket.startSocket()