package com.jibee.upwork01

import android.app.Application
import androidx.lifecycle.*
import com.jibee.upwork01.models.Stories.Stories_All
import com.jibee.upwork01.models.postStory.PostStory
import com.jibee.upwork01.repository.Repository

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _postStory: MutableLiveData<PostStory> = MutableLiveData()
    private val _storyKey: MutableLiveData<String> = MutableLiveData()
    private val _userStoryKey: MutableLiveData<String> = MutableLiveData()
    private val _storyList: LiveData<ArrayList<Stories_All>> = MutableLiveData()

    private val repository = Repository(application)

    val currentUserStories = Transformations.switchMap(_userStoryKey) {
        repository.getCurrentUserStory().asLiveData()
    }
    val friendsStories = Transformations.switchMap(_storyKey) {
        repository.getAllFriendsStories().asLiveData()
    }


    //Get All Stories query
    fun setStoryKey(key: String) {
        _storyKey.value = key
    }

    fun setUserStoryKey(key: String) {
        _userStoryKey.value = key
    }

    init {
        setStoryKey("test")
        setUserStoryKey("test")
    }


    val postStoryResponse = Transformations
        .switchMap(_postStory) {
            repository.addStory(it)
        }

    fun setStoryInfo(postStory: PostStory) {
        if (_postStory.value == postStory)
            return
        _postStory.value = postStory
    }

    //update seen status of story
    fun updateSeenStatus(storyId: Int, status: Boolean) =
        repository.updateStorySeenStatus(
            storyId,
            status
        )


    fun cancelJobs() = repository.cancelJobs()
}
