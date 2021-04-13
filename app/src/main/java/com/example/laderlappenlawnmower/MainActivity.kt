package com.example.laderlappenlawnmower

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.util.Log

class MainActivity : AppCompatActivity() {
    val permissionChecker: PermissionChecker = PermissionChecker(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothExample()
    }

    fun bluetoothExample(){
        val actionFilter1 = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val actionFilter2 = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        val actionFilter3 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        val actionFilter4 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        registerReceiver(BluetoothReceiver(), actionFilter1)
        registerReceiver(BluetoothReceiver(), actionFilter2)
        registerReceiver(BluetoothReceiver(), actionFilter3)
        registerReceiver(BluetoothReceiver(), actionFilter4)

        BluetoothConnectionHandler.onConnect.add {
            Log.d("Yay", "Connected successfully")
            BluetoothConnectionHandler.send(UByteArray(1))
        }

        BluetoothConnectionHandler.onDisconnect.add {
            Log.d("Oh no", "Disconnected")
        }

        BluetoothConnectionHandler.onMessage.put(1) { data ->
            Log.d("Received a message", "of type 1")
        }

        val connectedStatus = BluetoothConnectionHandler.isConnected

        permissionChecker.doAfterPermissionsAllowed{
            Log.d("Permissions", "were allowed")

            BluetoothConnectionHandler.connectToArduino {
                Log.d("This", "is a test")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        permissionChecker.onActivityResult(requestCode, resultCode, data) // <- Needed for permissionChecker
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults) // <- Needed for permissionChecker
    }
}