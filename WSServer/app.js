"use strict";
// Optional. You will see this name in eg. 'ps' or 'top' command
process.title = 'node-chat';
// Port where we'll run the websocket server
var webSocketsServerPort = 1337;
// websocket and http servers
var webSocketServer = require('websocket').server;
var http = require('http');
/**
 * Global variables
 */
// list of currently connected clients (users)
var clients = [];

var jsonPing = JSON.stringify({ type: 'ping' });
var jsonDisconnect = JSON.stringify({ type: 'disconnected' });


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
  console.log((new Date()) + " Server is listening on port "
    + webSocketsServerPort);
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
  console.log((new Date()) + ' Connection from origin '
    + request.origin + '.');
  // accept connection - you should check 'request.origin' to
  // make sure that client is connecting from your website
  var connection = request.accept(null, request.origin);
  // we need to know client index to remove them on 'close' event
  var index = clients.push(connection) - 1;
  var clientConnections = {
    connection: false
  }
  var userInfo = {
    userName: false,
    index: false
  };

  console.log((new Date()) + ' Connection accepted.');
  // user sent some message
  connection.on('message', function (message) {
    console.log(message)
    if (message.type === 'utf8') { // accept only text
      // first message sent by user is their name
      if (userInfo.userName === false) {
        // remember user name
        clientConnections = {
          connection: false
        }
        userInfo = {
          userName: htmlEntities(message.utf8Data),
          id: index
        };

      } else {
        var messageData = JSON.parse(message.utf8Data)
        console.log(messageData)
        if (messageData.type == "data") {

          // log and broadcast the message
          console.log((new Date()) + ' Received Message from '
            + userInfo.userName + ': ' + message.utf8Data);

          // we want to keep history of all sent messages
          var obj = {
            data: htmlEntities(message.utf8Data),
            author: userInfo.userName,
          };
          // broadcast message to all connected clients
          var json = JSON.stringify({ type: 'message', data: obj });

          // connection.sendUTF(json);
          for (var i = 0; i < clients.length; i++) {
            if (userInfo.id != i) {
              clients[i].sendUTF(json);
            }
          }
        } else if (messageData.type === 'ping') {
          console.log("Did recive ping")
          
          clientConnections = {
            connection: true
          }
          
        }
      }
    }
  });
  var interval = setInterval(function () {
    var intervalId = setTimeout(function () {
      for (var i = 0; i < clients.length; i++) {
        console.log("Sent ping to all members")
        clients[i].sendUTF(jsonPing);
      }
      var intervalId2 = setTimeout(function () {
        if (clientConnections.connection === false && clients.length > 0) {
          console.log("Did not recieve ping",clients.length)
          for (var i = 0; i < clients.length; i++) {
            clients[i].sendUTF(jsonDisconnect);
          }
        }
        clientConnections = {
          connection: false
        }
      }, 1500)
    }, 1500)
  }, 3000)


  // user disconnected
  connection.on('close', function (connection) {
    if (userInfo.userName !== false) {

      console.log((new Date()) + " Peer "
        + userInfo.userName + " disconnected.");
      // remove user from the list of connected clients
      clients.splice(index, 1);
    }
  });
});