package com.example.myapplication.Utility

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.R
import kotlin.system.exitProcess

class NetworkStateChangeListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            if (!Connectivity.isInternetConnectivityAvailable(context)) {
                showNoInternetDialogue(context, intent)
            }
        }
    }

    private fun showNoInternetDialogue(context: Context, intent: Intent) {
        val noInternetBuilder: AlertDialog.Builder =
            AlertDialog.Builder(context, R.style.AlertDialogTheme)
        val noInternetView: View =
            LayoutInflater.from(context).inflate(R.layout.chech_network_state_layout, null)
        noInternetBuilder.setView(noInternetView)
        val noInternetDialog: AlertDialog = noInternetBuilder.create()
        noInternetDialog.setCanceledOnTouchOutside(false)
        noInternetDialog.setCancelable(false)
        noInternetView.findViewById<Button>(R.id.retry_bt)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    noInternetDialog.dismiss()
                    onReceive(context, intent)
                }
            })
        noInternetView.findViewById<Button>(R.id.exit_application_bt).setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                val activity: Activity = context as Activity
                noInternetDialog.dismiss()
                exitApplication(activity)
            }
        })
        if (noInternetDialog.window != null) {
            noInternetDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        noInternetDialog.show()
    }

    fun exitApplication(activity: Activity) {
        activity.finishAffinity()
    }
}