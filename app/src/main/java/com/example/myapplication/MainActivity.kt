package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.myapplication.Adapters.MainActivityViewPagerAdapter
import com.example.myapplication.Constants.ConstantValues
import com.example.myapplication.Fragments.ChatsFragment
import com.example.myapplication.Fragments.StatusFragment
import com.example.myapplication.Fragments.UsersFragment
import com.facebook.AccessToken
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserInfo
import java.lang.reflect.Field
import java.lang.reflect.Method

class MainActivity : AppCompatActivity() {
    private lateinit var mainActivityTb: androidx.appcompat.widget.Toolbar
    private lateinit var menuIv: ImageView
    private lateinit var mainActivityTL: TabLayout
    private lateinit var mainActivityVp: ViewPager
    private lateinit var mainActivityViewPagerAdapter: MainActivityViewPagerAdapter
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initVariables()
        setSupportActionBar(mainActivityTb)
        setUpViewPager()

        menuIv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                showPopupMenu(v)
            }
        })
    }

    private fun showPopupMenu(v: View?) {
        if (v != null) {
            val mainActivityPopupMenu: PopupMenu = PopupMenu(applicationContext, v)
            mainActivityPopupMenu.menuInflater.inflate(
                R.menu.main_activity_option_menu,
                mainActivityPopupMenu.menu
            )
            mainActivityPopupMenu.setOnMenuItemClickListener { item ->
                val menuItemId = item?.itemId
                if (menuItemId == R.id.edit_profile) {
                    gotoEditProfileActivity()
                }
                if (menuItemId == R.id.sign_out) {
                    signOutUser()
                    goToLoginActivity()
                }
                true
            }
            setForceShowIcon(mainActivityPopupMenu)
            mainActivityPopupMenu.show()
        }
    }

    private fun goToLoginActivity() {
        val signInActivityIntent: Intent = Intent(this, SignIn::class.java)
        startActivity(signInActivityIntent)
        finishAffinity()
    }

    private fun gotoEditProfileActivity() {
        val editProfileIntent: Intent = Intent(this, EditProfileActivity::class.java)
        startActivity(editProfileIntent)
    }

    private fun signOutUser() {
        var isUserLoggedInWithFacebook: Boolean = false
        var isUserLoggedInWithGoogle: Boolean = false
        if (firebaseAuth.currentUser != null) {
            for (loggedInUserInfo: UserInfo in firebaseAuth.currentUser!!.providerData) {
                if (loggedInUserInfo.providerId == "facebook.com") {
                    isUserLoggedInWithFacebook = true
                    break
                } else if (loggedInUserInfo.providerId == "google.com") {
                    isUserLoggedInWithGoogle = true
                    break
                }
            }
            if (isUserLoggedInWithFacebook) {
                FacebookSdk.sdkInitialize(applicationContext);
                LoginManager.getInstance().logOut()
                AccessToken.setCurrentAccessToken(null)
                Log.v(ConstantValues.LOGCAT_TEST,"Signed out from Facebook")
            } else if (isUserLoggedInWithGoogle) {
                GoogleSignIn.getClient(
                    this,
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                ).signOut().addOnCompleteListener {
                    Log.v(ConstantValues.LOGCAT_TEST,"Signed out from Google")
                }
            }
            Log.v(ConstantValues.LOGCAT_TEST,"Signed out from Firebase")
            firebaseAuth.signOut()
        }
    }


    fun setForceShowIcon(popupMenu: PopupMenu) {
        try {
            val fields: Array<Field> = popupMenu.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper: Any? = field.get(popupMenu)
                    if (menuPopupHelper
                            ?.javaClass?.name != null
                    ) {
                        val classPopupHelper = Class.forName(
                            menuPopupHelper
                                .javaClass.name
                        )
                        val setForceIcons: Method = classPopupHelper.getMethod(
                            "setForceShowIcon", Boolean::class.javaPrimitiveType
                        )
                        setForceIcons.invoke(menuPopupHelper, true)
                        break
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun setUpViewPager() {
        mainActivityViewPagerAdapter = MainActivityViewPagerAdapter(
            supportFragmentManager,
            ConstantValues.COUNT_OF_FRAGMENT_IN_MAIN_ACTIVITY
        )
        mainActivityViewPagerAdapter.addFragment(
            ChatsFragment(),
            ConstantValues.CHATS_FRAGMENT_TITLE
        )
        mainActivityViewPagerAdapter.addFragment(
            UsersFragment(),
            ConstantValues.USERS_FRAGMENT_TITLE
        )
        mainActivityViewPagerAdapter.addFragment(
            StatusFragment(),
            ConstantValues.STATUS_FRAGMENT_TITLE
        )
        mainActivityVp.adapter = mainActivityViewPagerAdapter
        mainActivityTL.setupWithViewPager(mainActivityVp)
    }

    private fun initVariables() {
        mainActivityTb = findViewById(R.id.main_activity_tb)
        menuIv = findViewById(R.id.menu_iv)
        mainActivityTL = findViewById(R.id.main_activity_tl)
        mainActivityVp = findViewById(R.id.main_activity_vp)
        firebaseAuth = FirebaseAuth.getInstance()
    }
}