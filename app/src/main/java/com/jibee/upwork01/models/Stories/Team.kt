package com.jibee.upwork01.models.Stories


import com.google.gson.annotations.SerializedName

data class Team(
    @SerializedName("ID")
    val iD: Int,
    @SerializedName("team_badge")
    val teamBadge: String,
    @SerializedName("team_country")
    val teamCountry: String,
    @SerializedName("team_key")
    val teamKey: String,
    @SerializedName("team_name")
    val teamName: String
)