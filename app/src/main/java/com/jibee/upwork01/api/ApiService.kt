package com.jibee.upwork01.api

import com.jibee.upwork01.models.Stories.Stories
import com.jibee.upwork01.models.Stories.UserStory
import com.jibee.upwork01.models.postStory.Story
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    //get stories from api
    @GET("GetFanFeedsShortVideoStory")
    suspend fun GetAllStories(
        @Query("userID") userId: Int = 13, //default value
        @Query("pageNumber") pageNumber: Int = 0, //default value
        @Query("currentUserID") currentUser: Int = 13, //default value
    ): Stories

    //get stories by UID from api
    @GET("GetShortVideoStorysByUserID")
    suspend fun GetStoriesByUID(
        @Query("userID") userId: Int = 13, //default value
        @Query("pageNumber") pageNumber: Int = 0, //default value
        @Query("currentUserID") currentUser: Int = 13, //default value
    ): UserStory


    //add story to api
    @POST("AddShortVideoStory")
    suspend fun AddStory(
        @Body story: Story,
        @Query("UserID") userId: Int = 13
    )

    @POST("UpdateStorySeenStatus")
    suspend fun UpdateSeenStatus(
        @Query("ShortVideoStoryId") shortVideoStoryId: Int,
        @Query("Status") status: Boolean,
        @Query("UserID") userId: Int
    )

}