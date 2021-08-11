package com.jibee.upwork01.models.Stories


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Stories_All(
    @SerializedName("Message")
    val message: String,
    @SerializedName("page")
    val page: Int,
    @SerializedName("results")
    val results: ArrayList<Result>,
    @SerializedName("StatusCode")
    val statusCode: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
):Serializable