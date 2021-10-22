package com.jibee.upwork01.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.jibee.upwork01.api.Resource
import com.jibee.upwork01.api.RetrofitBuilder
import com.jibee.upwork01.models.Stories.Stories_All
import com.jibee.upwork01.models.postStory.PostStory
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.util.*

object Repository {

    var job: CompletableJob? = null

    //get stories
    fun getAllStories(
        userID: Int = 13,
        pageNumber: Int = 0,
        currentUserID: Int = 13
    ): MutableLiveData<Resource<Stories_All>> {
        job = Job()

        return object : MutableLiveData<Resource<Stories_All>>() {
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
    fun addStory(postStory: PostStory, userID: Int = 13): LiveData<Resource<String>> {
        job = Job()

        return object : LiveData<Resource<String>>() {
            override fun onActive() {
                super.onActive()
                job?.let { job ->
                    CoroutineScope(IO + job).launch {
                        try {
                            //it is a text status
                            RetrofitBuilder.apiService.AddStory(postStory, userID)
                            withContext(Main) {
                                value = Resource.Success(postStory.toString())
                                job.complete()
                            }

                        } catch (t: Throwable) {
                            //catch error
                            Log.d("Network-Error", t.localizedMessage)
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


    //update story seen status
    fun updateStorySeenStatus(shortVideoStoryId: Int, status: Boolean, userId: Int) {
        CoroutineScope(IO).launch {
            try {
                RetrofitBuilder.apiService.UpdateSeenStatus(
                    shortVideoStoryId,
                    status,
                    userId
                )
            } catch (t: Throwable) {
                Log.d("Update-Error", t.localizedMessage)
            }

        }
    }

    fun cancelJobs() {
        job?.cancel()
    }
}