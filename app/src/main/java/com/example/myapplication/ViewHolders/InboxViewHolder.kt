package com.example.myapplication.ViewHolders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class InboxViewHolder: RecyclerView.ViewHolder {
    val inboxProfilePictureCiv: de.hdodenhof.circleimageview.CircleImageView
    val inboxDisplayNameTv: TextView
    val inboxLastMessageTimeTv: TextView
    val inboxLastTextTv: TextView
    val inboxUnreadCountTv: TextView
    constructor(itemView: View): super(itemView) {
        this.inboxProfilePictureCiv = itemView.findViewById(R.id.inbox_profile_picture_civ)
        this.inboxDisplayNameTv = itemView.findViewById(R.id.inbox_display_name_tv)
        this.inboxLastMessageTimeTv = itemView.findViewById(R.id.inbox_last_message_time_tv)
        this.inboxLastTextTv = itemView.findViewById(R.id.inbox_last_text_tv)
        this.inboxUnreadCountTv = itemView.findViewById(R.id.inbox_unread_count_tv)
    }
}