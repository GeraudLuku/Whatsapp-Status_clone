package com.jibee.upwork01.api

import com.jibee.upwork01.models.Stories.Stories_All
import com.jibee.upwork01.models.postStory.PostStory
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    //get stories from api
    @GET("GetFanFeedsShortVideoStory")
    suspend fun GetAllStories(
        @Query("userID") userId: Int = 13, //default value
        @Query("pageNumber") pageNumber: Int = 0, //default value
        @Query("currentUserID") currentUser: Int = 13, //default value
    ): Stories_All

    //get stories by UID from api
    @GET("GetShortVideoStorysByUserID")
    suspend fun GetStoriesByUID(
        @Query("userID") userId: Int = 13, //default value
    ): Stories_All


    //add story to api
    @POST("AddShortVideoStory")
    suspend fun AddStory(
        @Body postStory: PostStory,
        @Query("UserID") userId: Int = 13
    )

    @POST("UpdateStorySeenStatus")
    suspend fun UpdateSeenStatus(
        @Query("ShortVideoStoryId") shortVideoStoryId: Int,
        @Query("Status") status: Boolean,
        @Query("UserID") userId: Int
    )

}