package com.example.myapplication

import android.app.ProgressDialog
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.myapplication.Constants.ConstantValues
import com.example.myapplication.Encryption.Encryption
import com.example.myapplication.ModelClasses.User
import com.example.myapplication.Utility.ButtonState
import com.example.myapplication.Utility.NetworkStateChangeListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.scottyab.aescrypt.AESCrypt
import java.security.GeneralSecurityException

class SignUp : AppCompatActivity() {
    private lateinit var backIv: ImageView
    private lateinit var usernameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var confirmPasswordEt: EditText
    private lateinit var signUpBt: Button
    private lateinit var connectivityListener: NetworkStateChangeListener
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var userInfoDbRef: DatabaseReference
    private lateinit var signUpProgressDialogue: ProgressDialog

    override fun onResume() {
        registerNetworkListener()
        super.onResume()
    }

    override fun onDestroy() {
        unregisterNetworkListener()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        initVariables()
        ButtonState.enableButton(signUpBt)
        setupSignUpProgressDialogue()

        backIv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                finish()
            }
        })

        signUpBt.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(isAllFieldsNotEmpty()) {
                    if(isPasswordFieldValid()) {
                        signUpProgressDialogue.show()
                        ButtonState.disableButton(signUpBt)
                        registerUserInFirebase()
                    } else {
                        Toast.makeText(this@SignUp ,"Password and confirm password fields are not matching", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SignUp ,"Input field/fields can't be empty", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupSignUpProgressDialogue() {
        signUpProgressDialogue.setCanceledOnTouchOutside(false)
        signUpProgressDialogue.setTitle("Creating your profile")
        signUpProgressDialogue.setMessage("please wait while your account is being created")
    }

    private fun isPasswordFieldValid(): Boolean {
        return passwordEt.text.toString() == confirmPasswordEt.text.toString()
    }

    private fun registerUserInFirebase() {
        firebaseAuth.createUserWithEmailAndPassword(emailEt.text.toString().trim(), passwordEt.text.toString().trim()).addOnCompleteListener { task->
            signUpProgressDialogue.dismiss()
            ButtonState.enableButton(signUpBt)
            if(task.isSuccessful) {
                val currentUser: FirebaseUser? = firebaseAuth.currentUser
                currentUser?.sendEmailVerification()?.addOnCompleteListener { task->
                    if(task.isSuccessful) {
                        Toast.makeText(applicationContext, "user registered successfully, please verify your email", Toast.LENGTH_SHORT).show()
                        val createdUserFirebaseId: String = firebaseAuth.currentUser?.uid ?: "-1"
                        val AesEncryptedPassword: String = Encryption.aesEncryption(passwordEt.text.toString().trim())
                        val createdUser: User = User(ConstantValues.DEFAULT_LOGIN_TYPE,usernameEt.text.toString().trim(), emailEt.text.toString().trim(), AesEncryptedPassword, createdUserFirebaseId,ConstantValues.NOT_AVAILABLE ,ConstantValues.DEFAULT_USER_STATUS)
                        userInfoDbRef = db.reference.child("userinfo")
                        userInfoDbRef.child(createdUserFirebaseId).setValue(createdUser)
                        usernameEt.text.clear()
                        emailEt.text.clear()
                        passwordEt.text.clear()
                        confirmPasswordEt.text.clear()
                    } else {
                        Toast.makeText(applicationContext, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(applicationContext, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isAllFieldsNotEmpty(): Boolean {
        return !(usernameEt.text.isEmpty() || emailEt.text.isEmpty() || passwordEt.text.isEmpty() || confirmPasswordEt.text.isEmpty())
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
        usernameEt = findViewById(R.id.username_et)
        emailEt = findViewById(R.id.email_et)
        passwordEt = findViewById(R.id.password_et)
        confirmPasswordEt = findViewById(R.id.confirm_password_et)
        signUpBt = findViewById(R.id.sign_up_bt)
        connectivityListener = NetworkStateChangeListener()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        signUpProgressDialogue = ProgressDialog(this)
    }
}