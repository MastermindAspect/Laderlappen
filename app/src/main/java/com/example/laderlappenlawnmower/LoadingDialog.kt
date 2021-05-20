package com.example.laderlappenlawnmower

import android.app.Activity
import android.app.AlertDialog
import android.widget.TextView

class LoadingDialog internal constructor(private val activity: Activity) {
    private lateinit var dialog: AlertDialog
    private var isRunning : Boolean = false

    //Initiates the loading animation and shows it to the screen.
    fun startLoadingAnimation(message: String) {
        if (!isRunning){
            val builder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val content = inflater.inflate(R.layout.custom_dialog,null)
            builder.setView(content)
            val textView : TextView = content.findViewById(R.id.textView)
            textView.text = message
            builder.setCancelable(false)
            isRunning = true
            dialog = builder.create()
            dialog.show()
        }
    }

    // Dismisses the loading animation.
    fun dismissDialog() {
        isRunning = false
        dialog.dismiss()
    }
}