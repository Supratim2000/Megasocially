package com.example.myapplication

import android.app.ProgressDialog
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.myapplication.Utility.ButtonState
import com.example.myapplication.Utility.NetworkStateChangeListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ForgotActivity : AppCompatActivity() {
    private lateinit var backIv: ImageView
    private lateinit var emailEt: EditText
    private lateinit var submitBt: Button
    private lateinit var connectivityListener: NetworkStateChangeListener
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var forgotProgressDialogue: ProgressDialog

    override fun onStart() {
        registerNetworkListener()
        super.onStart()
    }

    override fun onDestroy() {
        unregisterNetworkListener()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)
        initVariables()
        setupforgotProgressDialogue()
        ButtonState.enableButton(submitBt)

        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        backIv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                finish()
            }
        })

        submitBt.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(emailEt.text.toString().trim().isNotEmpty()) {
                    forgotProgressDialogue.show()
                    ButtonState.disableButton(submitBt)
                    sendPasswordResetEmail(emailEt.text.toString().trim())
                } else {
                    Toast.makeText(this@ForgotActivity, "Email field can't be empty", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupforgotProgressDialogue() {
        forgotProgressDialogue.setTitle("Reset in progress")
        forgotProgressDialogue.setMessage("please wait while we are sending password reset email")
    }

    private fun sendPasswordResetEmail(emailString: String) {
        firebaseAuth.sendPasswordResetEmail(emailString).addOnCompleteListener {task->
            forgotProgressDialogue.dismiss()
            ButtonState.enableButton(submitBt)
            if(task.isSuccessful) {
                Toast.makeText(applicationContext, "password reset email sent to provided email address", Toast.LENGTH_SHORT).show()
                emailEt.text.clear()
            } else {
                Toast.makeText(applicationContext, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun registerNetworkListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(
                connectivityListener,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    fun unregisterNetworkListener() {
        try {
            unregisterReceiver(connectivityListener)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun initVariables() {
        backIv = findViewById(R.id.back_iv)
        emailEt = findViewById(R.id.email_et)
        submitBt = findViewById(R.id.submit_bt)
        connectivityListener = NetworkStateChangeListener()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        forgotProgressDialogue = ProgressDialog(this)
    }
}