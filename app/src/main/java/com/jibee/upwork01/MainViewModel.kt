package com.jibee.upwork01

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.jibee.upwork01.api.Resource
import com.jibee.upwork01.models.Stories.Result
import com.jibee.upwork01.models.Stories.Stories_All
import com.jibee.upwork01.models.postStory.PostStory
import com.jibee.upwork01.repository.Repository

class MainViewModel : ViewModel() {
    private val _postStory: MutableLiveData<PostStory> = MutableLiveData()
    private val _storyKey: MutableLiveData<String> = MutableLiveData()
    private val _userStoryKey: MutableLiveData<String> = MutableLiveData()
    private val _storyList: LiveData<ArrayList<Stories_All>> = MutableLiveData()


    //Get All Stories query
    fun setStoryKey(key: String) {
        _storyKey.value = key
    }
    fun setUserStoryKey(key: String) {
        _userStoryKey.value = key
    }

    val storyObject = Transformations.switchMap(_storyKey) {
        Repository.getAllStories()
    }

    val userStoryObject = Transformations.switchMap(_userStoryKey) {
        Repository.getAllStories()
    }

    init {
        setStoryKey("test")
        setUserStoryKey("test")
    }


    val postStoryResponse = Transformations
        .switchMap(_postStory) {
            Repository.addStory(it)
        }

    fun setStoryInfo(postStory: PostStory) {
        if (_postStory.value == postStory)
            return
        _postStory.value = postStory
    }

    //update seen status of story
    fun updateSeenStatus(storyId: Int, status: Boolean, userId: Int) =
        Repository.updateStorySeenStatus(
            storyId,
            status,
            userId
        )


    fun cancelJobs() = Repository.cancelJobs()
}
