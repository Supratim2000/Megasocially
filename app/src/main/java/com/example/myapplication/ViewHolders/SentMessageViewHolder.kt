package com.example.myapplication.ViewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class SentMessageViewHolder: RecyclerView.ViewHolder {
    val sentTextTv: TextView
    val sentReactionIv: ImageView
    val sentTimeTv: TextView

    constructor(itemView: View): super(itemView) {
        this.sentTextTv = itemView.findViewById(R.id.sent_text_tv)
        this.sentReactionIv = itemView.findViewById(R.id.sent_reaction_iv)
        this.sentTimeTv = itemView.findViewById(R.id.sent_time_tv)
    }
}