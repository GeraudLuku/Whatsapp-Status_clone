package com.jibee.upwork01.repo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jibee.upwork01.models.Src
import kotlinx.coroutines.launch

class StoriesViewModel(application: Application) : AndroidViewModel(application) {


    //function to add a story
    fun addStory(src: Src) {
        viewModelScope.launch {
            FirebaseService.postStatus(src,getApplication())
        }
    }


}