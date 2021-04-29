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

    /*companion object {
        val autoDriveOn : String = "03001522<"
        val autoDriveOff : String = "03001523<"
        val driveForwardOn : String = "03001630<"
        val driveForwardOff : String = "03001640<"
        val driveRightOn : String = "03001631<"
        val driveRightOff : String = "03001641<"
        val driveDownOn : String = "03001632<"
        val driveDownOff : String = "03001642<"
        val driveLeftOn : String = "03001633<"
        val driveLeftOff : String = "03001643<"
    }*/

    @ExperimentalUnsignedTypes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_automowercontroller)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.hide()
        }

        //send initial command to bluetooth that we are starting with manual driving
        BluetoothConnectionHandler.sendExperimental(15,23)

        BluetoothConnectionHandler.onDisconnect.add {
            val intent = Intent(this, MainActivity::class.java)
            Toast.makeText(this,"Bluetooth disconnected!", Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }

        //change this function
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
                        BluetoothConnectionHandler.sendExperimental(16,30)
                    }
                    MotionEvent.ACTION_UP -> {
                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.MULTIPLY
                        BluetoothConnectionHandler.sendExperimental(16,40)
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
                        BluetoothConnectionHandler.sendExperimental(16,32)
                    }
                    MotionEvent.ACTION_UP -> {
                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.MULTIPLY
                        BluetoothConnectionHandler.sendExperimental(16,42)
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
                        BluetoothConnectionHandler.sendExperimental(16,33)
                    }
                    MotionEvent.ACTION_UP -> {
                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.MULTIPLY
                        BluetoothConnectionHandler.sendExperimental(16,43)
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
                        BluetoothConnectionHandler.sendExperimental(16,31)
                    }
                    MotionEvent.ACTION_UP -> {
                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.MULTIPLY
                        BluetoothConnectionHandler.sendExperimental(16,41)
                    }
                }
                return view.onTouchEvent(motionEvent) ?: true
            }
        })

        switchAutodrive.setOnClickListener {
            autoDrive(switchAutodrive.isChecked)
        }
    }

    @ExperimentalUnsignedTypes
    private fun autoDrive(active: Boolean){
        /*change view by disabling
            * Status textView
            * Mower Position textView
            * Driving arrows
        Then make some clean design showing that the mower is operating automatic
        */
        if (active){
            BluetoothConnectionHandler.sendExperimental(15, 22)
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
            BluetoothConnectionHandler.sendExperimental(15, 23)
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