package com.example.laderlappenlawnmower

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.nio.ByteBuffer
import java.util.ArrayList
import kotlin.properties.Delegates

class WifiClient(uri: String) {
    // Event that is fired when the app connects to the web socket server.
    var onConnectWebServer = ArrayList<() -> Unit>()

    // Event that is fired when the app disconnects from the web socket server.
    var onDisconnectWebServer = ArrayList<() -> Unit>()

    // Event that is fired when both the app and the raspberry have connected to the web socket server.
    var onConnectRaspberry = ArrayList<() -> Unit>()

    // Event that is fired when either the app or the raspberry disconnect from the web socket server.
    var onDisconnectRaspberry = ArrayList<() -> Unit>()

    // Boolean with "observable" that is called when the value changes. The observable calls the respective events mentioned above.
    var raspberryIsConnected: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if(oldValue != newValue){
            if(newValue){
                onConnectRaspberry.forEach { it() }
            }
            else{
                onDisconnectRaspberry.forEach { it() }
            }
        }
    }
    var isConnectedToWebServer: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if(oldValue != newValue){
            if(newValue){
                onConnectWebServer.forEach { it() }
            }
            else{
                onDisconnectWebServer.forEach { it() }
            }
        }
    }

    // Event that is called when a message is received. The event provides the message body (value) for the respective message head (key).
    var onMessage = mutableMapOf<String, (String) -> Unit>()

    // The main socket variable that is used to communcicate with the web socket server.
    // Uses the "uri" variable in the class constructor at the top.
    private var socket = object : WebSocketClient(URI(uri)){
        // Called when the socket successfully connects to the web socket server.
        override fun onOpen(handshakedata: ServerHandshake?) {
            isConnectedToWebServer = true
        }

        // Called when a message is recieved from the web socket server.
        // The following function retrieves the head and body as described in the protocol in our GitHub wiki page.
        override fun onMessage(message: String?) {

            if(message != null){
                if (message == "ping") send("ping") // We use a ping to make sure we are still connected.
                else if(message == "Raspberry Connected") raspberryIsConnected = true
                else if(message == "disconnected") raspberryIsConnected = false
                else {
                    val from = message[0].toString() + message[1].toString()
                    val to = message[2].toString() + message[3].toString()

                    if(from == "00" && to == "03"){
                        var i = 4
                        while(true){
                            val head = message[i].toString() + message[i + 1].toString()
                            i += 2
                            val body = message[i].toString() + message[i + 1].toString()
                            i += 2

                            onMessage.forEach { entry ->
                                if(entry.key == head){
                                    entry.value(body)
                                }
                            }

                            if(message[i] == '>'){
                                break
                            }
                        }
                    }
                }

            }
        }

        // Called when we get disconnected from the web socket server.
        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            isConnectedToWebServer = false
        }

        // Called when the socket raises an exception.
        override fun onError(e: Exception?) {
            e?.printStackTrace()
        }
    }

    // Attempts to connect to the web socket server.
    fun connect(){
        if(isConnectedToWebServer){
            throw Exception("You are already connected.")
        }
        socket.connect()
    }

    // Attempts to disconnect from the web socket server.
    fun disconnect(){
        if(!isConnectedToWebServer){
            throw Exception("You are already disconnected.")
        }
        socket.close()
    }

    // Sends a message to the web socket server.
    // The function first checks if both the app and the raspberry are actually connected before trying to send.
    // The function will also send the message "App" the first time. This is to help the server identify this web socket client as the app.
    fun send(head: String, body: String, initSend : Boolean = false){
        if(!isConnectedToWebServer || !raspberryIsConnected){
            throw Exception("Could not send message.")
        }
        if (initSend){
            socket.send("App")
        }
        else {
            // The message is built based on the protocol described in our protocol.
            val message = "0301" + head + body
            socket.send(message)
        }
    }
}