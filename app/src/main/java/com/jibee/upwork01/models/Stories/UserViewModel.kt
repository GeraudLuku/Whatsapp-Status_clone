package com.jibee.upwork01.models.Stories


import com.google.gson.annotations.SerializedName

data class UserViewModel(
    @SerializedName("FullName")
    val fullName: String,
    @SerializedName("isVerified")
    val isVerified: Boolean,
    @SerializedName("NotificationToken")
    val notificationToken: String,
    @SerializedName("ProfilePhoto")
    val profilePhoto: String,
    @SerializedName("Team")
    val team: Team,
    @SerializedName("UserID")
    val userID: Int,
    @SerializedName("UserName")
    val userName: String
)