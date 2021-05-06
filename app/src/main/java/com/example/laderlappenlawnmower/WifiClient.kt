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
    var onConnect = ArrayList<() -> Unit>()
    var onDisconnect = ArrayList<() -> Unit>()
    var isConnected: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if(oldValue != newValue){
            if(newValue){
                onConnect.forEach { it() }
            }
            else{
                onDisconnect.forEach { it() }
            }
        }
    }
    var onMessage = mutableMapOf<String, (String) -> Unit>()

    private var socket = object : WebSocketClient(URI(uri)){
        override fun onOpen(handshakedata: ServerHandshake?) {
            isConnected = true
        }

        override fun onMessage(message: String?) {

            if(message != null){
                if (message == "ping") send("ping")
                else {
                    val from = message[0].toString() + message[1].toString()
                    val to = message[2].toString() + message[3].toString()

                    if(from == "01" && to == "03"){
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

                            if(message[i] == '>' && message[i + 1] == '<'){
                                break
                            }
                        }
                    }
                }

            }
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            isConnected = false
        }

        override fun onError(e: Exception?) {
            e?.printStackTrace()
        }
    }

    fun connect(){
        if(isConnected){
            throw Exception("You are already connected.")
        }
        socket.connect()
        isConnected = true

    }

    fun disconnect(){
        if(!isConnected){
            throw Exception("You are already disconnected.")
        }
        socket.close()
        isConnected = false

    }

    fun send(head: String, body: String, initSend : Boolean = false, onClosing : Boolean = false){
        if(!isConnected){
            throw Exception("You cannot send because you are disconnected.")
        }
        else if (initSend){
            socket.send("App")
        }
        else {
            val message = "0301" + head + body + "<"
            socket.send(message)
        }
    }
}