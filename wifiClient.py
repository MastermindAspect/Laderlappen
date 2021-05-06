import websocket
import json
try:
    import thread
except ImportError:
    import _thread as thread
import time


def on_message(ws, message):
    try:
        messageLoad = json.loads(message)
        if (messageLoad['type'] == 'ping'):
            data = {"type": "ping"}
            ws.send(json.dumps(data))
        elif (messageLoad["type"] == "disconnected"):
            print("disconnected")
    except:
        print("Message doesnt have a type.")
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


if __name__ == "__main__":
    ws = websocket.WebSocketApp("ws://212.25.137.196:1337",
                                on_open=on_open,
                                on_message=on_message,
                                on_error=on_error,
                                on_close=on_close)
    ws.run_forever()
