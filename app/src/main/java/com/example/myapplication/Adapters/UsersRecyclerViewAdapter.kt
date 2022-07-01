package com.example.myapplication.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ChatActivity
import com.example.myapplication.Constants.ConstantValues
import com.example.myapplication.ModelClasses.User
import com.example.myapplication.R
import com.example.myapplication.ViewHolders.UsersViewHolder
import com.google.firebase.database.core.Constants
import com.squareup.picasso.Picasso
import java.io.Serializable

class UsersRecyclerViewAdapter: RecyclerView.Adapter<UsersViewHolder> {
    private val userList: ArrayList<User>
    private val context: Context

    constructor(context: Context) {
        userList = ArrayList()
        this.context = context
    }

    fun getUserList(): ArrayList<User> = userList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val createdView: View = LayoutInflater.from(context).inflate(R.layout.user_chat_layout, parent, false)
        val createdViewHolder: UsersViewHolder = UsersViewHolder(createdView)
        return createdViewHolder
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val currentUser: User = userList.get(position)
        Picasso.get().load(currentUser.getUserDp()).placeholder(R.drawable.default_user_logo).into(holder.userProfilePicture)
        holder.userDisplayName.text = currentUser.getUserName()
        holder.userStatus.text = currentUser.getUserStatus()

        //Click event on item(Will send click event to Activity)
        holder.itemView.setOnClickListener { view->
            startChatActivityWithCurrentUserObject(currentUser)
        }
    }

    override fun getItemCount(): Int = userList.size

    private fun startChatActivityWithCurrentUserObject(user: User) {
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
}