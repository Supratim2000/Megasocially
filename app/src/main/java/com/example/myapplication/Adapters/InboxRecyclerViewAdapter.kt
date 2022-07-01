package com.example.myapplication.Adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ChatActivity
import com.example.myapplication.Constants.ConstantValues
import com.example.myapplication.ModelClasses.InboxItemModel
import com.example.myapplication.ModelClasses.User
import com.example.myapplication.R
import com.example.myapplication.ViewHolders.InboxViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class InboxRecyclerViewAdapter: RecyclerView.Adapter<InboxViewHolder> {
    private val inboxList: ArrayList<InboxItemModel>
    private val context: Context

    constructor(context: Context) : super() {
        this.inboxList = ArrayList()
        this.context = context
    }

    fun getInboxList(): ArrayList<InboxItemModel> = inboxList
    fun addItemToInboxList(inboxItemModel: InboxItemModel) {
        this.inboxList.add(inboxItemModel)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val createdView: View = LayoutInflater.from(context).inflate(R.layout.user_inbox_layout, parent, false)
        val createdViewHolder: InboxViewHolder = InboxViewHolder(createdView)
        return createdViewHolder
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        val currentInboxItem: InboxItemModel = inboxList[position]
        Picasso.get().load(currentInboxItem.getInboxChatUserUidProfilePicture()).placeholder(R.drawable.default_user_logo).into(holder.inboxProfilePictureCiv)
        holder.inboxDisplayNameTv.text = currentInboxItem.getInboxChatUserUidName()
        holder.inboxLastMessageTimeTv.text = currentInboxItem.getInboxChatLastMessageTime()
        holder.inboxLastTextTv.text = "${currentInboxItem.getInboxChatLastMessageFromUidUserName()}: ${currentInboxItem.getInboxChatLastMessage()}"
        if(currentInboxItem.getInboxChatUnreadCount()>0) {
            holder.inboxUnreadCountTv.text = currentInboxItem.getInboxChatUnreadCount().toString()
            holder.inboxUnreadCountTv.visibility = View.VISIBLE
        } else {
            holder.inboxUnreadCountTv.visibility = View.INVISIBLE
        }


        holder.itemView.setOnClickListener { view ->
            if(FirebaseAuth.getInstance().currentUser != null) {
                FirebaseDatabase.getInstance().reference.child("userinfo").child(currentInboxItem.getInboxChatUserUid()).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val clickedUser: User? = snapshot.getValue(User::class.java)
                        if(clickedUser != null) {
                            startChatActivityWithSelectedUserDetails(clickedUser)
                        } else {
                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                            Log.v(ConstantValues.LOGCAT_TEST, "Clicked fetched user is null")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) { }
                })
            } else {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                Log.v(ConstantValues.LOGCAT_TEST, "Logged in user is null")
            }
        }
    }

    private fun startChatActivityWithSelectedUserDetails(user: User) {
        val chatIntent: Intent = Intent(context, ChatActivity::class.java)
        chatIntent.putExtra(ConstantValues.CHAT_INTENT_USER_LOGIN_TYPE, user.getLoginType())
        chatIntent.putExtra(ConstantValues.CHAT_INTENT_USER_NAME, user.getUserName())
        chatIntent.putExtra(ConstantValues.CHAT_INTENT_USER_EMAIL, user.getUserEmail())
        chatIntent.putExtra(ConstantValues.CHAT_INTENT_USER_PASSWOED_AES_ENCRYPTED, user.getUserPasswordAesEncrypted())
        chatIntent.putExtra(ConstantValues.CHAT_INTENT_USER_ID, user.getUserId())
        chatIntent.putExtra(ConstantValues.CHAT_INTENT_USER_DP, user.getUserDp())
        chatIntent.putExtra(ConstantValues.CHAT_INTENT_USER_STATUS, user.getUserStatus())
        context.startActivity(chatIntent)
    }

    override fun getItemCount(): Int = inboxList.size
}