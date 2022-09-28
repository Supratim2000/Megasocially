package com.example.myapplication.ViewHolders

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class ReceivedMessageViewHolder: RecyclerView.ViewHolder {
    val receivedImageIv: ImageView
    val receivedImageTimeTv: TextView
    val receivedImageReactionIv: ImageView
    val receivedTextTv: TextView
    val receivedReactionIv: ImageView
    val receivedTimeTv: TextView

    constructor(itemView: View): super(itemView) {
        this.receivedImageIv = itemView.findViewById(R.id.received_image_iv)
        this.receivedImageTimeTv = itemView.findViewById(R.id.received_image_time_tv)
        this.receivedImageReactionIv = itemView.findViewById(R.id.received_image_reaction_iv)
        this.receivedTextTv = itemView.findViewById(R.id.received_text_tv)
        this.receivedReactionIv = itemView.findViewById(R.id.received_reaction_iv)
        this.receivedTimeTv = itemView.findViewById(R.id.received_time_tv)
    }
}