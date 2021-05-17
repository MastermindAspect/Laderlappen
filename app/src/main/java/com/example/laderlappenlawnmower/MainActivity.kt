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
        lateinit var _loadingDialog : LoadingDialog
        val socket = WifiClient("ws://212.25.137.67:1337")
        val connectedStatus = socket.isConnected
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        _loadingDialog = LoadingDialog(this@MainActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.hide()
        }

        socket.onConnect.add{
            socket.send("","",true)
            val intent = Intent(this, AutomowerControllerActivity::class.java)
            startActivity(intent)
        }

        socket.onDisconnect.add{
            Log.d("socket", "disconnected")
        }

        socket.onMessage["10"] = { body ->
            if(body == "20"){
                Log.d("oh no", "collided")
            }
        }

        buttonConnect.setOnClickListener {
            connectToWebSocket()
        }
    }

    private fun connectToWebSocket(){
        try {
            _loadingDialog.startLoadingAnimation()
            socket.connect()
        }
        catch (e: Exception){
            Toast.makeText(this,"You are already connected!",Toast.LENGTH_SHORT)
        }
        finally {
            if (socket.isConnected)Toast.makeText(this,"Connected to Server!",Toast.LENGTH_SHORT)
            else Toast.makeText(this,"Could not connect to Server!",Toast.LENGTH_SHORT)
            _loadingDialog.dismissDialog()
        }
    }

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


