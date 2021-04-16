package com.example.laderlappenlawnmower

import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_automowercontroller.*

class AutomowerControllerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_automowercontroller)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.hide()
        }

        //send initial command to bluetooth that we are starting with manual driving
        BluetoothConnectionHandler.send("Auto: Off".toByteArray().toUByteArray())

        BluetoothConnectionHandler.onDisconnect.add {
            val intent = Intent(this, MainActivity::class.java)
            Toast.makeText(this,"Bluetooth disconnected!", Toast.LENGTH_SHORT)
            startActivity(intent)
        }

        BluetoothConnectionHandler.onMessage.put(1){
            when (it.toString()){
                "collision" -> {
                    statusButtonLight.background = getDrawable(R.drawable.circlered)
                }
                "no-collision" ->{
                    statusButtonLight.background = getDrawable(R.drawable.circlegreen)
                }
            }
        }

        buttonHistory.setOnClickListener {}

        buttonArrowUp.setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                view.performClick()
                when(motionEvent.action){
                    MotionEvent.ACTION_DOWN -> {
                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                        BluetoothConnectionHandler.send("Forward".toByteArray().toUByteArray())
                    }
                    MotionEvent.ACTION_UP -> {
                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.MULTIPLY
                        BluetoothConnectionHandler.send("Forward-stop".toByteArray().toUByteArray())
                    }
                }
                return view.onTouchEvent(motionEvent) ?: true
            }
        })

        buttonArrowDown.setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                view.performClick()
                when(motionEvent.action){
                    MotionEvent.ACTION_DOWN -> {
                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                        BluetoothConnectionHandler.send("Backwards".toByteArray().toUByteArray())
                    }
                    MotionEvent.ACTION_UP -> {
                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.MULTIPLY
                        BluetoothConnectionHandler.send("Backwards-stop".toByteArray().toUByteArray())
                    }
                }
                return view.onTouchEvent(motionEvent) ?: true
            }
        })

        buttonArrowLeft.setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                view.performClick()
                when(motionEvent.action){
                    MotionEvent.ACTION_DOWN -> {
                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                        BluetoothConnectionHandler.send("Left".toByteArray().toUByteArray())
                    }
                    MotionEvent.ACTION_UP -> {
                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.MULTIPLY
                        BluetoothConnectionHandler.send("Left-stop".toByteArray().toUByteArray())
                    }
                }
                return view.onTouchEvent(motionEvent) ?: true
            }
        })

        buttonArrowRight.setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                view.performClick()
                when(motionEvent.action){
                    MotionEvent.ACTION_DOWN -> {
                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                        BluetoothConnectionHandler.send("Right".toByteArray().toUByteArray())
                    }
                    MotionEvent.ACTION_UP -> {
                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.MULTIPLY
                        BluetoothConnectionHandler.send("Right-stop".toByteArray().toUByteArray())
                    }
                }
                return view.onTouchEvent(motionEvent) ?: true
            }
        })

        switchAutodrive.setOnClickListener {
            autoDrive(switchAutodrive.isChecked)
        }
    }
    private fun autoDrive(active: Boolean){
        /*change view by disabling
            * Status textView
            * Mower Position textView
            * Driving arrows
        Then make some clean design showing that the mower is operating automatic
        */
        if (active){
            BluetoothConnectionHandler.send("Auto: On".toByteArray().toUByteArray())
            buttonArrowRight.isVisible = false
            buttonArrowLeft.isVisible = false
            buttonArrowUp.isVisible = false
            buttonArrowDown.isVisible = false
            statusTextView.isVisible = false
            mowerPositionTextView.isVisible = false
            mowerPositionTitle.isVisible=false
            statusButtonLight.isVisible=false
            mowerPositionXCoordinate.isVisible=false
            mowerPositionYCoordinate.isVisible=false
        }
        else {
            BluetoothConnectionHandler.send("Auto: Off".toByteArray().toUByteArray())
            buttonArrowRight.isVisible = true
            buttonArrowLeft.isVisible = true
            buttonArrowUp.isVisible = true
            buttonArrowDown.isVisible = true
            statusTextView.isVisible = true
            mowerPositionTextView.isVisible = true
            mowerPositionTitle.isVisible=true
            statusButtonLight.isVisible=true
            mowerPositionXCoordinate.isVisible=true
            mowerPositionYCoordinate.isVisible=true
        }
    }
}