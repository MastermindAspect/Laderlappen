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
    var onConnectWebServer = ArrayList<() -> Unit>()
    var onDisconnectWebServer = ArrayList<() -> Unit>()
    var onConnectRaspberry = ArrayList<() -> Unit>()
    var onDisconnectRaspberry = ArrayList<() -> Unit>()
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
    var onMessage = mutableMapOf<String, (String) -> Unit>()

    private var socket = object : WebSocketClient(URI(uri)){
        override fun onOpen(handshakedata: ServerHandshake?) {
            isConnectedToWebServer = true
        }

        override fun onMessage(message: String?) {

            if(message != null){
                if (message == "ping") send("ping")
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

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            isConnectedToWebServer = false
        }

        override fun onError(e: Exception?) {
            e?.printStackTrace()
        }
    }

    fun connect(){
        if(isConnectedToWebServer){
            throw Exception("You are already connected.")
        }
        socket.connect()
    }

    fun disconnect(){
        if(!isConnectedToWebServer){
            throw Exception("You are already disconnected.")
        }
        socket.close()
    }

    fun send(head: String, body: String, initSend : Boolean = false){
        if(!isConnectedToWebServer || !raspberryIsConnected){
            throw Exception("Could not send message.")
        }
        if (initSend){
            socket.send("App")
        }
        else {
            val message = "0301" + head + body
            socket.send(message)
        }
    }
}