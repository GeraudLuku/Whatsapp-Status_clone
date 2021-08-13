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
        userID: Int = 11,
        pageNumber: Int = 0,
        currentUserID: Int = 11
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
    fun addStory(postStory: PostStory): LiveData<Resource<String>> {
        job = Job()

        return object : LiveData<Resource<String>>() {
            override fun onActive() {
                super.onActive()
                job?.let { job ->
                    CoroutineScope(IO + job).launch {
                        var response = ""
                        try {
                            //upload media to firebase if its text just add directly

                            //check if its text or media
                            if (postStory.mimeType.equals(".txt")) {
                                //it is a text status
                                response = RetrofitBuilder.apiService.AddStory(postStory)
                            } else {
                                //it upload media to Firebase Storage
                                val storageRef = Firebase.storage.reference
                                val ref =
                                    storageRef.child("uploads/" + UUID.randomUUID().toString())
                                val uploadTask = ref.putFile(Uri.parse(postStory.mediaURL))

                                val urlTask =
                                    uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                                        if (!task.isSuccessful) {
                                            task.exception?.let {
                                                throw it
                                            }
                                        }
                                        return@Continuation ref.downloadUrl
                                    }).addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val downloadUri = task.result
                                            //now get the url of the media and add it to the post object
                                            launch {
                                                postStory.mediaURL = downloadUri.toString()
                                                response =
                                                    RetrofitBuilder.apiService.AddStory(postStory)
                                            }
                                        } else {
                                            task.exception?.let {
                                                throw it
                                            }
                                        }
                                    }.addOnFailureListener {
                                        throw it
                                    }

                                withContext(Main) {
                                    //print response message
                                    value = Resource.Success(response)
                                    job.complete()
                                }
                            }
                        } catch (t: Throwable) {
                            //catch error
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

    fun cancelJobs() {
        job?.cancel()
    }
}