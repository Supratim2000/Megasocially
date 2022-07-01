package com.example.myapplication.ModelClasses

class User {
    private var loginType: String
    private var userName: String
    private var userEmail: String
    private var userPasswordAesEncrypted: String
    private var userId: String
    private var userDp: String
    private var userStatus: String

    //Empty constructor for Firebase
    constructor() {
        this.loginType = "-1"
        this.userName = "-1"
        this.userEmail = "-1"
        this.userPasswordAesEncrypted = "-1"
        this.userId = "-1"
        this.userDp = "-1"
        this.userStatus = ""
    }

    constructor(
        loginType: String,
        userName: String,
        userEmail: String,
        userPasswordAesEncrypted: String,
        userId: String,
        userDp: String,
        userStatus: String
    ) {
        this.loginType = loginType
        this.userName = userName
        this.userEmail = userEmail
        this.userPasswordAesEncrypted = userPasswordAesEncrypted
        this.userId = userId
        this.userDp = userDp
        this.userStatus = userStatus
    }

    constructor(
        loginType: String,
        userName: String,
        userEmail: String,
        userPasswordAesEncrypted: String,
        userId: String,
        userDp: String
    ) {
        this.loginType = loginType
        this.userName = userName
        this.userEmail = userEmail
        this.userPasswordAesEncrypted = userPasswordAesEncrypted
        this.userId = userId
        this.userDp = userDp
        this.userStatus = ""
    }

    //Getters
    public fun getLoginType(): String = this.loginType
    public fun getUserName(): String = this.userName
    public fun getUserEmail(): String = this.userEmail
    public fun getUserPasswordAesEncrypted(): String = this.userPasswordAesEncrypted
    public fun getUserId(): String = this.userId
    public fun getUserDp(): String = this.userDp
    public fun getUserStatus(): String = this.userStatus

    //Setters
    public fun setLoginType(loginType: String) {
        this.loginType = loginType
    }

    public fun setUserName(userName: String) {
        this.userName = userName
    }

    public fun setUserEmail(userEmail: String) {
        this.userEmail = userEmail
    }

    public fun setUserPasswordAesEncrypted(userPasswordAesEncrypted: String) {
        this.userPasswordAesEncrypted = userPasswordAesEncrypted
    }

    public fun setUserId(userId: String) {
        this.userId = userId
    }

    public fun setUserDp(userDp: String) {
        this.userDp = userDp
    }

    public fun setUserStatus(userStatus: String) {
        this.userStatus = userStatus
    }
}