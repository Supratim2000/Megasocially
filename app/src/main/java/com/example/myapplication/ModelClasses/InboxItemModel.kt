package com.example.myapplication.ModelClasses

class InboxItemModel {
    private var inboxChatUnreadCount: Int
    private var inboxChatUserUid: String
    private var inboxChatUserUidName: String
    private var inboxChatUserUidProfilePicture: String
    private var inboxChatLastMessageFromUid: String
    private var inboxChatLastMessageFromUidUserName: String
    private var inboxChatLastMessage: String
    private var inboxChatLastMessageTimestamp: Long
    private var inboxChatLastMessageTime: String

    constructor() {
        this.inboxChatUnreadCount = 0
        this.inboxChatUserUid = "-1"
        this.inboxChatUserUidName = "-1"
        this.inboxChatUserUidProfilePicture = "-1"
        this.inboxChatLastMessageFromUid = "-1"
        this.inboxChatLastMessageFromUidUserName = "-1"
        this.inboxChatLastMessage = "-1"
        this.inboxChatLastMessageTimestamp = -1L
        this.inboxChatLastMessageTime = "-1"
    }

    constructor(
        inboxChatUnreadCount: Int,
        inboxChatUserUid: String,
        inboxChatUserUidName: String,
        inboxChatUserUidProfilePicture: String,
        inboxChatLastMessageFromUid: String,
        inboxChatLastMessageFromUidUserName: String,
        inboxChatLastMessage: String,
        inboxChatLastMessageTimestamp: Long,
        inboxChatLastMessageTime: String
    ) {
        this.inboxChatUnreadCount = inboxChatUnreadCount
        this.inboxChatUserUid = inboxChatUserUid
        this.inboxChatUserUidName = inboxChatUserUidName
        this.inboxChatUserUidProfilePicture = inboxChatUserUidProfilePicture
        this.inboxChatLastMessageFromUid = inboxChatLastMessageFromUid
        this.inboxChatLastMessageFromUidUserName = inboxChatLastMessageFromUidUserName
        this.inboxChatLastMessage = inboxChatLastMessage
        this.inboxChatLastMessageTimestamp = inboxChatLastMessageTimestamp
        this.inboxChatLastMessageTime = inboxChatLastMessageTime
    }


    //getters
    public fun getInboxChatUnreadCount(): Int = this.inboxChatUnreadCount
    public fun getInboxChatUserUid(): String = this.inboxChatUserUid
    public fun getInboxChatUserUidName(): String = this.inboxChatUserUidName
    public fun getInboxChatUserUidProfilePicture(): String = this.inboxChatUserUidProfilePicture
    public fun getInboxChatLastMessageFromUid(): String = this.inboxChatLastMessageFromUid
    public fun getInboxChatLastMessageFromUidUserName(): String = this.inboxChatLastMessageFromUidUserName
    public fun getInboxChatLastMessage(): String = this.inboxChatLastMessage
    public fun getInboxChatLastMessageTimestamp(): Long = this.inboxChatLastMessageTimestamp
    public fun getInboxChatLastMessageTime(): String = this.inboxChatLastMessageTime

    //setter
    public fun setInboxChatUnreadCount(inboxChatUnreadCount: Int) {
        this.inboxChatUnreadCount = inboxChatUnreadCount
    }
    public fun setInboxChatUserUid(inboxChatUserUid: String) {
        this.inboxChatUserUid = inboxChatUserUid
    }
    public fun setInboxChatUserUidName(inboxChatUserUidName: String) {
        this.inboxChatUserUidName = inboxChatUserUidName
    }
    public fun setInboxChatUserUidProfilePicture(inboxChatUserUidProfilePicture: String) {
        this.inboxChatUserUidProfilePicture = inboxChatUserUidProfilePicture
    }
    public fun setInboxChatLastMessageFromUid(inboxChatLastMessageFromUid: String) {
        this.inboxChatLastMessageFromUid = inboxChatLastMessageFromUid
    }
    public fun setInboxChatLastMessageFromUidUserName(inboxChatLastMessageFromUidUserName: String) {
        this.inboxChatLastMessageFromUidUserName = inboxChatLastMessageFromUidUserName
    }
    public fun setInboxChatLastMessage(inboxChatLastMessage: String) {
        this.inboxChatLastMessage = inboxChatLastMessage
    }
    public fun setInboxChatLastMessageTimestamp(inboxChatLastMessageTimestamp: Long) {
        this.inboxChatLastMessageTimestamp =inboxChatLastMessageTimestamp
    }
    public fun setInboxChatLastMessageTime(inboxChatLastMessageTime: String) {
        this.inboxChatLastMessageTime = inboxChatLastMessageTime
    }
}