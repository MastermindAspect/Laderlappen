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
        val socket = WifiClient("ws://212.25.139.39:1337")
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
            runOnUiThread {
                Toast.makeText(this, "Raspberry is connected to WebServer... Redirecting...",Toast.LENGTH_SHORT).show()
                _loadingDialog.dismissDialog()
                val intent = Intent(this, AutomowerControllerActivity::class.java)
                startActivity(intent)
            }
        }

        //Register a listener for when the App connects to the WebServer
        socket.onConnectWebServer.add {
            runOnUiThread {
                socket.send("","",true)
                _loadingDialog.dismissDialog()
                _loadingDialog.startLoadingAnimation("Waiting for Raspberry")
                Toast.makeText(this, "Connected to WebServer",Toast.LENGTH_SHORT).show()
            }
        }

        //Register a listener for when the App disconnects to the WebServer
        socket.onDisconnectWebServer.add{
            runOnUiThread {
                Toast.makeText(this, "Disconnected from WebServer",Toast.LENGTH_SHORT).show()
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
            _loadingDialog.startLoadingAnimation("Connecting to WebServer")
            socket.connect()
        }
        catch (e: Exception){
            _loadingDialog.dismissDialog()
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