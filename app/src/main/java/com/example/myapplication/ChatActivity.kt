package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.media.Image
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapters.ChatRecyclerViewAdapter
import com.example.myapplication.Constants.ConstantValues
import com.example.myapplication.Encryption.Encryption
import com.example.myapplication.ModelClasses.InboxItemModel
import com.example.myapplication.ModelClasses.MessageModel
import com.example.myapplication.ModelClasses.User
import com.example.myapplication.Utility.InternetConnectivityListener
import com.facebook.CallbackManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.core.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var chatActivityTb: androidx.appcompat.widget.Toolbar
    private lateinit var chatBackIv: ImageView
    private lateinit var chatProfileIv: de.hdodenhof.circleimageview.CircleImageView
    private lateinit var chatUsernameTv: TextView
    private lateinit var presenceTv: TextView
    private lateinit var chatRv: RecyclerView
    private lateinit var chatMessageEt: EditText
    private lateinit var chatMessageSendFab: FloatingActionButton
    private lateinit var chatAttachmentIv: ImageView
    private lateinit var chatCameraIv: ImageView
    private lateinit var chatRvAdapter: ChatRecyclerViewAdapter
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var imageSendingDialogue: ProgressDialog
    private var senderUid: String? = null
    private var receiverUid: String? = null

    override fun onBackPressed() {
        //Setting unread count to zero when ChatActivity destroyed
        if(receiverUid != null) {
            firebaseDb.reference.child("inbox").child(firebaseAuth.currentUser!!.uid)
                .child(receiverUid!!).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()) {
                            firebaseDb.reference.child("inbox").child(firebaseAuth.currentUser!!.uid)
                                .child(receiverUid!!).child("inboxChatUnreadCount").setValue(0)
                        }
                        finish()
                    }

                    override fun onCancelled(error: DatabaseError) { }
                })
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        //Setting unread count to zero when ChatActivity destroyed
        if(receiverUid != null) {
            firebaseDb.reference.child("inbox").child(firebaseAuth.currentUser!!.uid)
                .child(receiverUid!!).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()) {
                            firebaseDb.reference.child("inbox").child(firebaseAuth.currentUser!!.uid)
                                .child(receiverUid!!).child("inboxChatUnreadCount").setValue(0)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) { }
                })
            super.onDestroy()
        } else {
            super.onDestroy()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ConstantValues.CHAT_INTENT_IMAGE_SELECT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            if(data.data != null) {
                val selectedImageUri: Uri = data.data!!
                sendSelectedImageMessageToFirebaseStorage(selectedImageUri)
            }
        }
    }

    private fun sendSelectedImageMessageToFirebaseStorage(selectedImageUri: Uri) {
        val calendar: Calendar = Calendar.getInstance()
        val sentImageStorageReference: StorageReference = FirebaseStorage.getInstance().reference.child("chat_image").child(calendar.timeInMillis.toString())
        imageSendingDialogue.show()
        sentImageStorageReference.putFile(selectedImageUri).addOnCompleteListener { task->
            imageSendingDialogue.dismiss()
            if(task.isSuccessful) {
                sentImageStorageReference.downloadUrl.addOnSuccessListener { uriInFirebaseStorage->
                    val linkInFirebaseStorage: String = uriInFirebaseStorage.toString()
                    updateFirebaseRealtimeDatabaseAfterSendingImage(linkInFirebaseStorage)
                }
            }
        }
    }

    private fun updateFirebaseRealtimeDatabaseAfterSendingImage(linkInFirebaseStorage: String) {
        val senderRoom: String = "$senderUid$receiverUid"
        val receiverRoom: String = "$receiverUid$senderUid"

        //Creating a random key in firebase realtime database for both sender room and receiver room to have same message id
        val randomDbKey: String? = firebaseDb.reference.push().key


        if(randomDbKey != null && senderUid != null && receiverUid != null) {
            val date: Date = Date()
            val currentTime: String = getCurrentTime(date)
            val currentMessage: MessageModel = MessageModel(randomDbKey, linkInFirebaseStorage,
                senderUid!!,
                receiverUid!!, ConstantValues.NOT_AVAILABLE_INT, date.time,currentTime , ConstantValues.IMAGE_MESSAGE_TYPE)

            //Adding message item for both sender and receiver side
            firebaseDb.reference.child("chats").child(senderRoom).child("messages").child(randomDbKey).setValue(currentMessage).addOnSuccessListener {
                firebaseDb.reference.child("chats").child(receiverRoom).child("messages").child(randomDbKey).setValue(currentMessage).addOnSuccessListener {

                    //Get logged in user details
                    firebaseDb.reference.child("userinfo").child(firebaseAuth.currentUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val loggedInUser: User? = snapshot.getValue(User::class.java)
                            if(loggedInUser != null) {
                                val senderInboxItemModel: InboxItemModel = InboxItemModel(ConstantValues.ZERO_VALUE_INT,
                                    receiverUid!!, intent.getStringExtra(ConstantValues.CHAT_INTENT_USER_NAME)!!, intent.getStringExtra(ConstantValues.CHAT_INTENT_USER_DP)!!,
                                    senderUid!!, loggedInUser.getUserName(),linkInFirebaseStorage, date.time, currentTime, false)

                                firebaseDb.reference.child("inbox").child(receiverUid!!).child(
                                    senderUid!!
                                ).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if(snapshot.exists()) {
                                            val unreadCount: Int? = snapshot.getValue(InboxItemModel::class.java)?.getInboxChatUnreadCount()
                                            val newlyCreated: Boolean? = snapshot.getValue(InboxItemModel::class.java)?.getInboxItemNewCreated()
                                            if(unreadCount != null && newlyCreated != null) {
                                                firebaseDb.reference.child("inbox").child(
                                                    senderUid!!
                                                ).child(receiverUid!!).addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        if(snapshot.getValue(InboxItemModel::class.java) != null) {
                                                            senderInboxItemModel.setInboxChatUnreadCount(snapshot.getValue(InboxItemModel::class.java)!!.getInboxChatUnreadCount())
                                                            senderInboxItemModel.setInboxItemNewCreated(false)
                                                            firebaseDb.reference.child("inbox").child(
                                                                senderUid!!
                                                            ).child(receiverUid!!).setValue(senderInboxItemModel).addOnSuccessListener {
                                                                val receiverInboxItemModel: InboxItemModel = InboxItemModel(unreadCount + 1,
                                                                    senderUid!!, loggedInUser.getUserName(), loggedInUser.getUserDp(),
                                                                    senderUid!!, loggedInUser.getUserName(),linkInFirebaseStorage, date.time, currentTime, false)

                                                                firebaseDb.reference.child("inbox").child(
                                                                    receiverUid!!
                                                                ).child(senderUid!!).setValue(receiverInboxItemModel).addOnSuccessListener {
                                                                    Log.v(ConstantValues.LOGCAT_TEST, "Both sender and receiver inbox updated successfully")
                                                                }
                                                            }
                                                        } else {
                                                            Toast.makeText(this@ChatActivity,  "Something went wrong", Toast.LENGTH_SHORT).show()
                                                        }

                                                    }

                                                    override fun onCancelled(error: DatabaseError) {}
                                                })
                                            } else {
                                                Toast.makeText(this@ChatActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            firebaseDb.reference.child("inbox").child(
                                                senderUid!!
                                            ).child(receiverUid!!).setValue(senderInboxItemModel).addOnSuccessListener {
                                                val receiverInboxItemModel: InboxItemModel = InboxItemModel(ConstantValues.ZERO_VALUE_INT,
                                                    senderUid!!, loggedInUser.getUserName(), loggedInUser.getUserDp(),
                                                    senderUid!!, loggedInUser.getUserName(),linkInFirebaseStorage, date.time, currentTime,true)

                                                firebaseDb.reference.child("inbox").child(
                                                    receiverUid!!
                                                ).child(senderUid!!).setValue(receiverInboxItemModel).addOnSuccessListener {
                                                    Log.v(ConstantValues.LOGCAT_TEST, "Both sender and receiver inbox updated successfully")
                                                }
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) { }
                                })

                            } else {
                                Toast.makeText(this@ChatActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                                Log.v(ConstantValues.LOGCAT_TEST, "Logged in fetched user is null")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) { }
                    })
                    Log.v(ConstantValues.LOGCAT_TEST, "Both messages registered successfully in Firebase Database")
                }
            }
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            Log.v(ConstantValues.LOGCAT_TEST, "random generated key from Firebase is null")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        initVariables()
        setSupportActionBar(chatActivityTb)
        setupChatRecyclerView()


        val fetchedIntent: Intent = intent
        val openedUserLoginType: String? =
            fetchedIntent.getStringExtra(ConstantValues.CHAT_INTENT_USER_LOGIN_TYPE)
        val openedUserUserName: String? =
            fetchedIntent.getStringExtra(ConstantValues.CHAT_INTENT_USER_NAME)
        val openedUserUserEmail: String? =
            fetchedIntent.getStringExtra(ConstantValues.CHAT_INTENT_USER_EMAIL)
        val openedUserUserPasswordAesEncrypted: String? =
            fetchedIntent.getStringExtra(ConstantValues.CHAT_INTENT_USER_PASSWOED_AES_ENCRYPTED)
        val openedUserUserId: String? =
            fetchedIntent.getStringExtra(ConstantValues.CHAT_INTENT_USER_ID)
        val openedUserUserDp: String? =
            fetchedIntent.getStringExtra(ConstantValues.CHAT_INTENT_USER_DP)
        val openedUserUserStatus: String? =
            fetchedIntent.getStringExtra(ConstantValues.CHAT_INTENT_USER_STATUS)

        if (openedUserLoginType != null && openedUserUserName != null && openedUserUserEmail != null && openedUserUserPasswordAesEncrypted != null && openedUserUserId != null && openedUserUserDp != null && openedUserUserStatus != null) {
            val openedUserObject: User = User(
                openedUserLoginType,
                openedUserUserName,
                openedUserUserEmail,
                openedUserUserPasswordAesEncrypted,
                openedUserUserId,
                openedUserUserDp,
                openedUserUserStatus
            )

            //Show if opened user is offline/online or if user is typing
            firebaseDb.reference.child("presence").child(openedUserObject.getUserId()).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val presenceStatus: String? = snapshot.getValue(String::class.java)
                        if(presenceStatus != null) {
                            if(presenceStatus.isEmpty()) {
                                presenceTv.text = ""
                                presenceTv.visibility = View.GONE
                            } else if(presenceStatus == "online") {
                                presenceTv.text = "online"
                                presenceTv.visibility = View.VISIBLE
                            } else {
                                presenceTv.text = "typing..."
                                presenceTv.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            if (firebaseAuth.currentUser != null) {
                setupImageSendingDialogue()
                senderUid = firebaseAuth.currentUser!!.uid
                receiverUid = openedUserUserId


                //Setting unread count to zero when ChatActivity opened
                firebaseDb.reference.child("inbox").child(firebaseAuth.currentUser!!.uid)
                    .child(receiverUid!!).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()) {
                                firebaseDb.reference.child("inbox").child(firebaseAuth.currentUser!!.uid)
                                    .child(receiverUid!!).child("inboxChatUnreadCount").setValue(0)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) { }
                    })


                setUserDetailsInActivity(openedUserObject)
                fetchChatsFromFirebase(senderUid!!, receiverUid!!)
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                Log.v(ConstantValues.LOGCAT_TEST, "Current user is null")
            }
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            Log.v(ConstantValues.LOGCAT_TEST, "Some field got from intent is null")
        }

        chatAttachmentIv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(senderUid != null && receiverUid != null) {
                    openImageSelectionOptions()
                } else {
                    Toast.makeText(this@ChatActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
        })

        chatCameraIv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                TODO("Not yet implemented")
            }
        })

        chatMessageSendFab.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(isMessageNotEmpty() && senderUid != null && receiverUid != null) {
                    sendMessageToFirebaseDatabase(chatMessageEt.text.toString() ,senderUid!!, receiverUid!!)
                    chatMessageEt.text.clear()
                } else {
                    Toast.makeText(this@ChatActivity, "Message can't be empty", Toast.LENGTH_SHORT).show()
                }
            }
        })

        chatBackIv.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                finish()
            }
        })

        //Showing (typing...) status in app and updating (typing...) status in firebase database
        val handler: Handler = Handler()
        chatMessageEt.addTextChangedListener(object : TextWatcher {
            val runnable: Runnable = Runnable {
                firebaseDb.reference.child("presence").child(firebaseAuth.currentUser!!.uid).setValue("online")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) {
                if(firebaseAuth.currentUser != null) {
                    firebaseDb.reference.child("presence").child(firebaseAuth.currentUser!!.uid).setValue("typing...")
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed(runnable, ConstantValues.TYPING_STATUS_CHANGE_TIMEOUT)
                }
            }
        })
    }

    private fun setupImageSendingDialogue() {
        imageSendingDialogue.setMessage("Sending image...")
        imageSendingDialogue.setCancelable(false)
    }

    private fun openImageSelectionOptions() {
        val imageSelectIntent = Intent()
        imageSelectIntent.action = Intent.ACTION_GET_CONTENT
        imageSelectIntent.type = "image/*"
        startActivityForResult(imageSelectIntent, ConstantValues.CHAT_INTENT_IMAGE_SELECT_REQUEST_CODE)
    }

    private fun fetchChatsFromFirebase(senderUid: String, receiverUid: String) {
        val senderRoom: String = "$senderUid$receiverUid"
        val receiverRoom: String = "$receiverUid$senderUid"
        chatRvAdapter.setSenderRoom(senderRoom)
        chatRvAdapter.setReceiverRoom(receiverRoom)
        firebaseDb.reference.child("chats").child(senderRoom).child("messages").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatRvAdapter.getChatList().clear()
                for(messageSnapshot: DataSnapshot in snapshot.children) {
                    val fetchedMessage: MessageModel? = messageSnapshot.getValue(MessageModel::class.java)
                    if(fetchedMessage != null) {
                        chatRvAdapter.addValueToChatList(fetchedMessage)
                    } else {
                        Log.v(ConstantValues.LOGCAT_TEST, "Fetched message is null")
                    }
                }
                chatRvAdapter.notifyDataSetChanged()
                chatRv.scrollToPosition(chatRvAdapter.getChatList().size-1)
            }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private fun setupChatRecyclerView() {
        chatRv.adapter = chatRvAdapter
        chatRv.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false)
    }

    private fun sendMessageToFirebaseDatabase(message:String, senderUid: String, receiverUid: String) {
        if(isMessageNotEmpty()) {
            val senderRoom: String = "$senderUid$receiverUid"
            val receiverRoom: String = "$receiverUid$senderUid"

            //Creating a random key in firebase realtime database for both sender room and receiver room to have same message id
            val randomDbKey: String? = firebaseDb.reference.push().key

            if(randomDbKey != null) {
                val date: Date = Date()
                val currentTime: String = getCurrentTime(date)
                val currentMessage: MessageModel = MessageModel(randomDbKey, Encryption.aesEncryption(message), senderUid, receiverUid, ConstantValues.NOT_AVAILABLE_INT, date.time,currentTime , ConstantValues.TEXT_MESSAGE_TYPE)

                //Adding message item for both sender and receiver side
                firebaseDb.reference.child("chats").child(senderRoom).child("messages").child(randomDbKey).setValue(currentMessage).addOnSuccessListener {
                    firebaseDb.reference.child("chats").child(receiverRoom).child("messages").child(randomDbKey).setValue(currentMessage).addOnSuccessListener {

                        //Get logged in user details
                        firebaseDb.reference.child("userinfo").child(firebaseAuth.currentUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val loggedInUser: User? = snapshot.getValue(User::class.java)
                                if(loggedInUser != null) {
                                    val senderInboxItemModel: InboxItemModel = InboxItemModel(ConstantValues.ZERO_VALUE_INT, receiverUid, intent.getStringExtra(ConstantValues.CHAT_INTENT_USER_NAME)!!, intent.getStringExtra(ConstantValues.CHAT_INTENT_USER_DP)!!, senderUid, loggedInUser.getUserName(),Encryption.aesEncryption(message), date.time, currentTime, false)

                                    firebaseDb.reference.child("inbox").child(receiverUid).child(senderUid).addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if(snapshot.exists()) {
                                                val unreadCount: Int? = snapshot.getValue(InboxItemModel::class.java)?.getInboxChatUnreadCount()
                                                val newlyCreated: Boolean? = snapshot.getValue(InboxItemModel::class.java)?.getInboxItemNewCreated()
                                                if(unreadCount != null && newlyCreated != null) {
                                                    firebaseDb.reference.child("inbox").child(senderUid).child(receiverUid).addListenerForSingleValueEvent(object : ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            if(snapshot.getValue(InboxItemModel::class.java) != null) {
                                                                senderInboxItemModel.setInboxChatUnreadCount(snapshot.getValue(InboxItemModel::class.java)!!.getInboxChatUnreadCount())
                                                                senderInboxItemModel.setInboxItemNewCreated(false)
                                                                firebaseDb.reference.child("inbox").child(senderUid).child(receiverUid).setValue(senderInboxItemModel).addOnSuccessListener {
                                                                    val receiverInboxItemModel: InboxItemModel = InboxItemModel(unreadCount + 1, senderUid, loggedInUser.getUserName(), loggedInUser.getUserDp(), senderUid, loggedInUser.getUserName(),Encryption.aesEncryption(message), date.time, currentTime, false)

                                                                    firebaseDb.reference.child("inbox").child(receiverUid).child(senderUid).setValue(receiverInboxItemModel).addOnSuccessListener {
                                                                        Log.v(ConstantValues.LOGCAT_TEST, "Both sender and receiver inbox updated successfully")
                                                                    }
                                                                }
                                                            } else {
                                                                Toast.makeText(this@ChatActivity,  "Something went wrong", Toast.LENGTH_SHORT).show()
                                                            }

                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })
                                                } else {
                                                    Toast.makeText(this@ChatActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                firebaseDb.reference.child("inbox").child(senderUid).child(receiverUid).setValue(senderInboxItemModel).addOnSuccessListener {
                                                    val receiverInboxItemModel: InboxItemModel = InboxItemModel(ConstantValues.ZERO_VALUE_INT, senderUid, loggedInUser.getUserName(), loggedInUser.getUserDp(), senderUid, loggedInUser.getUserName(),Encryption.aesEncryption(message), date.time, currentTime,true)

                                                    firebaseDb.reference.child("inbox").child(receiverUid).child(senderUid).setValue(receiverInboxItemModel).addOnSuccessListener {
                                                        Log.v(ConstantValues.LOGCAT_TEST, "Both sender and receiver inbox updated successfully")
                                                    }
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) { }
                                    })

                                } else {
                                    Toast.makeText(this@ChatActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                                    Log.v(ConstantValues.LOGCAT_TEST, "Logged in fetched user is null")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) { }
                        })
                        Log.v(ConstantValues.LOGCAT_TEST, "Both messages registered successfully in Firebase Database")
                    }
                }
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                Log.v(ConstantValues.LOGCAT_TEST, "random generated key from Firebase is null")
            }
        } else {
            Toast.makeText(this, "Message can' be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentTime(date: Date): String {
        val dateFormat: DateFormat = SimpleDateFormat("HH:mm")
        dateFormat.timeZone = TimeZone.getDefault()
        return dateFormat.format(date)
    }

    private fun isMessageNotEmpty(): Boolean {
        return chatMessageEt.text.toString().isNotEmpty()
    }

    private fun setUserDetailsInActivity(user: User) {
        Picasso.get().load(user.getUserDp()).placeholder(R.drawable.default_user_logo).into(chatProfileIv)
        chatUsernameTv.text = user.getUserName()
    }

    private fun initVariables() {
        chatActivityTb = findViewById(R.id.chat_activity_tb)
        chatBackIv = findViewById(R.id.chat_back_iv)
        chatProfileIv = findViewById(R.id.chat_profile_iv)
        chatUsernameTv = findViewById(R.id.chat_username_tv)
        presenceTv = findViewById(R.id.presence_tv)
        chatRv = findViewById(R.id.chat_rv)
        chatMessageEt = findViewById(R.id.chat_message_et)
        chatMessageSendFab = findViewById(R.id.chat_message_send_fab)
        chatAttachmentIv = findViewById(R.id.chat_attachment_iv)
        chatCameraIv = findViewById(R.id.chat_camera_iv)
        chatRvAdapter = ChatRecyclerViewAdapter(this,ConstantValues.NOT_AVAILABLE,ConstantValues.NOT_AVAILABLE)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDb = FirebaseDatabase.getInstance()
        imageSendingDialogue = ProgressDialog(this)
    }
}