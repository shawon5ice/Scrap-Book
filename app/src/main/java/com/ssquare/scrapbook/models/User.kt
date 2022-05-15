package com.ssquare.scrapbook.models

class User {
    private var username: String = ""
    private var fullname: String = ""
    private var bio: String = ""
    private var profile_image: String = ""
    private var uid: String = ""
    private var email: String = ""
    private var followingList: MutableList<String> = ArrayList()
    private var followersList: MutableList<String> = ArrayList()

    constructor()

    constructor(userName:String, fullName:String, bio:String, profileImage:String, uid:String,email:String,followingList: MutableList<String>,followersList: MutableList<String>){
        this.username = userName
        this.fullname = userName
        this.bio = userName
        this.profile_image = profileImage
        this.email = email
        this.uid= uid
        this.followersList = followersList
        this.followingList = followingList
    }

    fun getUserName(): String? {
        return username
    }

    fun setUserName(userName: String?) {
        this.username = userName!!
    }

    fun getFullName(): String? {
        return fullname
    }

    fun setFullName(fullName: String?) {
        this.fullname = fullName!!
    }

    fun getBio(): String? {
        return bio
    }

    fun setBio(bio: String?) {
        this.bio = bio!!
    }

    fun getProfileImage(): String? {
        return profile_image
    }

    fun setProfileImage(profileImage: String?) {
        this.profile_image = profileImage!!
    }

    fun getUid(): String? {
        return uid
    }

    fun setUid(uid: String?) {
        this.uid = uid!!
    }

    fun getEmail(): String? {
        return email
    }

    fun setEmail(email: String?) {
        this.email = email!!
    }


}