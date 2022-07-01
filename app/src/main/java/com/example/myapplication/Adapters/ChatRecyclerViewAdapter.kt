package com.example.myapplication.Adapters

import android.content.Context
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Constants.ConstantValues
import com.example.myapplication.ModelClasses.MessageModel
import com.example.myapplication.R
import com.example.myapplication.ViewHolders.ReceivedMessageViewHolder
import com.example.myapplication.ViewHolders.SentMessageViewHolder
import com.github.pgreze.reactions.ReactionPopup
import com.github.pgreze.reactions.ReactionsConfig
import com.github.pgreze.reactions.dsl.reactionConfig
import com.github.pgreze.reactions.dsl.reactions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class ChatRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private val reactionList: ArrayList<Int>
    private val chatList: ArrayList<MessageModel>
    private val context: Context
    private var senderRoom: String
    private var receiverRoom: String

    constructor(context: Context, senderRoom: String, receiverRoom: String) {
        reactionList = ArrayList()
        addReactions()
        this.chatList = ArrayList()
        this.context = context
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }

    public fun setSenderRoom(senderRoom: String) {
        this.senderRoom = senderRoom
    }

    public fun setReceiverRoom(receiverRoom: String) {
        this.receiverRoom = receiverRoom
    }

    private fun addReactions() {
        reactionList.add(R.drawable.ic_fb_like)
        reactionList.add(R.drawable.ic_fb_love)
        reactionList.add(R.drawable.ic_fb_laugh)
        reactionList.add(R.drawable.ic_fb_wow)
        reactionList.add(R.drawable.ic_fb_sad)
        reactionList.add(R.drawable.ic_fb_angry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == ConstantValues.ITEM_SENT_VIEW_TYPE) {
            val sentView: View = LayoutInflater.from(context).inflate(R.layout.sent_item, parent, false)
            val sentViewHolder: SentMessageViewHolder = SentMessageViewHolder(sentView)
            return sentViewHolder
        } else {
            val receivedView: View = LayoutInflater.from(context).inflate(R.layout.received_item, parent, false)
            val receivedViewHolder: ReceivedMessageViewHolder = ReceivedMessageViewHolder(receivedView)
            return receivedViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage: MessageModel = chatList.get(position)

        val config: ReactionsConfig = reactionConfig(context) {
            reactions {
                resId    { R.drawable.ic_fb_like }
                resId    { R.drawable.ic_fb_love }
                resId    { R.drawable.ic_fb_laugh }
                reaction { R.drawable.ic_fb_wow scale ImageView.ScaleType.FIT_XY }
                reaction { R.drawable.ic_fb_sad scale ImageView.ScaleType.FIT_XY }
                reaction { R.drawable.ic_fb_angry scale ImageView.ScaleType.FIT_XY }
            }
        }

        val popup = ReactionPopup(context, config, { pos ->
            if(holder.javaClass == SentMessageViewHolder::class.java) {
                val sentViewHolder: SentMessageViewHolder = holder as SentMessageViewHolder
                if(pos != -1) {
                    sentViewHolder.sentReactionIv.setImageResource(reactionList[pos])
                    sentViewHolder.sentReactionIv.visibility = View.VISIBLE
                }
            }
            if(holder.javaClass == ReceivedMessageViewHolder::class.java) {
                val receivedViewHolder: ReceivedMessageViewHolder = holder as ReceivedMessageViewHolder
                if(pos != -1) {
                    receivedViewHolder.receivedReactionIv.setImageResource(reactionList[pos])
                    receivedViewHolder.receivedReactionIv.visibility = View.VISIBLE
                }
            }

            currentMessage.setMessageReaction(pos)

            FirebaseDatabase.getInstance().reference.child("chats").child(senderRoom).child("messages").child(currentMessage.getMessageId()).setValue(currentMessage).addOnSuccessListener {
                FirebaseDatabase.getInstance().reference.child("chats").child(receiverRoom).child("messages").child(currentMessage.getMessageId()).setValue(currentMessage).addOnSuccessListener {
                    Log.v(ConstantValues.LOGCAT_TEST, "Reaction Int value added fro both sender and receiver in Firebase Database")
                }
            }

            true // true is closing popup, false is requesting a new selection
        })

        if(holder.javaClass == SentMessageViewHolder::class.java) {
            val sentViewHolder: SentMessageViewHolder = holder as SentMessageViewHolder
            sentViewHolder.sentTextTv.text = currentMessage.getMessageText()
            sentViewHolder.sentTimeTv.text = currentMessage.getMessageLocalTime()
            if(currentMessage.getMessageReaction() >=0) {
                sentViewHolder.sentReactionIv.visibility = View.VISIBLE
                sentViewHolder.sentReactionIv.setImageResource(reactionList[currentMessage.getMessageReaction()])
            } else {
                sentViewHolder.sentReactionIv.visibility = View.INVISIBLE
            }

            sentViewHolder.sentTextTv.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if(v != null && event != null) {
                        popup.onTouch(v,event)
                    }
                    return false
                }
            })

        } else {
            val receivedViewHolder: ReceivedMessageViewHolder = holder as ReceivedMessageViewHolder
            receivedViewHolder.receivedTextTv.text = currentMessage.getMessageText()
            receivedViewHolder.receivedTimeTv.text = currentMessage.getMessageLocalTime()
            if(currentMessage.getMessageReaction() >=0) {
                receivedViewHolder.receivedReactionIv.visibility = View.VISIBLE
                receivedViewHolder.receivedReactionIv.setImageResource(reactionList[currentMessage.getMessageReaction()])
            } else {
                receivedViewHolder.receivedReactionIv.visibility = View.INVISIBLE
            }

            receivedViewHolder.receivedTextTv.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if(v != null && event != null) {
                        popup.onTouch(v,event)
                    }
                    return false
                }
            })
        }
    }

    override fun getItemCount(): Int = chatList.size

    override fun getItemViewType(position: Int): Int {
        val currentUserUid: String? = FirebaseAuth.getInstance().currentUser?.uid
        if(currentUserUid != null) {
            return if(currentUserUid == chatList[position].getMessageSenderId()) {
                ConstantValues.ITEM_SENT_VIEW_TYPE
            } else {
                ConstantValues.ITEM_RECEIVED_VIEW_TYPE
            }
        } else {
            Log.v(ConstantValues.LOGCAT_TEST, "Current user uid is null")
        }
        return ConstantValues.NOT_AVAILABLE_INT
    }

    public fun getChatList(): ArrayList<MessageModel> = this.chatList
    public fun addValueToChatList(messageModel: MessageModel) {
        this.chatList.add(messageModel)
    }
}