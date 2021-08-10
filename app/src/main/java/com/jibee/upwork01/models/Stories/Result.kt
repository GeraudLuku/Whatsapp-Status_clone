package com.jibee.upwork01.models.Stories


import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("AddedDateAndTime")
    val addedDateAndTime: String,
    @SerializedName("DateTimeForDb")
    val dateTimeForDb: String,
    @SerializedName("Engagement")
    val engagement: Int,
    @SerializedName("ID")
    val iD: Int,
    @SerializedName("MediaURL")
    val mediaURL: String,
    @SerializedName("MimeType")
    val mimeType: String,
    @SerializedName("ProfileClicks")
    val profileClicks: Int,
    @SerializedName("Reach")
    val reach: Int,
    @SerializedName("SeenStatus")
    val seenStatus: Boolean,
    @SerializedName("Shares")
    val shares: Int,
    @SerializedName("UserId")
    val userId: Int,
    @SerializedName("UserViewModel")
    val userViewModel: UserViewModel
)