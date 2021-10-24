package com.jibee.upwork01.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.withTransaction
import com.jibee.upwork01.api.RetrofitBuilder
import com.jibee.upwork01.db.StoriesDatabase
import com.jibee.upwork01.models.Stories.Stories_All
import com.jibee.upwork01.models.Stories.UserStory
import com.jibee.upwork01.models.postStory.PostStory
import com.jibee.upwork01.util.Resource
import com.jibee.upwork01.util.networkBoundResource
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class Repository(private val context: Context) {

    val db = StoriesDatabase.invoke(context)
    val storiesDao = db.getStoriesDao()

    val api = RetrofitBuilder.apiService

    fun getCurrentUserStory(userID: Int = 13) = networkBoundResource(
        query = {
            storiesDao.getAllUserStories()
        },
        fetch = {
            api.GetStoriesByUID(userID)
        },
        saveFetchResult = { userStory ->
            db.withTransaction {
                storiesDao.deleteAllUserStories()
                storiesDao.insertUserStories(userStory)
            }
        }
    )

    fun getAllFriendsStories(
        userID: Int = 13,
        pageNumber: Int = 0,
        currentUserID: Int = 13
    ) = networkBoundResource(
        query = {
            storiesDao.getAllFriendsStories()
        },
        fetch = {
            api.GetAllStories(userID, pageNumber, currentUserID)
        },
        saveFetchResult = { allStories ->
            db.withTransaction {
                storiesDao.deleteAllFriendsStories()
                storiesDao.insertFriendStories(allStories)
            }
        }
    )

    var job: CompletableJob? = null


    //get stories
    fun getStoriesByUID(
        userID: Int = 13
    ): MutableLiveData<Resource<UserStory>> {
        job = Job()

        return object : MutableLiveData<Resource<UserStory>>() {
            override fun onActive() {
                super.onActive()
                job?.let { job ->
                    CoroutineScope(IO + job).launch {
                        val storiesObject: UserStory
                        try {
                            storiesObject =
                                RetrofitBuilder.apiService.GetStoriesByUID(userId = userID)
                            withContext(Main) {
                                value = Resource.Success(storiesObject)
                                job.complete()
                            }
                        } catch (t: Throwable) {
                            //get retrofit exceptions
                            Log.d("Network-Error", "Network Error")
                            withContext(Main) {
                                value = Resource.Error(t, null)
                                job.complete()
                            }
                        }
                    }
                }
            }
        }
    }

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
                                value = Resource.Error(t, null)
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
                                value = Resource.Error(t, null)
                                job.complete()
                            }
                        }

                    }
                }
            }
        }
    }


    //update story seen status
    fun updateStorySeenStatus(shortVideoStoryId: Int, status: Boolean, userId: Int = 13) {
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