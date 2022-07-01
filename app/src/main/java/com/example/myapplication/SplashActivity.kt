package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import com.facebook.AccessToken
import com.google.android.gms.measurement.module.Analytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_DELAY_IN_MILLIS: Long = 3000L
    private lateinit var appLogoIv: ImageView
    private lateinit var appNameTv: TextView
    private lateinit var poweredByAndroidStudioTv: TextView
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initVariables()
        val currentUser: FirebaseUser? = firebaseAuth.currentUser
        initSplash(currentUser)
    }

    private fun initSplash(currentUser: FirebaseUser?): Unit {
        val facebookAccessToken: AccessToken? = AccessToken.getCurrentAccessToken()
        Handler().postDelayed({
            if((currentUser != null && currentUser.isEmailVerified) || (facebookAccessToken != null && !facebookAccessToken.isExpired)) {
                val splashToMainIntent: Intent = Intent(this, MainActivity::class.java)
                startActivity(splashToMainIntent)
            } else {
                val splashToLoginIntent: Intent = Intent(this, SignIn::class.java)
                startActivity(splashToLoginIntent)
            }
            finish()
        },SPLASH_TIME_DELAY_IN_MILLIS)
    }

    private fun initVariables(): Unit {
        appLogoIv = findViewById(R.id.app_logo_iv)
        appNameTv = findViewById(R.id.app_name_tv)
        poweredByAndroidStudioTv = findViewById(R.id.powered_by_android_studio_tv)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseAuth = FirebaseAuth.getInstance()
    }
}