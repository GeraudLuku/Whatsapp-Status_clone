package com.jibee.upwork01.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jibee.upwork01.models.Stories.Result

class Converters {

    inline fun <reified T> genericType() = object : TypeToken<T>() {}.type

    @TypeConverter
    fun fromResultList(results: List<Result>): String {
        return Gson().toJson(results)
    }

    @TypeConverter
    fun toResultList(results: String): List<Result> {
        val type = genericType<List<Result>>()
        return Gson().fromJson(results, type)
    }
}