package com.jibee.upwork01.models.postStory


import com.google.gson.annotations.SerializedName

data class PostStory(
    @SerializedName("AddedDateAndTime")
    val addedDateAndTime: String,
    @SerializedName("DateTimeForDb")
    val dateTimeForDb: String = addedDateAndTime,
    @SerializedName("Engagement")
    val engagement: Int = 0,
    @SerializedName("ID") //autogenerated
    val iD: Int = 0,
    @SerializedName("MediaURL")
    var mediaURL: String,
    @SerializedName("MimeType")
    val mimeType: String,
    @SerializedName("ProfileClicks")
    val profileClicks: Int = 0,
    @SerializedName("Reach")
    val reach: Int = 0,
    @SerializedName("Shares")
    val shares: Int = 0,
    @SerializedName("UserId")
    val userId: Int = 11
)