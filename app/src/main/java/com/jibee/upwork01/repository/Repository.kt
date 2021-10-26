package com.jibee.upwork01.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.jibee.upwork01.api.RetrofitBuilder
import com.jibee.upwork01.db.StoriesDatabase
import com.jibee.upwork01.models.Stories.Result
import com.jibee.upwork01.models.Stories.Stories
import com.jibee.upwork01.models.postStory.Story
import com.jibee.upwork01.util.Resource
import com.jibee.upwork01.util.networkBoundResource
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class Repository(private val context: Context) {

    private val db = StoriesDatabase.invoke(context)
    private val storiesDao = db.getStoriesDao()

    private val api = RetrofitBuilder.apiService

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
                storiesDao.insertFriendStories(storyObjectToStories(allStories))
            }
        }
    )

    private fun storyObjectToStories(response: Stories): ArrayList<Stories> {

        //we have to separate seen and un seen status

        val stories = arrayListOf<Stories>() // contain and return all stories seen and unSeen

        val ids = mutableSetOf<Int>()
        for (item in response.results) {
            ids.add(item.userId)
        }

        val userIDs = ids.toList()

        //iterate via userIds
        for ((index, i) in userIDs.withIndex()) {

            val itemList = ArrayList<Result>() //list of unSeen status
            val itemListSeen = ArrayList<Result>() //List of seen status

            for (item in response.results) {
                if (item.userId == i && !item.seenStatus) {
                    //add to unseen status arraylist
                    itemList.add(item)
                } else if (item.userId == i && item.seenStatus) {
                    //add to seen status arraylist
                    itemListSeen.add(item)
                }
            }

            //check if any of them are not null first
            if (itemList.size > 0) {
                //create a stories object
                stories.add(
                    Stories(
                        message = response.message,
                        page = response.page,
                        results = itemList,
                        statusCode = response.statusCode,
                        totalPages = index,
                        totalResults = itemList.count(),
                        seen = false
                    )
                )
            }

            if (itemListSeen.size > 0) {
                //create a stories object
                stories.add(
                    Stories(
                        message = response.message,
                        page = response.page,
                        results = itemList,
                        statusCode = response.statusCode,
                        totalPages = index,
                        totalResults = itemListSeen.count(),
                        seen = true
                    )
                )
            }
        }
        return stories
    }

    var job: CompletableJob? = null

    //post a story
    fun addStory(story: Story, userID: Int = 13): LiveData<Resource<String>> {
        job = Job()

        return object : LiveData<Resource<String>>() {
            override fun onActive() {
                super.onActive()
                job?.let { job ->
                    CoroutineScope(IO + job).launch {
                        try {
                            //it is a text status
                            RetrofitBuilder.apiService.AddStory(story, userID)
                            withContext(Main) {
                                value = Resource.Success(story.toString())
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