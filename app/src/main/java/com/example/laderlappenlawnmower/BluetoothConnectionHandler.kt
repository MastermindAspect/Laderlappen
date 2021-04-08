package com.example.laderlappenlawnmower

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import kotlin.properties.Delegates

class BluetoothConnectionHandler() {
    companion object {
        lateinit var socket: BluetoothSocket
        val REQUEST_ENABLE_BT = 1
        val REQUEST_ENABLE_LOCATION = 2
        var outputStream: OutputStream? = null
        var inputStream: InputStream? = null
        var shouldListen = false
        var onConnect = ArrayList<() -> Unit>()
        var onDisconnect = ArrayList<() -> Unit>()
        var connected: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
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

        fun connectToDevice(device: BluetoothDevice, wasSuccessful: (success: Boolean) -> Unit){
            Thread {
                try {
                    socket = device.createRfcommSocketToServiceRecord(Globals.arduinoUUID)
                    socket.connect()
                    outputStream = socket.outputStream
                    inputStream = socket.inputStream
                    doOnMainThread {
                        connected = true
                        wasSuccessful(true)
                    }
                    sendHeartbeatToArduino(true)
                    listener.run()
                } catch(e: Exception){
                    doOnMainThread {
                        connected = false
                        wasSuccessful(false)
                    }
                }
            }.start()
        }

        fun listen() {
            val BUFFER_SIZE = 1024
            val buffer = ByteArray(BUFFER_SIZE)

            try {
                var bytesReceived = inputStream!!.read(buffer)
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
                            val newBuffer = ByteArray(BUFFER_SIZE)
                            val newBytesReceived = inputStream!!.read(newBuffer)
                            dataLength = newBuffer[0].toInt()
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
                        onMessageReceived(type, data.toUByteArray())
                    }
                }
            } catch (e: IOException) {
                doOnMainThread {
                    connected = false
                }
                e.printStackTrace()
                shouldListen = false
            }
        }

        fun send(bytes: UByteArray) {
            if(::socket.isInitialized && outputStream != null && connected){
                outputStream!!.write(bytes.toByteArray())
            }
        }

        fun sendClockInToArduino(){
            val buffer = UByteArray(3)
            buffer[0] = 1.toUByte()
            buffer[1] = 1.toUByte()
            buffer[2] = 1.toUByte()
            send(buffer)
        }

        fun sendClockOutToArduino(){
            val buffer = UByteArray(3)
            buffer[0] = 1.toUByte()
            buffer[1] = 1.toUByte()
            buffer[2] = 0.toUByte()
            send(buffer)
        }

        fun sendWarningToArduino(){
            val buffer = UByteArray(3)
            buffer[0] = 2.toUByte()
            buffer[1] = 1.toUByte()
            buffer[2] = 1.toUByte()
            send(buffer)
        }

        fun sendTimeLeftToArduino(timeLeftInMinutes: Int){
            val myTime = timeLeftInMinutes + 1
            if(myTime <= 255){
                val buffer = UByteArray(3)
                buffer[0] = 6.toUByte()
                buffer[1] = 1.toUByte()
                buffer[2] = myTime.toUByte()
                send(buffer)
            }
            else if(myTime <= 65535){
                val buffer = UByteArray(4)
                buffer[0] = 6.toUByte()
                buffer[1] = 2.toUByte()
                buffer[2] = myTime.shr (8).toUByte()
                buffer[3] = myTime.toUByte()
                send(buffer)
            }
            else{
                val buffer = UByteArray(5)
                buffer[0] = 6.toUByte()
                buffer[1] = 3.toUByte()
                buffer[2] = myTime.shr (16).toUByte()
                buffer[3] = myTime.shr (8).toUByte()
                buffer[4] = myTime.toUByte()
                send(buffer)
            }
        }

        fun sendDatabaseFailureToArduino(){
            val buffer = UByteArray(3)
            buffer[0] = 8.toUByte()
            buffer[1] = 1.toUByte()
            buffer[2] = 1.toUByte()
            send(buffer)
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

        var listener = Runnable {
            run {
                shouldListen = true
                while(shouldListen){
                    listen()
                }
            }
        }

        fun connectToArduino(onTimeout: () -> Unit){
            //"onTimeout" is only called if we were unable to find arduino after 20 seconds of discovery
            val pairedDevices: Set<BluetoothDevice>? = Globals.btAdapter?.bondedDevices
            var foundArduino = false
            pairedDevices?.forEach { device ->
                val mac = device.address // MAC address
                if(mac == Globals.arduinoMAC){
                    foundArduino = true
                    connectToDevice(device) { wasSuccessful ->
                        if(!wasSuccessful){
                            startDiscovery {
                                doOnMainThread(onTimeout)
                            }
                        }
                    }
                }
            }
            if(!foundArduino) {
                startDiscovery {
                    doOnMainThread(onTimeout)
                }
            }
        }

        fun startDiscovery(onArduinoNotFound: () -> Unit){
            Globals.btAdapter?.startDiscovery()
            Handler().postDelayed(
                    {
                        if (!connected) {
                            Globals.btAdapter?.cancelDiscovery()
                            onArduinoNotFound()
                        }
                    },
                    20000 // value in milliseconds
            )
        }

        fun doOnMainThread(myFunction: () -> Unit){
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                myFunction()
            }
        }

        fun onMessage(messageType: Int, listener: (UByteArray) -> Unit){
            onMessage.put(messageType, listener)
        }

        fun sendHeartbeatToArduino(first: Boolean){
            if(first){
                val buffer = UByteArray(3)
                buffer[0] = 7.toUByte()
                buffer[1] = 1.toUByte()
                buffer[2] = 1.toUByte()
                send(buffer)
            }
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                if(connected){
                    val buffer = UByteArray(3)
                    buffer[0] = 7.toUByte()
                    buffer[1] = 1.toUByte()
                    buffer[2] = 1.toUByte()
                    send(buffer)
                    sendHeartbeatToArduino(false)
                }
            }, 5000)
        }
    }

}