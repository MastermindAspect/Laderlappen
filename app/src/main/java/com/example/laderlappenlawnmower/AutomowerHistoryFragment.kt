package com.example.laderlappenlawnmower

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

class AutomowerHistoryFragment: DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_automower_history, container, false)
        //Do logic for the fragment here.

        return rootView

        //Show the dialog in a activity using code as follows
        /*
        button.setOnClickListener {
            val dialog = AutomowerHistoryFragment()
            dialog.show(supportFragmentManager, "mowerHistory")
        }
         */
    }

}