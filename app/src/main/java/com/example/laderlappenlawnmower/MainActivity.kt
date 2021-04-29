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

class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var permissionChecker: PermissionChecker
        lateinit var _loadingDialog : LoadingDialog
        val connectedStatus = BluetoothConnectionHandler.isConnected
        val actionFilter1 = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val actionFilter2 = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        val actionFilter3 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        val actionFilter4 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionChecker = PermissionChecker(this)
        _loadingDialog = LoadingDialog(this@MainActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.hide()
        }

        registerReceiver(BluetoothReceiver(), actionFilter1)
        registerReceiver(BluetoothReceiver(), actionFilter2)
        registerReceiver(BluetoothReceiver(), actionFilter3)
        registerReceiver(BluetoothReceiver(), actionFilter4)

        BluetoothConnectionHandler.onConnect.add {
            val intent = Intent(this,AutomowerControllerActivity::class.java)
            //in case of sending data use putExtra()
            startActivity(intent)
        }

        buttonConnect.setOnClickListener {
            permissionChecker.doAfterPermissionsAllowed{
                connectToBluetooth()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        permissionChecker.onActivityResult(requestCode, resultCode, data) // <- Needed for permissionChecker
    }

    private fun connectToBluetooth(){
        _loadingDialog.startLoadingAnimation()
        BluetoothConnectionHandler.connectToArduino {
            _loadingDialog.dismissDialog()
            if (!connectedStatus){
                Toast.makeText(this,"Failed to connect to mower!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults) // <- Needed for permissionChecker
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(BluetoothReceiver());
    }
}


