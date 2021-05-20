package com.example.laderlappenlawnmower

import android.app.Activity
import android.app.AlertDialog

class LoadingDialog internal constructor(private val activity: Activity) {
    private lateinit var dialog: AlertDialog
    private var isRunning : Boolean = false

    fun startLoadingAnimation() {
        if (!isRunning){
            val builder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            builder.setView(inflater.inflate(R.layout.custom_dialog,null))
            builder.setCancelable(false)
            isRunning = true
            dialog = builder.create()
            dialog.show()
        }
    }

    fun dismissDialog() {
        isRunning = false
        dialog.dismiss()
    }
}