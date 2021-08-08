package com.jibee.upwork01.repo

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jibee.upwork01.models.Src
import com.jibee.upwork01.models.Story
import kotlinx.coroutines.launch

class StoriesViewModel(application: Application) : AndroidViewModel(application) {

    //live data variable to hold the story items
    val _posts: MutableLiveData<ArrayList<Story>> = MutableLiveData()

    init {
        //get stories on view mdel attachment
        getStatus()
    }

    //function to add a story
    fun addStory(src: Src) {
        viewModelScope.launch {
            FirebaseService.postStatus(src, getApplication())
        }
    }

    fun getStatus() {

        Log.d("result", "hello world")
        //array list to hold array of src
        val list = arrayListOf<Src>()
        val listStory = arrayListOf<Story>()

        Firebase.database.reference.child("story")

            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    //clear list before using them again
                    list.clear()
                    listStory.clear()

                    for (snapshot in snapshot.children) {

                        for (dc in snapshot.child("content").children) {

                            Log.d("hell-1-", dc.value.toString())
                            //convert each of them to src object
                            val src = dc.getValue(Src::class.java)
                            list.add(src!!)
                            Log.d("count", "${list.size}")
                        }

                        //when you have the list of scr, now create a list of story
                        listStory.add(Story(content = list))

                        //final pass the value to livedata object
                        _posts.value = listStory
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


//            .addChildEventListener(object : ChildEventListener {
//                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//
//                    //inside each user enter the content
//                    Log.d("content", snapshot.child("content").value.toString())
//
//                    for (dc in snapshot.child("content").children) {
//                        Log.d("hell-1-", dc.value.toString())
//                        //convert each of them to src object
//                        val src = dc.getValue(Src::class.java)
//                        list.add(src!!)
//                        Log.d("count", "${list.size}")
//                    }
//
//                    //when you have the list of scr, now create a list of story
//                    listStory.add(Story(content = list))
//
//                    //final pass the value to livedata object
//                    _posts.value = listStory
//                }
//
//                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//
//                }
//
//                override fun onChildRemoved(snapshot: DataSnapshot) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//            })
    }

}