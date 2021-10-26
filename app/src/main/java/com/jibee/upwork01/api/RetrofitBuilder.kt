package com.jibee.upwork01.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitBuilder {
    const val BASE_URL = "http://staging.footballbuzz.net/api/v1/"
    const val TOKEN = "c6cd811a-af3a-4ea5-8509-c12484c302d6"

    //create Logger
    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    //custom interceptor to apply Headers application wide
    val headerInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()

            Log.d("Respose-Method", request.method) // [GET, POST]

            request = request.newBuilder()
                .addHeader("content-type", "application/json")
                .addHeader("SessionToken", TOKEN)
                .build()

            return chain.proceed(request)
        }
    }

    // create OkHtp Client
    private val okHttp = OkHttpClient.Builder()
        .callTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(headerInterceptor)
        .addInterceptor(logger)


    private val retrofitBuilder: Retrofit.Builder by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttp.build())
    }

    val apiService: ApiService by lazy {
        retrofitBuilder
            .build()
            .create(ApiService::class.java)
    }
}