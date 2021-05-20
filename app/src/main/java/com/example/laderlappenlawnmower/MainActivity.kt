package com.example.laderlappenlawnmower

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    companion object {
        // We create a global socket variable that can be used across the app.
        val socket = WifiClient("ws://212:25:137:72:1337")
        lateinit var _loadingDialog : LoadingDialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        _loadingDialog = LoadingDialog(this@MainActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.hide()
        }

        // Register a listener for when the raspberry connects.
        socket.onConnectRaspberry.add{
            Toast.makeText(this, "Raspberry is connected to WebServer... Redirecting...",Toast.LENGTH_SHORT).show()
            // Send the initial "App" message once we connect.
            socket.send("","",true)
            // Dismiss the loading dialog and go to the AutomowerControllerActivity, where we can control the mower.
            _loadingDialog.dismissDialog()
            val intent = Intent(this, AutomowerControllerActivity::class.java)
            startActivity(intent)
        }

        // Register a listener for when the app disconnects from the web socket server.
        socket.onDisconnectWebServer.add{
            Toast.makeText(this, "Disconnected from WebServer",Toast.LENGTH_SHORT).show()
        }

        // Register a listener for then we receive a message with the head "10".
        socket.onMessage["10"] = { body ->
            if(body == "20"){
                Log.d("oh no", "collided")
            }
        }

        // Attempt to connect when the "connect" button is pressed.
        buttonConnect.setOnClickListener {
            connectToWebSocket()
        }
    }

    // Function that attempts to connect to the web socket server and hides loading dialog if successful.
    private fun connectToWebSocket(){
        try {
            _loadingDialog.startLoadingAnimation()
            socket.connect()
            if (socket.isConnectedToWebServer)Toast.makeText(this,"Connected to Server!",Toast.LENGTH_SHORT).show()
            else {
                _loadingDialog.dismissDialog()
                Toast.makeText(this,"Could not connect to Server!",Toast.LENGTH_SHORT).show()
            }
        }
        catch (e: Exception){
            Toast.makeText(this,"You are already connected!",Toast.LENGTH_SHORT).show()
        }
    }

    // Function that disconnects the socket when the app is destroyed/stopped.
    override fun onDestroy() {
        super.onDestroy()
        try {
            socket.disconnect()
        }
        catch (e: Exception){
            Log.d("error", "Exception")
        }
    }
}


//fixa så att varje gång någon connectar till webservern skicka ut nuvarande data angående om raspberry/app är connectade


