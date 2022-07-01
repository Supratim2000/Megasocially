package com.example.myapplication.ViewHolders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import de.hdodenhof.circleimageview.CircleImageView

class UsersViewHolder: RecyclerView.ViewHolder {
    val userProfilePicture: CircleImageView
    val userDisplayName: TextView
    val userStatus: TextView

    constructor(itemView: View) : super(itemView) {
        this.userProfilePicture = itemView.findViewById(R.id.user_profile_picture)
        this.userDisplayName = itemView.findViewById(R.id.user_display_name)
        this.userStatus = itemView.findViewById(R.id.user_status)
    }
}