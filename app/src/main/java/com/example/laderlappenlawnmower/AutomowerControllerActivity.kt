package com.example.laderlappenlawnmower

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_automowercontroller.*


class AutomowerControllerActivity : AppCompatActivity() {

    companion object {
        val socket = MainActivity.socket
    }

    @ExperimentalUnsignedTypes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_automowercontroller)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.hide()
        }

        //send initial command to bluetooth that we are starting with manual driving
        socket.send("15", "23")

        socket.onDisconnect.add {
            val intent = Intent(this, MainActivity::class.java)
            Toast.makeText(this, "Socket disconnected!", Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }

        socket.onMessage["10"] = { body ->
            if(body == "20"){
                Log.d("oh no", "collided")
                statusButtonLight.background = resources.getDrawable(R.drawable.circlered,theme)
            }
        }

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                checkWifi()
                mainHandler.postDelayed(this, 2000)
            }
        })

        buttonArrowUp.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                view.performClick()
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        checkWifi()

                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                        socket.send("16", "30")
                    }
                    MotionEvent.ACTION_UP -> {
                        checkWifi()

                        buttonArrowUp.backgroundTintMode = PorterDuff.Mode.MULTIPLY
                        socket.send("16", "40")
                    }
                }
                return view.onTouchEvent(motionEvent) ?: true
            }
        })

        buttonArrowDown.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                view.performClick()
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        checkWifi()

                        buttonArrowDown.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                        socket.send("16", "32")
                    }
                    MotionEvent.ACTION_UP -> {
                        checkWifi()

                        buttonArrowDown.backgroundTintMode = PorterDuff.Mode.MULTIPLY
                        socket.send("16", "42")
                    }
                }
                return view.onTouchEvent(motionEvent) ?: true
            }
        })

        buttonArrowLeft.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                view.performClick()
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        checkWifi()

                        buttonArrowLeft.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                        socket.send("16", "33")
                    }
                    MotionEvent.ACTION_UP -> {
                        checkWifi()

                        buttonArrowLeft.backgroundTintMode = PorterDuff.Mode.MULTIPLY
                        socket.send("16", "43")
                    }
                }
                return view.onTouchEvent(motionEvent) ?: true
            }
        })

        buttonArrowRight.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                view.performClick()
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        checkWifi()
                        buttonArrowRight.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                        socket.send("16", "31")
                    }
                    MotionEvent.ACTION_UP -> {
                        checkWifi()
                        buttonArrowRight.backgroundTintMode = PorterDuff.Mode.MULTIPLY
                        socket.send("16", "41")
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
        if (active){
            socket.send("15", "22")
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
            socket.send("15", "23")
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

    private fun checkWifi(){
        if (!isOnline(this)) {
            socket.disconnect()
        }
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }
}