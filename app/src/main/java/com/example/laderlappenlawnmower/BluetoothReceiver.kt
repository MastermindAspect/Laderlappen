package com.example.laderlappenlawnmower

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityCompat

class BluetoothReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        val action = intent.action
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
            )
            when (state) {
                BluetoothAdapter.STATE_OFF -> bluetoothWasTurnedOff()
            }
        }
        else if (action == BluetoothDevice.ACTION_FOUND) {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            if(device != null){
                bluetoothDeviceFound(device)
            }
        }
    }

    fun bluetoothWasTurnedOff(){
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        ActivityCompat.startActivityForResult(Globals.currentActivity, enableBtIntent, BluetoothConnectionHandler.REQUEST_ENABLE_BT, null)
    }

    fun bluetoothDeviceFound(device: BluetoothDevice){
        if(!device.address.isNullOrEmpty()){
            Log.d("Device address", device.address)
        }
        if(!device.name.isNullOrEmpty()){
            Log.d("Device name", device.name)
        }
        if(device.address == Globals.arduinoMAC){
            Globals.btAdapter?.cancelDiscovery()
            BluetoothConnectionHandler.connectToDevice(device){}
        }
    }

}