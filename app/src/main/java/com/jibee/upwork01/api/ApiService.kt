package com.jibee.upwork01.api

import com.jibee.upwork01.models.Stories.Stories_All
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiService {

    //get stories from api
    //GetFanFeedsShortVideoStory?userID=2&pageNumber=0&currentUserID=2
    @Headers(
        "content-type: application/json",
        "SessionToken: 30c8af89-3756-4150-8cfd-6df20d331107"
    )
    @GET("GetFanFeedsShortVideoStory")
    suspend fun GetAllStories(
        @Query("userID") userId: Int = 2, //default value
        @Query("pageNumber") pageNumber: Int = 0, //default value
        @Query("currentUserID") currentUser: Int = 2 //default value
    ): Stories_All

}