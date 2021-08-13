package com.jibee.upwork01.api

import com.jibee.upwork01.models.Stories.Stories_All
import com.jibee.upwork01.models.postStory.PostStory
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    //get stories from api
    @GET("GetFanFeedsShortVideoStory")
    suspend fun GetAllStories(
        @Query("userID") userId: Int = 11, //default value
        @Query("pageNumber") pageNumber: Int = 0, //default value
        @Query("currentUserID") currentUser: Int = 11, //default value
    ): Stories_All


    //add story to api
    @POST("AddShortVideoStory")
    suspend fun AddStory(@Body postStory: PostStory): String

}