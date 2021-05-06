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
    companion object {
        fun example(){
            var socket = WifiClient("http://1.2.3.4:8080")
            socket.onConnect.add{
                Log.d("yay", "connected")
                var head = 0x16 // 16 is the "drive command" head
                var body = 0x30 // 30 is the "up pressed" command
                socket.send(head, body)
            }
            socket.onDisconnect.add{
                Log.d("oh no", "disconnected")
            }
            socket.onMessage[0x10] = { body -> // 10 is the "event" head
                if(body == 0x20){ // 20 is the "collision" body
                    Log.d("oh no", "collided")
                }
            }
            socket.connect()
        }
    }

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

        override fun onMessage(message: String?) {}

        override fun onMessage(bytes: ByteBuffer?) {
            if(bytes != null){
                val buffer = bytes.array()
                val from = buffer[0].toInt()
                val to = buffer[1].toInt()

                if(from == 0x01 && to == 0x03){
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

                        if(buffer[i].toInt() == 0x3E && buffer[i + 1].toInt() == 0x3C){ // 3E and 3C is hexadecimal for the > and < characters
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

    fun send(head: Int, body: Int){
        if(!isConnected){
            throw Exception("You cannot send because you are disconnected.")
        }

        val bytes = ByteArray(5)
        bytes[0] = 0x03
        bytes[1] = 0x01
        bytes[2] = head.toByte()
        bytes[3] = body.toByte()
        bytes[4] = 0x3C.toByte() // 3C is hexadecimal for the < character
        socket.send(bytes)
    }
}