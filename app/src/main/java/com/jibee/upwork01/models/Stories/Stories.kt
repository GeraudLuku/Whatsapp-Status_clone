package com.jibee.upwork01.models.Stories


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "friends_stories")
data class Stories(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerializedName("Message")
    val message: String,
    @SerializedName("page")
    val page: Int,
    @SerializedName("results")
    val results: List<Result>,
    @SerializedName("StatusCode")
    val statusCode: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int,
    val seen: Boolean = true
) : Serializable {

    val stories: List<Result>
        get() {
            return results.filter {
                !it.isExpired
            }
        }

}