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
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.appexecutors.picker.Picker
import com.appexecutors.picker.Picker.Companion.PICKED_MEDIA_LIST
import com.appexecutors.picker.Picker.Companion.REQUEST_CODE_PICKER
import com.appexecutors.picker.utils.PickerOptions
import com.jibee.upwork01.R
import com.jibee.upwork01.util.URIPathHelper
import com.theartofdev.edmodo.cropper.CropImage
import com.videotrimmer.library.utils.CompressOption
import com.videotrimmer.library.utils.TrimVideo
import kotlinx.android.synthetic.main.fragment_main.*
import java.net.URLConnection


class MainFragment : Fragment() {

    private lateinit var navController: NavController


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

        //configure nav controller
        navController = findNavController()


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
                val action = MainFragmentDirections.actionMainFragmentToDetailFragment(resultUri.toString())
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
        return mimeType != null && mimeType.startsWith("image")
    }

}