package com.example.laderlappenlawnmower

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.util.ArrayList
import kotlin.properties.Delegates

class WifiClient(uri: String) {
    var onConnect = ArrayList<() -> Unit>()
    var onDisconnect = ArrayList<() -> Unit>()
    var isConnected: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if(oldValue != newValue){
            if(newValue == true){
                onConnect.forEach { it() }
            }
            else{
                onDisconnect.forEach { it() }
            }
        }
    }
    var onMessage = mutableMapOf<Int, (Int) -> Unit>()

    private var socket = object : WebSocketClient(URI(uri)){
        override fun onOpen(handshakedata: ServerHandshake?) {
            isConnected = true
        }

        override fun onMessage(message: String?) {
            if(message != null){
                val buffer = message.toByteArray()
                val from = buffer[0].toInt()
                val to = buffer[1].toInt()

                if(from == 1 && to == 3){
                    var i = 2
                    while(true){
                        val head = buffer[i].toInt()
                        i++
                        val body = buffer[i].toInt()
                        i++

                        onMessage.forEach { entry ->
                            if(entry.key == head){
                                entry.value(body)
                            }
                        }

                        if(buffer[i].toInt() == 62){ // 62 is the ASCII value for the character >
                            break
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
    }

    fun disconnect(){
        if(!isConnected){
            throw Exception("You are already disconnected.")
        }

        socket.close()
    }
}