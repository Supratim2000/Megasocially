package com.example.myapplication.ViewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class ReceivedMessageViewHolder: RecyclerView.ViewHolder {
    val receivedTextTv: TextView
    val receivedReactionIv: ImageView
    val receivedTimeTv: TextView

    constructor(itemView: View): super(itemView) {
        this.receivedTextTv = itemView.findViewById(R.id.received_text_tv)
        this.receivedReactionIv = itemView.findViewById(R.id.received_reaction_iv)
        this.receivedTimeTv = itemView.findViewById(R.id.received_time_tv)
    }
}