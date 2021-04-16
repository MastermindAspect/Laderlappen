package com.example.laderlappenlawnmower

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BluetoothReceiver : BroadcastReceiver() {
    companion object{
        private var btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        var onBluetoothTurnedOn = ArrayList<() -> Unit>()
        var onBluetoothTurnedOff = ArrayList<() -> Unit>()
        var onBluetoothDeviceFound = ArrayList<(BluetoothDevice) -> Unit>()
        var onBluetoothDiscoveryStarted = ArrayList<() -> Unit>()
        var onBluetoothDiscoveryStopped = ArrayList<() -> Unit>()

        fun startDiscovery(){
            btAdapter?.startDiscovery()
        }

        fun stopDiscovery(){
            btAdapter?.cancelDiscovery()
        }

        fun getDiscoveryState(): Boolean? {
            return btAdapter?.isDiscovering
        }
    }

    override fun onReceive(context: Context?, intent: Intent) {
        val action = intent.action
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            val state = intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.ERROR
            )
            if(state == BluetoothAdapter.STATE_ON){
                onBluetoothTurnedOn.forEach { it() }
            }
            else if(state == BluetoothAdapter.STATE_OFF){
                onBluetoothTurnedOff.forEach { it() }
            }
        }
        else if (action == BluetoothDevice.ACTION_FOUND) {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            if(device != null){
                Log.d("Bluetooth device found", device.address)
                onBluetoothDeviceFound.forEach{ it(device) }
            }
        }
        else if (action == BluetoothAdapter.ACTION_DISCOVERY_STARTED){
            onBluetoothDiscoveryStarted.forEach { it() }
        }
        else if (action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED){
            onBluetoothDiscoveryStopped.forEach { it() }
        }
    }
}