package com.jibee.upwork01.features

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.appexecutors.picker.Picker
import com.appexecutors.picker.Picker.Companion.PICKED_MEDIA_LIST
import com.appexecutors.picker.Picker.Companion.REQUEST_CODE_PICKER
import com.appexecutors.picker.utils.PickerOptions
import com.bumptech.glide.Glide
import com.jibee.upwork01.MainViewModel
import com.jibee.upwork01.R
import com.jibee.upwork01.adapters.StoriesAdapter
import com.jibee.upwork01.adapters.StoryAdapter
import com.jibee.upwork01.databinding.FragmentMainBinding
import com.jibee.upwork01.models.Stories.Stories
import com.jibee.upwork01.util.Resource
import com.jibee.upwork01.util.URIPathHelper
import com.theartofdev.edmodo.cropper.CropImage
import com.videotrimmer.library.utils.CompressOption
import com.videotrimmer.library.utils.TrimVideo
import kotlinx.android.synthetic.main.fragment_main.*
import java.net.URLConnection


class MainFragment : Fragment(R.layout.fragment_main), StoriesAdapter.OnItemClickedListener {

    private lateinit var navController: NavController

    private val viewModel: MainViewModel by viewModels()

    private val storyList: ArrayList<Stories> = arrayListOf()
    private val storyListSeen: ArrayList<Stories> = arrayListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMainBinding.bind(view)
        val storyAdapter = StoryAdapter()
        val seenStoryAdapter = StoryAdapter()
        binding.apply {

            //recycler view divider
            val dividerItemDecoration = DividerItemDecoration(
                recyclerView.context,
                LinearLayoutManager.VERTICAL
            )

            recyclerView.apply {
                layoutManager = LinearLayoutManager(view.context)
                addItemDecoration(dividerItemDecoration)
                setHasFixedSize(false)
                adapter = storyAdapter
            }

            recyclerViewViewed.apply {
                layoutManager = LinearLayoutManager(view.context)
                addItemDecoration(dividerItemDecoration)
                setHasFixedSize(false)
                adapter = seenStoryAdapter
            }

        }

        viewModel.userStories.observe(viewLifecycleOwner) { result ->
            binding.apply {
                result.data?.run {
                    circularStatusView.setPortionsCount(this.totalResults)
                    circularStatusView.setPortionsColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.custom2
                        )
                    )
                    Glide.with(requireView())
                        .load(this.results[0].userViewModel.profilePhoto)
                        .into(profile_image)

                    add_story_indicator.text = "View my stories"
                }

            }

            retryUserStoryIndicator.isVisible =
                result is Resource.Error && result.data?.results.isNullOrEmpty()
        }

        viewModel.friendsStories.observe(viewLifecycleOwner) { results ->

            Log.d("Friends Stories", "${results.data}")

            storyList.clear()
            storyListSeen.clear()

            results.data?.forEach { story ->
                if (story.seen)
                    storyListSeen.add(story)
                else
                    storyList.add(story)
            }

            storyAdapter.submitList(storyList)
            seenStoryAdapter.submitList(storyListSeen)

            binding.retryIndicator.isVisible =
                results is Resource.Error && results.data.isNullOrEmpty()
            binding.emptyIndicator.isVisible = results is Resource.Success && results.data.isNullOrEmpty()

        }

        //configure nav controller
        navController = findNavController()

        setListeners()

        (activity as AppCompatActivity).setSupportActionBar(toolbar)
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
            viewModel.friendsStory.value = !viewModel.friendsStory.value
        }

        //on click of user story retry
        retryUserStoryIndicator.setOnClickListener {
            viewModel.userStory.value = !viewModel.userStory.value
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

    override fun onItemCLicked(story: Stories) {
        Log.d("Story-Click", "${story.results}")
        //goto story view fragment
        val action = MainFragmentDirections.actionMainFragmentToStoryViewFragment(story)
        navController.navigate(action)
    }
}