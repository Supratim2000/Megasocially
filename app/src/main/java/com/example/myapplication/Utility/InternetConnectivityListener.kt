package com.example.myapplication.Utility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class InternetConnectivityListener: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context != null && intent != null) {
            if (Connectivity.isInternetConnectivityAvailable(context)) {
                if(FirebaseAuth.getInstance().currentUser != null) {
                    FirebaseDatabase.getInstance().reference.child("presence").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue("online")
                }
            } else {
                if(FirebaseAuth.getInstance().currentUser != null) {
                    FirebaseDatabase.getInstance().reference.child("presence").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue("")
                }
            }
        }
    }
}