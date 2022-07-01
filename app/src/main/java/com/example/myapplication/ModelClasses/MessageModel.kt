package com.example.myapplication.ModelClasses

class MessageModel {
    private var messageId: String
    private var messageText: String
    private var messageSenderId: String
    private var messageReceiverId: String
    private var messageReaction: Int
    private var messageTimestamp: Long
    private var messageLocalTime: String
    private var messageType: String

    constructor() {
        this.messageId = "-1"
        this.messageText = "-1"
        this.messageSenderId = "-1"
        this.messageReceiverId = "-1"
        this.messageReaction = -1
        this.messageTimestamp = -1L
        this.messageLocalTime="-1"
        this.messageType = "-1"
    }

    constructor(
        messageId: String,
        messageText: String,
        messageSenderId: String,
        messageReceiverId: String,
        messageReaction: Int,
        messageTimestamp: Long,
        messageLocalTime: String,
        messageType: String
    ) {
        this.messageId = messageId
        this.messageText = messageText
        this.messageSenderId = messageSenderId
        this.messageReceiverId = messageReceiverId
        this.messageReaction = messageReaction
        this.messageTimestamp = messageTimestamp
        this.messageLocalTime=messageLocalTime
        this.messageType = messageType
    }

    //Getters
    public fun getMessageId(): String = this.messageId
    public fun getMessageText(): String = this.messageText
    public fun getMessageSenderId(): String = this.messageSenderId
    public fun getMessageReceiverId(): String = this.messageReceiverId
    public fun getMessageReaction(): Int = this.messageReaction
    public fun getMessageTimestamp(): Long = this.messageTimestamp
    public fun getMessageLocalTime(): String = this.messageLocalTime
    public fun getMessageType(): String = this.messageType

    //Setters
    public fun setMessageId(messageId: String){
        this.messageId = messageId
    }
    public fun setMessageText(messageText: String){
        this.messageText = messageText
    }
    public fun setMessageSenderId(messageSenderId: String){
        this.messageSenderId = messageSenderId
    }
    public fun setMessageReceiverId(messageReceiverId: String){
        this.messageReceiverId = messageReceiverId
    }
    public fun setMessageReaction(messageReaction: Int){
        this.messageReaction = messageReaction
    }
    public fun setMessageTimestamp(messageTimestamp: Long) {
        this.messageTimestamp = messageTimestamp
    }
    public fun setMessageLocalTime(messageLocalTime: String) {
        this.messageLocalTime = messageLocalTime
    }
    public fun setMessageType(messageType: String){
        this.messageType = messageType
    }
}