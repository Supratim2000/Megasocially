package com.example.myapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Constants.ConstantValues
import com.example.myapplication.ModelClasses.User
import com.example.myapplication.Utility.ButtonState
import com.example.myapplication.Utility.NetworkStateChangeListener
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.google.firebase.database.core.Constants
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class SignIn : AppCompatActivity() {
    private lateinit var exitIv: ImageView
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var loginBt: Button
    private lateinit var signUpBt: Button
    private lateinit var forgotTv: TextView
    private lateinit var googleLoginBt: Button
    private lateinit var facebookLoginBt: Button
    private lateinit var connectivityListener: NetworkStateChangeListener
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var signInProgressDialogue: ProgressDialog
    private lateinit var db: FirebaseDatabase
    private lateinit var userInfoDbRef: DatabaseReference
    private lateinit var callBackManager: CallbackManager

    override fun onBackPressed() {
        showExitDialogue()
    }

    override fun onResume() {
        registerNetworkListener()
        super.onResume()
    }

    override fun onDestroy() {
        unregisterNetworkListener()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ConstantValues.RC_SIGN_IN && resultCode == Activity.RESULT_OK && data != null) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val googleSignInAccount: GoogleSignInAccount =
                    task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(googleSignInAccount.idToken)
            } catch (e: ApiException) {
                e.printStackTrace()
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            callBackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        initVariables()
        setupSignInProgressDialogue()
        ButtonState.enableButton(loginBt)

        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        exitIv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                showExitDialogue()
            }
        })

        loginBt.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (isLoginFieldEmpty()) {
                    Toast.makeText(this@SignIn, "Login fields can't be empty", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    signInProgressDialogue.show()
                    ButtonState.disableButton(loginBt)
                    loginUser()
                }
            }
        })

        signUpBt.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (v != null) {
                    jumpToSignUpActivity()
                }
            }
        })

        forgotTv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (v != null) {
                    jumpToForgotActivity()
                }
            }
        })

        googleLoginBt.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                loginUserUsingGoogle()
            }
        })

        facebookLoginBt.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                loginUserUsingFacebook()
            }
        })
        registerFacebookResultCallback()
    }

    private fun registerFacebookResultCallback() {
        LoginManager.getInstance()
            .registerCallback(callBackManager, object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Log.v(ConstantValues.LOGCAT_TEST, "Facebook Login Cancelled")
                }

                override fun onError(error: FacebookException) {
                    Log.v(ConstantValues.LOGCAT_TEST, "Facebook Login error: ${error.message}")
                    error.printStackTrace()
                }

                override fun onSuccess(result: LoginResult) {
                    val profile: Profile? = Profile.getCurrentProfile()
                    val accessToken: AccessToken? = AccessToken.getCurrentAccessToken()
                    if (BuildConfig.DEBUG) {
                        FacebookSdk.setIsDebugEnabled(true);
                        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
                        Log.v(ConstantValues.LOGCAT_TEST, accessToken.toString())
                    }
                    integrateLoginInFacebookWithFirebase(accessToken, profile)
                }
            })
    }

    private fun integrateLoginInFacebookWithFirebase(accessToken: AccessToken?, profile: Profile?) {
        if (accessToken != null && profile != null) {
            val authCredential: AuthCredential =
                FacebookAuthProvider.getCredential(accessToken.token)

            firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fetchUserDetailsFromFacebookLogin(accessToken, task, profile)
                } else {
                    Log.v(ConstantValues.LOGCAT_TEST, "Facebook login failure")
                }
            }
        }
    }

    private fun fetchUserDetailsFromFacebookLogin(
        accessToken: AccessToken?,
        task: Task<AuthResult>,
        profile: Profile?
    ) {
        if (accessToken != null && profile != null) {
            val request = GraphRequest.newMeRequest(
                accessToken,
                object : GraphRequest.GraphJSONObjectCallback {
                    override fun onCompleted(
                        obj: JSONObject?,
                        response: GraphResponse?
                    ) {
                        try {
                            if (obj != null) {
                                val createdUserFirebaseId: String =
                                    task.result.user?.uid ?: ConstantValues.NOT_AVAILABLE
                                val facebookUserName = obj.getString("name")
                                val facebookUserDpUrl: String =
                                    profile.getProfilePictureUri(400, 400).toString()
                                val createdUser: User = User(
                                    ConstantValues.FACEBOOK_LOGIN_TYPE,
                                    facebookUserName,
                                    ConstantValues.NOT_AVAILABLE,
                                    ConstantValues.NOT_AVAILABLE,
                                    createdUserFirebaseId,
                                    facebookUserDpUrl,
                                    ConstantValues.DEFAULT_USER_STATUS
                                )
                                userInfoDbRef = db.reference.child("userinfo")
                                userInfoDbRef.child(createdUserFirebaseId).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if(!snapshot.exists()) {
                                            userInfoDbRef.child(createdUserFirebaseId).setValue(createdUser)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) { }
                                })
                            }
                        } catch (e: JSONException) {
                            e.message?.let { Log.v(ConstantValues.LOGCAT_TEST, it) }
                        }
                    }
                })
            val parameters = Bundle()
            parameters.putString("fields", "id,name,link")
            request.parameters = parameters
            request.executeAsync()
            goToMainActivity()
        } else {
            Log.v(ConstantValues.LOGCAT_TEST, "AccessToken is null")
        }
    }

    private fun loginUserUsingFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
    }

    private fun loginUserUsingGoogle() {
        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ConstantValues.default_web_client_id)
                .requestEmail()
                .build()

        val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(this, gso);

        val signInIntent: Intent = googleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, ConstantValues.RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        if (idToken != null) {
            val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser: FirebaseUser? = firebaseAuth.currentUser
                    if (currentUser != null) {
                        val createdUser: User = User(
                            ConstantValues.GOOGLE_LOGIN_TYPE,
                            currentUser.displayName!!,
                            currentUser.email!!,
                            ConstantValues.NOT_AVAILABLE,
                            currentUser.uid,
                            currentUser.photoUrl.toString(),
                            ConstantValues.DEFAULT_USER_STATUS
                        )
                        userInfoDbRef = db.reference.child("userinfo")
                        userInfoDbRef.child(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(!snapshot.exists()) {
                                    userInfoDbRef.child(currentUser.uid).setValue(createdUser)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) { }
                        })
                        goToMainActivity()
                    }
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSignInProgressDialogue() {
        signInProgressDialogue.setCanceledOnTouchOutside(false)
        signInProgressDialogue.setTitle("Logging in")
        signInProgressDialogue.setMessage("please wait while your are signing in to your account")
    }

    private fun loginUser() {
        firebaseAuth.signInWithEmailAndPassword(
            emailEt.text.toString().trim(),
            passwordEt.text.toString().trim()
        ).addOnCompleteListener { task ->
            signInProgressDialogue.dismiss()
            ButtonState.enableButton(loginBt)
            if (task.isSuccessful) {
                val currentUser: FirebaseUser? = firebaseAuth.currentUser
                if (currentUser != null && currentUser.isEmailVerified) {
                    goToMainActivity()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "user not verified, go to your email to verify",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(applicationContext, task.exception?.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun goToMainActivity() {
        val signInToMainIntent: Intent = Intent(this, MainActivity::class.java)
        startActivity(signInToMainIntent)
        finish()
    }

    private fun isLoginFieldEmpty(): Boolean {
        return emailEt.text.isEmpty() || passwordEt.text.isEmpty()
    }

    private fun showExitDialogue() {
        val exitBuilder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        val exitView: View = LayoutInflater.from(this)
            .inflate(R.layout.exit_dialogue, findViewById(R.id.exit_dialogue_layout_parent))
        exitBuilder.setView(exitView)
        val exitDialog: AlertDialog = exitBuilder.create()
        exitDialog.setCanceledOnTouchOutside(false)
        exitView.findViewById<ImageView>(R.id.exit_dialogue_iv)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    exitDialog.dismiss()
                }
            })
        exitView.findViewById<Button>(R.id.yes_bt)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    exitDialog.dismiss()
                    finish()
                }
            })
        exitView.findViewById<Button>(R.id.no_bt).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                exitDialog.dismiss()
            }
        })
        if (exitDialog.window != null) {
            exitDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        exitDialog.show()
    }

    private fun jumpToForgotActivity() {
        val forgotIntent: Intent = Intent(this, ForgotActivity::class.java)
        startActivity(forgotIntent)
    }

    private fun jumpToSignUpActivity() {
        val signUpIntent: Intent = Intent(this, SignUp::class.java)
        startActivity(signUpIntent)
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
        exitIv = findViewById(R.id.exit_iv)
        emailEt = findViewById(R.id.email_et)
        passwordEt = findViewById(R.id.password_et)
        loginBt = findViewById(R.id.login_bt)
        signUpBt = findViewById(R.id.sign_up_bt)
        forgotTv = findViewById(R.id.forgot_tv)
        googleLoginBt = findViewById(R.id.google_login_bt)
        facebookLoginBt = findViewById(R.id.facebook_login_bt)
        connectivityListener = NetworkStateChangeListener()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        signInProgressDialogue = ProgressDialog(this)
        db = FirebaseDatabase.getInstance()
        callBackManager = CallbackManager.Factory.create()
    }
}