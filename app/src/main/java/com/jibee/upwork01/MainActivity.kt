package com.jibee.upwork01

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //attach activity to view model
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.cancelJobs()
    }

}