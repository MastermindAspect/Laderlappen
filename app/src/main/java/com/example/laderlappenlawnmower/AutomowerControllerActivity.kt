package com.example.laderlappenlawnmower

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class AutomowerControllerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_automowercontroller)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.hide()
        }
    }
}