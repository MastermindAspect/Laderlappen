package com.example.laderlappenlawnmower
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog

class PermissionChecker(var context: Activity) {

    val REQUEST_ENABLE_BT = 1
    val REQUEST_ENABLE_LOCATION = 2
    var afterPermissionsAllowed: (() -> Unit)? = null

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setMessage(context.getString(R.string.bluetooth_permission_msg))
                    .setPositiveButton(context.getString(R.string.enable_bluetooth)) { _, _ ->
                        requestBluetoothTurnOn()
                    }
                    .setNegativeButton(context.getString(R.string.cancel)) { _, _ -> }
                    .show()
            }
            else {
                if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                    requestLocationAccess()
                }
                else {
                    if(afterPermissionsAllowed != null){
                        afterPermissionsAllowed!!()
                        afterPermissionsAllowed = null
                    }
                }
            }
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_ENABLE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (BluetoothAdapter.getDefaultAdapter()?.isEnabled == false) {
                            requestBluetoothTurnOn()
                        } else {
                            if(afterPermissionsAllowed != null){
                                afterPermissionsAllowed!!()
                                afterPermissionsAllowed = null
                            }
                        }
                    }
                } else {
                    AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setMessage(context.getString(R.string.location_access_permission))
                        .setPositiveButton(context.getString(R.string.allow_location_access)) { _, _ ->
                            requestLocationAccess()
                        }
                        .setNegativeButton(context.getString(R.string.cancel)) { _, _ -> }
                        .show()
                }
                return
            }
        }
    }

    fun doAfterPermissionsAllowed(myAfterPermissionsAllowed: () -> Unit){
        afterPermissionsAllowed = myAfterPermissionsAllowed
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            showNoBtAdapterAlert()
        }
        else if (BluetoothAdapter.getDefaultAdapter()?.isEnabled == false) {
            requestBluetoothTurnOn()
        }
        else {
            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                requestLocationAccess()
            }
            else {
                myAfterPermissionsAllowed()
                afterPermissionsAllowed = null
            }
        }
    }

    fun showNoBtAdapterAlert() {
        AlertDialog.Builder(context)
            .setCancelable(false)
            .setMessage(context.getString(R.string.bluetooth_adapter_error))
            .setPositiveButton(context.getString(R.string.ok)) { _, _ -> }.show()
    }

    fun requestBluetoothTurnOn() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    fun requestLocationAccess() {
        context.requestPermissions(
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_ENABLE_LOCATION
        )
    }
}