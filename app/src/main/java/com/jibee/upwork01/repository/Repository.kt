package com.jibee.upwork01.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.jibee.upwork01.api.Resource
import com.jibee.upwork01.api.RetrofitBuilder
import com.jibee.upwork01.models.Stories.Result
import com.jibee.upwork01.models.Stories.Stories_All
import com.jibee.upwork01.models.postStory.PostStory
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Call
import java.lang.Exception
import java.util.*
import kotlin.Comparator

object Repository {

    var job: CompletableJob? = null

    //get stories
    fun getAllStories(
        userID: Int = 11,
        pageNumber: Int = 0,
        currentUserID: Int = 11
    ): LiveData<Resource<Stories_All>> {
        job = Job()

        return object : LiveData<Resource<Stories_All>>() {
            override fun onActive() {
                super.onActive()
                job?.let { job ->
                    CoroutineScope(IO + job).launch {
                        val storiesObject: Stories_All
                        try {
                            storiesObject = RetrofitBuilder.apiService.GetAllStories(
                                userID,
                                pageNumber,
                                currentUserID
                            )
                            withContext(Main) {
                                value = Resource.Success(storiesObject)
                                job.complete()
                            }
                        } catch (t: Throwable) {
                            //get retrofit exceptions
                            Log.d("Network-Error", "Network Error")
                            withContext(Main) {
                                value = Resource.Error(t.localizedMessage!!, null)
                                job.complete()
                            }
                        }
                    }
                }
            }
        }
    }


    //post a story
    fun addStory(postStory: PostStory): LiveData<Call<String>> {
        job = Job()

        return object : LiveData<Call<String>>() {
            override fun onActive() {
                super.onActive()
                job?.let { job ->
                    CoroutineScope(IO + job).launch {
                        val responce = RetrofitBuilder.apiService.AddStory(postStory)
                        Log.d("Post-response", "$responce")
                        withContext(Main) {
                            //print response message
                        }
                    }
                }
            }
        }
    }

    fun cancelJobs() {
        job?.cancel()
    }
}