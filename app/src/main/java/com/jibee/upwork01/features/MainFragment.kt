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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.appexecutors.picker.Picker
import com.appexecutors.picker.Picker.Companion.PICKED_MEDIA_LIST
import com.appexecutors.picker.Picker.Companion.REQUEST_CODE_PICKER
import com.appexecutors.picker.utils.PickerOptions
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.jibee.upwork01.MainViewModel
import com.jibee.upwork01.R
import com.jibee.upwork01.adapters.StoriesAdapter
import com.jibee.upwork01.models.Stories.Result
import com.jibee.upwork01.models.Stories.Stories_All
import com.jibee.upwork01.util.Resource
import com.jibee.upwork01.util.URIPathHelper
import com.theartofdev.edmodo.cropper.CropImage
import com.videotrimmer.library.utils.CompressOption
import com.videotrimmer.library.utils.TrimVideo
import kotlinx.android.synthetic.main.fragment_main.*
import java.net.URLConnection


class MainFragment : Fragment(), StoriesAdapter.OnItemClickedListener {

    private lateinit var navController: NavController

    private lateinit var mainViewModel: MainViewModel

    private val storyList: ArrayList<Stories_All> = ArrayList()
    private val storyListSeen: ArrayList<Stories_All> = ArrayList()

    private lateinit var adapter: StoriesAdapter
    private lateinit var adapterSeen: StoriesAdapter

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
        }

        //configure nav controller
        navController = findNavController()

        //subscribe to the view model
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        initializeAdapters()
        registerObservers()
        setListeners()
    }

    private fun initializeAdapters() {
        //init recycler views
        adapter = StoriesAdapter(storyList, this, requireContext())
        adapterSeen = StoriesAdapter(storyListSeen, this, requireContext())

        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)

        recyclerView_viewed.adapter = adapterSeen
        recyclerView_viewed.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)

        //adding a divider
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            LinearLayoutManager.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView_viewed.addItemDecoration(dividerItemDecoration)
    }

    private fun setListeners() {
        //onclick listener for the camera image button
        val mPickerOptions =
            PickerOptions.init().apply {
                maxCount = 1                        //maximum number of images/videos to be picked
                //maxVideoDuration = 30               //maximum duration for video capture in seconds
                allowFrontCamera = true             //allow front camera use
                excludeVideos = false               //exclude or include video functionalities
            }
        cameraBtn.setOnClickListener {
            Picker.startPicker(
                requireActivity(),
                mPickerOptions
            )    //this -> context of Activity or Fragment
        }

        //onclick listener for status text image button
        status.setOnClickListener {
            navController.navigate(R.id.action_mainFragment_to_statusFragment)
        }

        //onClick of retry
        retryIndicator.setOnClickListener {
            mainViewModel.setStoryKey("test")
        }

        //on click of user story retry
        retryUserStoryIndicator.setOnClickListener {
            mainViewModel.setUserStoryKey("test")
        }

    }

    private fun registerObservers() {
        //listen to incoming current user stories
        mainViewModel.currentUserStories.observe(viewLifecycleOwner) { result ->

            if (result is Resource.Error) {
                Snackbar.make(requireView(), "You are Offline", Snackbar.LENGTH_LONG).show()
                retryUserStoryIndicator.visibility = View.VISIBLE
            } else {
                retryUserStoryIndicator.visibility = View.INVISIBLE

                val response = result?.data
                storyList.clear()
                storyListSeen.clear()

                when (response?.statusCode) {
                    200 -> {
                        //data available
                        circular_status_view.setPortionsCount(response.totalResults)
                        circular_status_view.setPortionsColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.custom2
                            )
                        )
                        Glide.with(requireView())
                            .load(response.results[0].userViewModel.profilePhoto)
                            .into(profile_image)

                        add_story_indicator.text = "View my stories"

                        //show user stories onclick of the profile image
                        profile_image.setOnClickListener {
                            val action =
                                MainFragmentDirections.actionMainFragmentToStoryViewFragment(
                                    Stories_All(
                                        response.message,
                                        response.page,
                                        response.results,
                                        response.statusCode,
                                        response.totalPages,
                                        response.totalResults
                                    )
                                )
                            navController.navigate(action)
                        }

                    }
                    417 -> {
                        //error: sessionToken incorrect
                        retryUserStoryIndicator.visibility = View.VISIBLE
                    }
                }

                val stories = result.data
                stories?.run {
                    circular_status_view.setPortionsCount(this.totalResults)
                    circular_status_view.setPortionsColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.custom2
                        )
                    )
                    add_story_indicator.text = "View my stories"
                }
            }
        }

        //listen to incoming story object
        mainViewModel.friendsStories.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Error) {
                Snackbar.make(requireView(), "You are Offline", Snackbar.LENGTH_LONG).show()
            } else {

                //load stories and hide text view
                emptyIndicator.visibility = View.INVISIBLE
                retryIndicator.visibility = View.INVISIBLE

                val response = result?.data
                storyList.clear()
                storyListSeen.clear()

                when (response?.statusCode) {
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
        }

    }

    private fun setupDataToView(response: Stories_All) {

        //we have to separate seen and un seen status

        val ids = mutableSetOf<Int>()
        for (item in response.results) {
            ids.add(item.userId)
        }

        val userIDs = ids.toList()

        //iterate via it
        var index = 0
        for (i in userIDs) {

            val itemList = ArrayList<Result>()
            val itemListSeen = ArrayList<Result>()
            for (item in response.results) {
                if (item.userId.equals(i) && !item.seenStatus) {
                    //add to unseen arraylist
                    itemList.add(item)
                } else if (item.userId.equals(i) && item.seenStatus) {
                    //add to seen arraylist
                    itemListSeen.add(item)
                }
            }

            //check if any of them are not null first
            if (itemList.size > 0) {
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
                adapter.notifyDataSetChanged()
            }

            if (itemListSeen.size > 0) {
                //create a story_all object
                storyListSeen.add(
                    Stories_All(
                        response.message,
                        response.page,
                        itemListSeen,
                        response.statusCode,
                        index,
                        itemListSeen.count()
                    )
                )
                //sort them in DESC order of time added
                storyListSeen.sortWith(Comparator { o1: Stories_All, o2: Stories_All ->
                    o2.totalPages.compareTo(o1.totalPages)
                })
                adapterSeen.notifyDataSetChanged()
            }

            index++


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
            val action =
                MainFragmentDirections.actionMainFragmentToDetailFragment(uri.toString())
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