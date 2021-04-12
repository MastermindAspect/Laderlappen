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
        private val arduinoMAC = "98:D3:11:F8:6A:DA"
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
                    Log.d("connection", "failed")
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
                var bytesReceived = inputStream!!.read(buffer)
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

        fun send(bytes: UByteArray) {
            if(::socket.isInitialized && outputStream != null && isConnected){
                outputStream!!.write(bytes.toByteArray())
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
            val pairedDevices: Set<BluetoothDevice>? = btAdapter?.bondedDevices
            var connected = false
            pairedDevices?.forEach { device ->
                if(device.address == arduinoMAC){
                    connectToDevice(device) { wasSuccessful ->
                        if(wasSuccessful){
                            connected = true
                            onFinish?.invoke()
                        }
                    }
                }
            }
            if(!connected) {
                var bluetoothDeviceFoundListener: ((BluetoothDevice) -> Unit)? = null
                bluetoothDeviceFoundListener = { device ->
                    if(device.address == arduinoMAC) {
                        BluetoothReceiver.stopDiscovery()
                        BluetoothReceiver.onBluetoothDeviceFound.remove(bluetoothDeviceFoundListener)
                        connectToDevice(device)
                    }
                }
                BluetoothReceiver.onBluetoothDeviceFound.add(bluetoothDeviceFoundListener)

                var bluetoothDiscoveryStoppedListener: (() -> Unit)? = null
                bluetoothDiscoveryStoppedListener = {
                    BluetoothReceiver.onBluetoothDiscoveryStopped.remove(bluetoothDiscoveryStoppedListener)
                    onFinish?.invoke()
                }
                BluetoothReceiver.onBluetoothDiscoveryStopped.add(bluetoothDiscoveryStoppedListener)

                BluetoothReceiver.startDiscovery()
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