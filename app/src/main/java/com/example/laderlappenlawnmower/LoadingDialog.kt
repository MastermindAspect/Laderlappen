package com.example.laderlappenlawnmower

import android.app.Activity
import android.app.AlertDialog

class LoadingDialog internal constructor(private val activity: Activity) {
    private var dialog: AlertDialog? = null
    fun startLoadingAnimation() {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.custom_dialog,null))
        builder.setCancelable(false)
        dialog = builder.create()
        dialog!!.show()
    }

    fun dismissDialog() {
        dialog!!.dismiss()
    }
}