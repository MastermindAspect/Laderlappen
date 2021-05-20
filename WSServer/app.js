"use strict";
// Optional. You will see this name in eg. 'ps' or 'top' command
process.title = 'node-chat';
// Port where we'll run the websocket server
var webSocketsServerPort = 1337;
// websocket and http servers
var webSocketServer = require('websocket').server;
var http = require('http');
// list of currently connected clients (users)
var clients = [];
var hasBeenCreated = false;
var ping = 'ping';
var jsonDisconnect = JSON.stringify({
	type: 'disconnected'
});
var intervalVariable = undefined;
var timeoutVariable;
var clientConnections = {}
var t1,t2,t3;
var raspberryConnected = false, appConnected = false;

/**
 * Helper function for escaping input strings
 */
function htmlEntities(str) {
	return String(str)
		.replace(/&/g, '&amp;').replace(/</g, '&lt;')
		.replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
/**
 * HTTP server
 */
var server = http.createServer(function (request, response) {
	// Not important for us. We're writing WebSocket server,
	// not HTTP server
});
server.listen(webSocketsServerPort, function () {
	console.log((new Date()) + " Server is listening on port " +
		webSocketsServerPort);
});
/**
 * WebSocket server
 */
var wsServer = new webSocketServer({

	httpServer: server
});
// This callback function is called every time someone
// tries to connect to the WebSocket server
wsServer.on('request', function (request) {
	console.log((new Date()) + ' Connection from origin ' +
		request.origin + '.');
	// accept connection - you should check 'request.origin' to
	// make sure that client is connecting from your website
	var connection = request.accept(null, request.origin);
	// we need to know client index to remove them on 'close' event
	var index = clients.push(connection) - 1;
	var userInfo = {
		userName: "",
		index: false
	};

	console.log((new Date()) + ' Connection accepted.');
	for (var i = 0; i < clients.length; i++) {
		if (!clientConnections["" + i]){
			clientConnections["" + i] = {"pingRetries": 0, "didRecieve": false};
		}
	}
	// user sent some message
	connection.on('message', function (message) {
		if (message.type === 'utf8') {
			console.log(message) // accept only text
			// first message sent by user is their name
			if (userInfo.userName === "") {
				// remember user name
				userInfo = {
					userName: htmlEntities(message.utf8Data),
					id: index
				};
				if (userInfo.userName == "Raspberry") {
					raspberryConnected = true;
				}
				else if (userInfo.userName == "App"){
					appConnected = true;
				}
				sendUpdatedConnection()
			} else if (message.utf8Data === 'ping') {
				console.log("Did recieve ping from: "+ userInfo.id)
				clientConnections[userInfo.id].didRecieve = true
				t2 = clientConnections
			} else {

				// log and broadcast the message
				console.log((new Date()) + ' Received Message from ' +
					userInfo.userName + ': ' + message.utf8Data);

				// we want to keep history of all sent messages
				var obj = {
					data: htmlEntities(message.utf8Data),
					author: userInfo.userName,
				};
				// broadcast message to all connected clients
				for (var i = 0; i < clients.length; i++) {
					if (userInfo.id != i) {
						clients[i].sendUTF(message.utf8Data);
					}
				}
			}
			
			t3 = userInfo
			if (intervalVariable == undefined) newInterval()
		}
	});
	// user disconnected
	connection.on('close', function (connection) {
		if (userInfo.userName !== "") {
			console.log((new Date()) + " Peer " +
				userInfo.userName + " disconnected.");
			// remove user from the list of connected clients
			clients.splice(index, 1);
			delete clientConnections[index];
			if (clients.length < 1){
				clearInterval(intervalVariable)
				clearTimeout(timeoutVariable)
				intervalVariable = undefined
			}
			sendUpdatedConnection()
			console.log("-----------------------------------------------")
			console.log("Remaining clients: " + clients)
			console.log("-----------------------------------------------")
		}
	});
});

function newInterval(){
	//starts intervals that will periodically send a ping message to see if all clients are still connected
	intervalVariable = setInterval(function () {
		if (clients.length > 0){
			for (var i = 0; i < clients.length; i++) {
				clients[i].sendUTF(ping);
			}
			timeoutVariable = setTimeout(function () {
				for (var j = 0; j < clients.length; j++){
					if (clientConnections[j].didRecieve){
						clientConnections[j].didRecieve = !clientConnections[j].didRecieve;
						clientConnections[j].pingRetries = 0
					}
					else {
						if (clientConnections[j].pingRetries >= 3){
							clients[j].close()
							clients.splice(j, 1);
							delete clientConnections[j];
							if (clients.length < 1){
								clearInterval(intervalVariable)
								clearTimeout(timeoutVariable)
								intervalVariable = undefined
							}
							for (var i = 0; i < clients.length; i++) {
								clients[i].sendUTF("Disconnected");
							}
							console.log("Too many failed pings for user: "+ j)
						}
						else{
							clientConnections[j].pingRetries += 1
							console.log("Did not recieve ping for user " + j)
							console.log("Retries: " + clientConnections[j].pingRetries)
						}
					}
				}
			}, 1500);
		}
	}, 3000);
}

function sendUpdatedConnection(){
	for (var i = 0; i < clients.length; i++) {
		if (raspberryConnected){
			clients[i].sendUTF("Raspberry Connected")
		}
		else if (!raspberryConnected){
			clients[i].sendUTF("Raspberry Not Connected")
		}
		else if (appConnected){
			clients[i].sendUTF("App Connected");
		}
		else if (!appConnected){
			clients[i].sendUTF("App Not Connected");
		}
	}
}