package com.jibee.upwork01.models.Stories


import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

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
) : Serializable {

    fun getCreatedTime(): Long {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        return format.parse(addedDateAndTime).time
    }

    val isExpired
    get() = ((getCreatedTime() / (1000 * 60 * 60)) % 24).toInt() > 24
}