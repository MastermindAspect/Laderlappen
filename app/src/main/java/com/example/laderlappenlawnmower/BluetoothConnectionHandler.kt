package com.example.laderlappenlawnmower

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import kotlin.properties.Delegates
import java.util.*
import android.bluetooth.BluetoothAdapter

class BluetoothConnectionHandler() {
    companion object {
        private var btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        private val arduinoMAC = "70:C9:4E:BB:A7:60"
        private val arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private lateinit var socket: BluetoothSocket
        private var outputStream: OutputStream? = null
        private var inputStream: InputStream? = null
        private var shouldListen = false

        var onConnect = ArrayList<() -> Unit>()
        var onDisconnect = ArrayList<() -> Unit>()
        var isConnected: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
            if(oldValue != newValue){
                if(newValue == true){
                    onConnect.forEach {
                        doOnMainThread {
                            it()
                        }
                    }
                }
                else{
                    onDisconnect.forEach {
                        doOnMainThread {
                            it()
                        }
                    }
                }
            }
        }
        var onMessage = mutableMapOf<Int, (UByteArray) -> Unit>()

        private fun connectToDevice(device: BluetoothDevice, onFinish: ((Boolean) -> Unit)? = null){
            Thread {
                try {
                    socket = device.createRfcommSocketToServiceRecord(arduinoUUID)
                    socket.connect()
                    Log.d("connection", "succeeded")
                    outputStream = socket.outputStream
                    inputStream = socket.inputStream
                    doOnMainThread {
                        isConnected = true
                        onFinish?.invoke(true)
                    }
                    listener.run()
                } catch(e: Exception){
                    Log.d("connection", "failed: " + e.message)
                    doOnMainThread {
                        isConnected = false
                        onFinish?.invoke(false)
                    }
                }
            }.start()
        }

        private fun listen() {
            val BUFFER_SIZE = 1024
            val buffer = ByteArray(BUFFER_SIZE)

            try {
                val bytesReceived = inputStream!!.read(buffer)
                Log.d("Received", buffer.toUByteArray().contentToString())
                var i = 0
                while(i < bytesReceived){
                    val type = buffer[i].toInt()
                    if(type == 0){
                        break
                    }
                    else{
                        i++
                        var dataLength = buffer[i].toInt()
                        if(dataLength == 0){
                            Log.d("datalength", "is zero")
                            val newBuffer = ByteArray(BUFFER_SIZE)
                            val newBytesReceived = inputStream!!.read(newBuffer)
                            Log.d("newbytesrec", newBytesReceived.toString())
                            dataLength = newBuffer[0].toInt()
                            Log.d("newdatalength", dataLength.toString())
                            for(x in 0..(newBytesReceived - 1)){
                                buffer[i + x] = newBuffer[x]
                            }
                        }
                        val data = ByteArray(dataLength)
                        i++
                        var o = 0
                        while(o < dataLength){
                            data[o] = buffer[i]
                            i++
                            o++
                        }
                        Log.d("Received at the end", buffer.toUByteArray().contentToString())
                        onMessageReceived(type, data.toUByteArray())
                    }
                }
            } catch (e: IOException) {
                doOnMainThread {
                    isConnected = false
                }
                e.printStackTrace()
                shouldListen = false
            }
        }

        // Testing the new communication protocol
        fun listenExperimental(){
            val BUFFER_SIZE = 1024
            val buffer = ByteArray(BUFFER_SIZE)
            try{
                var bytesReceived = inputStream!!.read(buffer)
                while(bytesReceived < 5){
                    bytesReceived += inputStream!!.read(buffer, bytesReceived, BUFFER_SIZE - bytesReceived)
                }

                val from = buffer[0]
                val to = buffer[1]

                if(from.toInt() == 0 && to.toInt() == 3){
                    var i = 2
                    while(true){
                        val head = buffer[i]
                        i++
                        val body = buffer[i]
                        i++
                        // Do something with head and body here (call onMessageReceived)
                        if(buffer[i].toInt() == 62){ // 62 is the ASCII value for the character >
                            break
                        }
                        else if(i + 2 <= bytesReceived){
                            continue
                        }
                        else{
                            var newBytesReceived = 0
                            while(newBytesReceived < 2){
                                newBytesReceived += inputStream!!.read(buffer, (bytesReceived + newBytesReceived), BUFFER_SIZE - (bytesReceived + newBytesReceived))
                            }
                            bytesReceived += newBytesReceived
                        }
                    }
                }
            }
            catch(e: IOException){
                doOnMainThread {
                    isConnected = false
                }
                e.printStackTrace()
                shouldListen = false
            }
        }

        fun send(bytes: UByteArray) {
            if(::socket.isInitialized && outputStream != null && isConnected){
                outputStream!!.write(bytes.toByteArray())
            }
        }

        // Testing the new communication protocol
        fun sendExperimental(head: Int, body: Int){
            if(::socket.isInitialized && outputStream != null && isConnected){
                val buffer = ByteArray(5)
                buffer[0] = 3
                buffer[1] = 0
                buffer[2] = head.toByte()
                buffer[3] = body.toByte()
                buffer[4] = 60.toByte()
                outputStream!!.write(buffer)
            }
        }

        private fun onMessageReceived(type: Int, buffer: UByteArray){
            onMessage.forEach { entry ->
                if(entry.key == type){
                    doOnMainThread {
                        entry.value(buffer)
                    }
                }
            }
        }

        private var listener = Runnable {
            run {
                shouldListen = true
                while(shouldListen){
                    listen()
                }
            }
        }

        // onFinish is called once the connecting process is finished (regardless of whether it successfully connected or not).
        fun connectToArduino(onFinish: (() -> Unit)? = null){
            try{
                val device = btAdapter?.getRemoteDevice(arduinoMAC)!!
                connectToDevice(device){
                    onFinish?.invoke()
                }
            }
            catch(e: Exception){
                onFinish?.invoke()
            }
        }

        private fun doOnMainThread(myFunction: () -> Unit){
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                myFunction()
            }
        }
    }
}