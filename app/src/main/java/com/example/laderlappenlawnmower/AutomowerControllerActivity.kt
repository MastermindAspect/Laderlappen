package com.example.laderlappenlawnmower

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_automowercontroller.*

class AutomowerControllerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_automowercontroller)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.hide()
        }

        buttonHistory.setOnClickListener {

        }

        buttonArrowUp.setOnClickListener {

        }

        buttonArrowDown.setOnClickListener {

        }

        buttonArrowLeft.setOnClickListener {

        }

        buttonArrowRight.setOnClickListener {

        }

        //statusButtonLight.background = getDrawable(R.drawable.circlered)
    }
}