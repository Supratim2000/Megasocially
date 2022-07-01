package com.example.myapplication.Utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class Connectivity {
    companion object {
        fun isInternetConnectivityAvailable(context: Context): Boolean {
            val connectivityManager: ConnectivityManager? = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            if(connectivityManager != null) {
                val networkInfo: Array<out NetworkInfo>? = connectivityManager.getAllNetworkInfo()
                if(networkInfo != null) {
                    for(info in networkInfo) {
                        if(info.state ==  NetworkInfo.State.CONNECTED) {
                            return true
                        }
                    }
                }
            }
            return false
        }
    }
}