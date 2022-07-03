package com.example.myapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import com.example.myapplication.Constants.ConstantValues
import com.example.myapplication.ModelClasses.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {
    private lateinit var editBackIv: ImageView
    private lateinit var editProfilePictureCiv: de.hdodenhof.circleimageview.CircleImageView
    private lateinit var editUserNameTv: TextView
    private lateinit var editLoginType: TextView
    private lateinit var usernameEt: EditText
    private lateinit var statusEt: EditText
    private lateinit var updateBt: Button
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var imageUploadProgressDialogue: ProgressDialog
    private lateinit var profileDetailsUpdateProgressDialogue: ProgressDialog

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ConstantValues.PICK_IMAGE_FROM_GALLERY_INTENT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            if(data.data != null) {
                editProfilePictureCiv.setImageURI(data.data)
                compressAndUploadImageToFirebaseStorage(data.data!!)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if(FirebaseAuth.getInstance().currentUser != null) {
            FirebaseDatabase.getInstance().reference.child("presence").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue("")
        }
    }

    override fun onResume() {
        if(FirebaseAuth.getInstance().currentUser != null) {
            FirebaseDatabase.getInstance().reference.child("presence").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue("online")
        }
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun compressAndUploadImageToFirebaseStorage(imageUri: Uri) {
        //Image compression code
        val actualImageBitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val stream: ByteArrayOutputStream = ByteArrayOutputStream()
        actualImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        val compressedImageByte: ByteArray = stream.toByteArray()

        //Firebase Storage upload code
        if(firebaseAuth.currentUser != null) {
            imageUploadProgressDialogue.show()
            val profilePictureStorageReference: StorageReference = firebaseStorage.reference.child("profile_picture").child(firebaseAuth.currentUser?.uid!!)
            profilePictureStorageReference.putBytes(compressedImageByte).addOnCompleteListener { task->
                imageUploadProgressDialogue.dismiss()
                if(task.isSuccessful) {
                    profilePictureStorageReference.downloadUrl.addOnSuccessListener { uri->
                        val imageUrlInFirebaseDatabase: String = uri.toString()
                        firebaseDb.reference.child("userinfo").child(firebaseAuth.currentUser!!.uid!!).child("userDp").setValue(imageUrlInFirebaseDatabase).addOnCompleteListener { task->
                            if(task.isSuccessful) {
                                Log.v(ConstantValues.LOGCAT_TEST ,"Image Url saved successfully in Firebase Realtime Database")
                            } else {
                                Log.v(ConstantValues.LOGCAT_TEST ,"Image Url save failed in Firebase Realtime Database")
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Profile picture upload failed", Toast.LENGTH_SHORT).show()
                    Log.v(ConstantValues.LOGCAT_TEST ,"Profile picture upload failed")
                }
            }
        } else {
            Toast.makeText(this, "Used not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        initVariable()
        setupImageUploadProgressDialogue()
        setupProfileDetailsUpdateProgressDialogue()
        fetchCurrentUserDetails()

        editBackIv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                finish()
            }
        })

        editProfilePictureCiv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                selectImageFromGallery()
            }
        })

        updateBt.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(isInputFieldsNotEmpty()) {
                    profileDetailsUpdateProgressDialogue.show()
                    updateUserInfoInFirebase(usernameEt.text.toString(), statusEt.text.toString())
                    finish()
                } else {
                    Toast.makeText(this@EditProfileActivity, "Input field/fields can't be empty", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun isInputFieldsNotEmpty(): Boolean {
        return usernameEt.text.toString().isNotEmpty() && statusEt.text.toString().isNotEmpty()
    }

    private fun setupProfileDetailsUpdateProgressDialogue() {
        profileDetailsUpdateProgressDialogue.setCanceledOnTouchOutside(false)
        profileDetailsUpdateProgressDialogue.setTitle("Waiting...")
        profileDetailsUpdateProgressDialogue.setMessage("Please wait while we are updating your profile details")
    }

    private fun selectImageFromGallery() {
        val pickImageFromGalleryIntent: Intent = Intent()
        pickImageFromGalleryIntent.action = Intent.ACTION_GET_CONTENT
        pickImageFromGalleryIntent.type = "image/*"
        startActivityForResult(pickImageFromGalleryIntent, ConstantValues.PICK_IMAGE_FROM_GALLERY_INTENT_REQUEST_CODE)
    }

    private fun fetchCurrentUserDetails() {
        if(firebaseAuth.currentUser?.uid != null) {
            firebaseDb.reference.child("userinfo").child(firebaseAuth.currentUser?.uid!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentUser: User? = snapshot.getValue(User::class.java)
                    if(currentUser != null) {
                        showOnActivity(currentUser)
                    }
                }

                override fun onCancelled(error: DatabaseError) { }
            })
        }
    }

    private fun showOnActivity(user: User) {
        Picasso.get().load(user.getUserDp()).placeholder(R.drawable.default_user_logo).into(editProfilePictureCiv)
        editUserNameTv.text = user.getUserName()
        editLoginType.text = "Login type: ${user.getLoginType()}"
        usernameEt.setText(user.getUserName())
        statusEt.setText(user.getUserStatus())
    }

    private fun updateUserInfoInFirebase(userNameString: String, userStatusString: String) {
        if(firebaseAuth.currentUser != null) {
            val currentUserDatabaseReference = firebaseDb.reference.child("userinfo").child(firebaseAuth.currentUser!!.uid)
            currentUserDatabaseReference.child("userName").setValue(userNameString).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    Log.v(ConstantValues.LOGCAT_TEST, "userName successfully updated in firebase realtime database")
                } else {
                    Log.v(ConstantValues.LOGCAT_TEST, "userName failed to update in firebase realtime database")
                }
            }
            currentUserDatabaseReference.child("userStatus").setValue(userStatusString).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    Log.v(ConstantValues.LOGCAT_TEST, "userStatus successfully updated in firebase realtime database")
                } else {
                    Log.v(ConstantValues.LOGCAT_TEST, "userStatus failed to update in firebase realtime database")
                }
            }
        } else {
            Log.v(ConstantValues.LOGCAT_TEST, "Current user is null")
        }
        profileDetailsUpdateProgressDialogue.dismiss()
    }

    private fun setupImageUploadProgressDialogue() {
        imageUploadProgressDialogue.setCanceledOnTouchOutside(false)
        imageUploadProgressDialogue.setTitle("Waiting...")
        imageUploadProgressDialogue.setMessage("please wait while we are Uploading your profile picture")
    }

    private fun initVariable() {
        editBackIv = findViewById(R.id.edit_back_iv)
        editProfilePictureCiv = findViewById(R.id.edit_profile_picture_civ)
        editUserNameTv = findViewById(R.id.edit_user_name_tv)
        editLoginType = findViewById(R.id.edit_login_type)
        usernameEt = findViewById(R.id.username_et)
        statusEt = findViewById(R.id.status_et)
        updateBt = findViewById(R.id.update_bt)
        firebaseDb = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        imageUploadProgressDialogue = ProgressDialog(this)
        profileDetailsUpdateProgressDialogue = ProgressDialog(this)
    }
}