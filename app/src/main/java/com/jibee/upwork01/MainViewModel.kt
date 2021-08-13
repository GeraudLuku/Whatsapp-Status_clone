package com.jibee.upwork01

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.jibee.upwork01.api.Resource
import com.jibee.upwork01.models.Stories.QueryStory
import com.jibee.upwork01.models.Stories.Stories_All
import com.jibee.upwork01.models.postStory.PostStory
import com.jibee.upwork01.repository.Repository

class MainViewModel : ViewModel() {
    private val _postStory: MutableLiveData<PostStory> = MutableLiveData()
    var storyObject: LiveData<Resource<Stories_All>> = MutableLiveData()


    init {
        getAllStories()
    }

    //get all stories
    fun getAllStories() {
        storyObject = Repository.getAllStories()
    }

    val postStoryResponse = Transformations
        .switchMap(_postStory) {
            Repository.addStory(it)
        }

    fun setStoryInfo(postStory: PostStory) {
        val update = postStory
        if (_postStory.value == postStory)
            return
        _postStory.value = update
    }


    fun cancelJobs() = Repository.cancelJobs()
}
