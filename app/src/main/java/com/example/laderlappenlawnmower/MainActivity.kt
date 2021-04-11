package com.example.laderlappenlawnmower

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var _bluetoothAdapter: BluetoothAdapter
        const val REQUEST_ENABLE_BLUETOOTH = 1
        var connectClicked : Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.hide()
        }
        val pm : PackageManager = this.packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
            _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            enableBluetooth()
            connect.setOnClickListener {
                connectClicked = true
                enableBluetooth()
            }
        }
        else {
            Toast.makeText(this, "Your device dose not support Bluetooth!", Toast.LENGTH_SHORT).show()
            connect.setOnClickListener {
                Toast.makeText(this, "Your device dose not support Bluetooth!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                if (_bluetoothAdapter.isEnabled) {
                    Toast.makeText(this, "Bluetooth has been enabled", Toast.LENGTH_SHORT).show()
                    Log.d("BluetoothTest", "$intentData")
                    if (connectClicked){
                        connectToBluetooth()
                        connectClicked = !connectClicked
                    }
                } else {
                    Toast.makeText(this, "Bluetooth has been disabled", Toast.LENGTH_SHORT).show()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth enabling has been canceled", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun enableBluetooth() {
        if (!_bluetoothAdapter.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }
        else if (connectClicked) (
                if (!_bluetoothAdapter.isEnabled) {
                    val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
                }
                else {
                    connectToBluetooth()
                    connectClicked = !connectClicked
                }
                )
    }

    private fun connectToBluetooth(){
        val _loadingDialog : LoadingDialog = LoadingDialog(this@MainActivity) //use this loader to show a loading screen (.startLoadingAnimation & .dismissDialog)
        _loadingDialog.startLoadingAnimation()
        //Connect Bluetooth to the mower here (on another thread than the UI thread)
        //Enable retry button in case the bluetooth disconnects/timeouts
        /*if ("success"){
            _loadingDialog.dismissDialog()
            val intent = Intent(this, NewIntent::class.java).apply {
                putExtra(EXTRA_MESSAGE,message)
            }
            startActivity(intent)
        }
        else {
            _loadingDialog.dismissDialog()
            retryConnection.isVisible = true
        }*/
    }
}
