from threading import local
from DataHandler import DataHandler

from usbcommunicator import UsbCommunicator
from wifiClient import WebSocket
from protocolhandler import ProtocolHandler
try:
    import thread
except ImportError:
    import _thread as thread


#Defines
TO_ARDUINO = "00"
TO_RASPBERRY = "01"
TO_BACKEND = "02"
TO_APP = "03"
HEAD_EVENT = "10"
HEAD_POSITION_X = "11"
HEAD_POSITION_Y = "12"
HEAD_ULTRASONIC_DATA = "13"
HEAD_LINE_FOLLOWER_DATA = "14"
HEAD_DRIVE_STATE = "15"
HEAD_DRIVE_COMMAND = "16"
BODY_COLLISION = "20"
BODY_ON_THE_LINE = "21"
BODY_AUTOMATIC_DRIVING_ON = "22"
BODY_MANUAL_DRIVING_ON = "23"



usbUno = UsbCommunicator(baudRate = 115200, portNumber = 0)
protocol = ProtocolHandler()
dataHandler = DataHandler()
wifiClient = WebSocket()
latestSessionTimeStamp = None


if __name__ == "__main__":
	thread.start_new_thread(wifiClient.startSocket, ())

	#msg = True

	while True:
		# Code for testing to send message to app, should be removed
		# if wifiClient.connected() and msg:
		# 	protocol.packageTo(TO_APP)
		# 	protocol.packageFrom(TO_ARDUINO)
		# 	protocol.packageHeadAndBody(HEAD_EVENT, BODY_COLLISION)
		# 	message = protocol.getPackage()
		# 	message += ">"
		# 	print(message)
		# 	wifiClient.sendMessage(message)
		# 	protocol.reset()
		# 	msg = False

	 	#If message is received from app, give that message to the arduino
		if wifiClient.getIfMessageReceived():
	 		print(wifiClient.getMessage())
	 		#usbUno.send(wifiClient.getMessage())

	# 	#if disconnected from the app, set arduino in auto driving mode.	
		if not wifiClient.connected():
			protocol.packageTo(TO_ARDUINO)
			protocol.packageFrom(TO_RASPBERRY)
			protocol.packageHeadAndBody(HEAD_DRIVE_STATE, BODY_AUTOMATIC_DRIVING_ON)
			message = protocol.getPackage()
			#usbUno.send(message)

		#If a message is received via serial, check the message and give it to the right recipients. 
		message = usbUno.readGetTry()
		if message != "" and protocol.unpackage(message):

			protocol.unpackage(message)

			to = protocol.getTo()
			if to == TO_ARDUINO:
				pass
			elif to == TO_RASPBERRY:
				pass
			elif to == TO_BACKEND:
				head, body = protocol.getHeadAndBody()
				dic = {}
				collision = False
				onTheLine = False
				for x in range(len(head)):
					if body[x] == BODY_COLLISION:
						collision = True
					if body[x] == BODY_ON_THE_LINE:
						onTheLine = True
					try:
						dic[head[x]] = dic[head[x]]+bytes.fromhex(body[x])[0]
					except:
						dic[head[x]] = bytes.fromhex(body[x])[0]

				dataHandler.storeMowerPath(dic[HEAD_POSITION_X], dic[HEAD_POSITION_Y], collision, onTheLine)

		 		if collision:
		 			#TODO check in what way the app want a collision message
		 			wifiClient.sendMessage()

		 		protocol.reset()
				
		 	elif to == TO_APP:
		 		pass

		
	


