package com.jibee.upwork01.features

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.appexecutors.picker.Picker
import com.appexecutors.picker.Picker.Companion.PICKED_MEDIA_LIST
import com.appexecutors.picker.Picker.Companion.REQUEST_CODE_PICKER
import com.appexecutors.picker.utils.PickerOptions
import com.jibee.upwork01.MainViewModel
import com.jibee.upwork01.R
import com.jibee.upwork01.adapters.StoriesAdapter
import com.jibee.upwork01.models.Stories.Result
import com.jibee.upwork01.models.Stories.Stories_All
import com.jibee.upwork01.util.TimeAgo
import com.jibee.upwork01.util.URIPathHelper
import com.theartofdev.edmodo.cropper.CropImage
import com.videotrimmer.library.utils.CompressOption
import com.videotrimmer.library.utils.TrimVideo
import kotlinx.android.synthetic.main.fragment_main.*
import java.net.URLConnection
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


class MainFragment : Fragment(), StoriesAdapter.OnItemClickedListener {

    private lateinit var navController: NavController

    private lateinit var mainViewModel: MainViewModel

    private val storyList: ArrayList<Stories_All> = ArrayList()

    private lateinit var adapter: StoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //set toolbar on fragment
        if (activity is AppCompatActivity) {
            setHasOptionsMenu(true)
            (activity as AppCompatActivity).setSupportActionBar(toolbar)

            Log.d("ActionBar", "Action Bar set")
        }

        //init recyclerview
        adapter = StoriesAdapter(storyList, this, requireContext())
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)

        //adding a divider
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            LinearLayoutManager.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)


        //configure nav controller
        navController = findNavController()

        //subscribe to the view model
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        //listen to incoming story object
        mainViewModel.storyObject.observe(viewLifecycleOwner, Observer {

            //check if its a success or error
            if (it.message != null) {
                //show retry error on screen with possibility to retry
                retryIndicator.visibility = View.VISIBLE
                Log.d("Network-Error", it.message)
            } else {

                //load stories and hide text view
                emptyIndicator.visibility = View.INVISIBLE
                retryIndicator.visibility = View.INVISIBLE

                val response = it.data!!
                storyList.clear()

                when (response.statusCode) {
                    200 -> {
                        //loop all results and get userIds
                        setupDataToView(response)
                    }
                    404 -> {
                        //no stories so show textview indicator
                        emptyIndicator.visibility = View.VISIBLE
                    }
                    417 -> {
                        //error: sessionToken incorrect
                        Log.d("error-retrofit", "incorrect sessionToken")
                        retryIndicator.visibility = View.VISIBLE
                    }
                }
            }
        })


        val mPickerOptions =
            PickerOptions.init().apply {
                maxCount = 1                        //maximum number of images/videos to be picked
                //maxVideoDuration = 30               //maximum duration for video capture in seconds
                allowFrontCamera = true             //allow front camera use
                excludeVideos = false               //exclude or include video functionalities
            }


        //onclick listener for the camera image button
        cameraBtn.setOnClickListener {
            Picker.startPicker(this, mPickerOptions)    //this -> context of Activity or Fragment
        }

        //onclick listener for status text image button
        status.setOnClickListener {
            navController.navigate(R.id.action_mainFragment_to_statusFragment)
        }

        //onClick of retry
        retryIndicator.setOnClickListener {
            mainViewModel.setStoryKey("test")
        }


    }

    private fun setupDataToView(response: Stories_All) {
        val ids = mutableSetOf<Int>()
        for (item in response.results) {
            ids.add(item.userId)
        }

        val userIDs = ids.toList()

        //iterate via it
        var index = 0
        for (i in userIDs) {
            val itemList = ArrayList<Result>()
            for (item in response.results) {
                if (item.userId.equals(i)) {
                    itemList.add(item)
                }
            }
            //create a story_all object
            storyList.add(
                Stories_All(
                    response.message,
                    response.page,
                    itemList,
                    response.statusCode,
                    index,
                    itemList.count()
                )
            )
            //sort them in DESC order of time added
            storyList.sortWith(Comparator { o1: Stories_All, o2: Stories_All ->
                o2.totalPages.compareTo(o1.totalPages)
            })

            index++

            adapter.notifyDataSetChanged()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICKER) {
            val mImageList =
                data?.getStringArrayListExtra(PICKED_MEDIA_LIST) as ArrayList //List of selected/captured images/videos
            mImageList.map {

                val uri = Uri.parse(it) //convert the string to a Uri path
                val uriPathHelper = URIPathHelper()
                val filePath =
                    uriPathHelper.getPath(requireContext(), uri) //get absolute path from Uri

                Log.d("CameraResult", filePath.toString())

                //check if it is an image or a video
                if (isImageFile(filePath)) {
                    //it is an image file
                    Log.d("MIME-TYPE", "It is an Image")
                    //send to crop function/Fragment
                    CropImage.activity(uri)
                        .start(requireContext(), this);
                } else {
                    //it is obviously a video file
                    Log.d("MIME-TYPE", "It is a video")
                    //send video to trimming intent
                    TrimVideo.activity(uri.toString())
                        .setCompressOption(CompressOption(8))
                        .start(this)
                }

            }
        }

        //for the image cropper activity
        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode === RESULT_OK) {
                val resultUri = result.uri
                Log.d("Cropper-Pass", result.toString())
                //send uri to Details fragment
                val action =
                    MainFragmentDirections.actionMainFragmentToDetailFragment(resultUri.toString())
                navController.navigate(action)

            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.d("Cropper-Err", error.toString())
            }
        }

        //for the video trimmer activity
        if (requestCode === TrimVideo.VIDEO_TRIMMER_REQ_CODE && data != null) {
            val uri = Uri.parse(TrimVideo.getTrimmedVideoPath(data))
            Log.d("Video-Trim", "Trimmed path:: $uri")
            //send uri to Details fragment
            val action = MainFragmentDirections.actionMainFragmentToDetailFragment(uri.toString())
            navController.navigate(action)
        }
    }

    fun isImageFile(path: String?): Boolean {
        val mimeType: String = URLConnection.guessContentTypeFromName(path)
        return mimeType.startsWith("image")
    }

    override fun onItemCLicked(story: Stories_All) {
        Log.d("Story-Click", "${story.results}")
        //goto story view fragment
        val action = MainFragmentDirections.actionMainFragmentToStoryViewFragment(story)
        navController.navigate(action)
    }

}