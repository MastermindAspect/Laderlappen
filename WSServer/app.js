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
var ping = 'ping';

var intervalVariable = undefined;
var timeoutVariable;
var clientConnections = {}
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
				clientConnections[index] = {"pingRetries": 0, "didRecieve": false, name:userInfo.userName};
				
				if (userInfo.userName == "Raspberry") {
					raspberryConnected = true;
				}
				else if (userInfo.userName == "App"){
					appConnected = true;
				}
				sendUpdatedConnection()
			} else if (message.utf8Data === 'ping') {
				console.log("Did recieve ping from: "+ userInfo.userName)
				clientConnections[index].didRecieve = true
				
			} else {
				if (userInfo.userName == "Raspberry") {
					raspberryConnected = true;
				}
				else if (userInfo.userName == "App"){
					appConnected = true;
				}				
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
			console.log("#-----------------------------------------------#")
			console.log("User "+ userInfo.userName +" disconnected")
			
			clients.splice(index, 1);
			if (clientConnections[index].name == "Raspberry"){
				raspberryConnected = false
			} else if (clientConnections[index].name == "App"){
				appConnected = false
			}
			delete clientConnections[index];
			if (clients.length == 0){
				clearInterval(intervalVariable)
				clearTimeout(timeoutVariable)
				intervalVariable = undefined
			}
			sendUpdatedConnection()
			console.log("Remaining clients: " + clients)
			console.log("#-----------------------------------------------#")
		}
	});
});

function newInterval(){
	//starts intervals that will periodically send a ping message to see if all clients are still connected
	//this ping method gives the client 3 retries before forcing the client to disconnect
	intervalVariable = setInterval(function () {
		if (clients.length > 0){
			for (var i = 0; i < clients.length; i++) {
				clients[i].sendUTF(ping);
			}
			timeoutVariable = setTimeout(function () {
				for (var j = 0; j < clients.length; j++){
					if (clientConnections[j]){
						if (clientConnections[j].didRecieve){
							clientConnections[j].didRecieve = false
							clientConnections[j].pingRetries = 0
						}
						else {
							if (clientConnections[j].pingRetries >= 3){
								if (clientConnections[j].name == "Raspberry"){
									raspberryConnected = false
								} else if (clientConnections[j].name == "App"){
									appConnected = false
								}
								clients[j].close()
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
					
				}
			}, 1500);
		}
	}, 3000);
}

function sendUpdatedConnection(){
	for (var i = 0; i < clients.length; i++) {
		if (clientConnections[i]){		
			if (raspberryConnected){
				if (clientConnections[i].name == "App"){
					clients[i].sendUTF("Raspberry Connected")
				}
			}
			else{
				if (clientConnections[i].name == "App"){
					clients[i].sendUTF("Raspberry Not Connected")
				}		
			}
			if (appConnected){
				if (clientConnections[i].name == "Raspberry"){
					clients[i].sendUTF("App Connected")
				}
			}
			else{
				if (clientConnections[i].name == "Raspberry"){
					clients[i].sendUTF("App Not Connected")
				}
			}
		}
	}
}