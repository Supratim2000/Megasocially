package com.example.myapplication.ViewHolders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class SentMessageViewHolder: RecyclerView.ViewHolder {
    val sentImageIv: ImageView
    val sentImageTimeTv: TextView
    val sentImageReactionIv: ImageView
    val sentImageIvLayout: LinearLayout
    val sentTextTv: TextView
    val sentReactionIv: ImageView
    val sentTimeTv: TextView

    constructor(itemView: View): super(itemView) {
        this.sentImageIv = itemView.findViewById(R.id.sent_image_iv)
        this.sentImageTimeTv = itemView.findViewById(R.id.sent_image_time_tv)
        this.sentImageReactionIv = itemView.findViewById(R.id.sent_image_reaction_iv)
        this.sentImageIvLayout = itemView.findViewById(R.id.sent_image_iv_layout)
        this.sentTextTv = itemView.findViewById(R.id.sent_text_tv)
        this.sentReactionIv = itemView.findViewById(R.id.sent_reaction_iv)
        this.sentTimeTv = itemView.findViewById(R.id.sent_time_tv)
    }
}