package com.jibee.upwork01

import android.app.Application
import androidx.lifecycle.*
import com.jibee.upwork01.models.Stories.Stories
import com.jibee.upwork01.models.postStory.Story
import com.jibee.upwork01.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val friendsStory = MutableStateFlow(true)
    private val friendsStoryFlow = friendsStory.flatMapLatest {
        repository.getAllFriendsStories()
    }
    val stories = friendsStoryFlow.asLiveData()

    val userStory = MutableStateFlow(true)
    private val userStoryFlow = userStory.flatMapLatest {
        repository.getCurrentUserStory()
    }
    val userStories = userStoryFlow.asLiveData()


    private val _postStory: MutableLiveData<Story> = MutableLiveData()
    private val _storyKey: MutableLiveData<String> = MutableLiveData()
    private val _userStoryKey: MutableLiveData<String> = MutableLiveData()
    private val _storyList: LiveData<ArrayList<Stories>> = MutableLiveData()

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

    fun setStoryInfo(story: Story) {
        if (_postStory.value == story)
            return
        _postStory.value = story
    }

    //update seen status of story
    fun updateSeenStatus(storyId: Int, status: Boolean) =
        repository.updateStorySeenStatus(
            storyId,
            status
        )


    fun cancelJobs() = repository.cancelJobs()
}
