package com.example.myapplication.Utility

import android.widget.Button
import com.example.myapplication.R

class ButtonState {
    companion object {
        fun disableButton(v: Button) {
            v.isEnabled=false
            v.setBackgroundResource(R.drawable.custom_button_disable)
        }

        fun enableButton(v: Button) {
            v.isEnabled=true
            v.setBackgroundResource(R.drawable.custom_button)
        }
    }
}