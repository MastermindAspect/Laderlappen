from usbcommunicator import UsbCommunicator
from protocolhandler import ProtocolHandler
import time
import numpy as np

usbUno = UsbCommunicator(baudRate = 115200, portNumber = 0)
proto = ProtocolHandler()
proto2 = ProtocolHandler()
proto3 = ProtocolHandler()

proto.packageFrom("01")
proto.packageTo("00")
proto.packageHeadAndBody("11", (255*3))
print(proto)
package = proto.getPackage()
print(package)

#proto2.unpackage("010010240102401024010240102401024010240102401024")
proto2.unpackage("01001024")
print(proto2.getFrom())
print(proto2.getTo())
h, b = proto2.getHeadAndBody()
print(h)
print(proto2.getPackage())


while True:
    msg = usbUno.readGetTry()
    if msg != "":
        if proto3.unpackage(msg):
            print(f"SUCCES: {proto3}")

    else:
        #global aStart
        #usbUno.send("This is a very long message and I hope that all can be transfered so that this is a good indicator of data")
        usbUno.send(proto2.getPackage())
        #pass
