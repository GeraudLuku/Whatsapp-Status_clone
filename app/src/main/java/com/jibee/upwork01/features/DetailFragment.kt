package com.jibee.upwork01.features

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.jibee.upwork01.MainViewModel
import com.jibee.upwork01.R
import com.jibee.upwork01.models.postStory.Story
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_detail.closeBtn
import kotlinx.android.synthetic.main.fragment_detail.postBtn
import kotlinx.android.synthetic.main.fragment_detail.progress_view
import kotlinx.android.synthetic.main.fragment_status.*
import java.text.SimpleDateFormat
import java.util.*

class DetailFragment : Fragment() {

    private lateinit var navController: NavController
    private val args: DetailFragmentArgs by navArgs()

    private lateinit var mainViewModel: MainViewModel
    private var listen: Boolean = false

    private var typeMedia: String = ""
    private var mediaUri: String = ""


    private var player: SimpleExoPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //hide keyboard if anywhere else is pressed
        view.setOnClickListener {
            view.let { activity?.hideKeyboard(it) }
        }

        //configure nav controller and get uri from intent
        navController = findNavController()

        //subscribe to the view model
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        mediaUri = args.mediaUri

        //listen to post story callback
        mainViewModel.postStoryResponse.observe(viewLifecycleOwner) {

            if (listen) {
                println(it.error?.localizedMessage)

                if (it.error?.localizedMessage != null) {
                    listen = false
                    progress_view.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), "Failed to Add Story", Toast.LENGTH_LONG)
                        .show()
                } else {
                    listen = false
                    println(it.error?.localizedMessage)
                    progress_view.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), "Story Added", Toast.LENGTH_SHORT).show()
                    //refresh story list
                    mainViewModel.setStoryKey("test")
                    //go back to main fragment
                    navController.popBackStack()
                }
            }
        }

        //image: file:///data/user/0/com.jibee.upwork01/cache/cropped394261694738506970.jpg
        //video file:///storage/emulated/0/Android/data/com.jibee.upwork01/files/Download/trimmed_video_2021_7_8_13_47_50.mp4


        if (getLastNCharsOfString(args.mediaUri, 4).equals(".mp4"))
            playVideo()
        else
            loadImage()


        //on x pressed quick fragment
        closeBtn.setOnClickListener {
            navController.popBackStack()
        }

        postBtn.setOnClickListener {

            //upload media to database first
            //it upload media to Firebase Storage
            val storageRef = Firebase.storage.reference
            val ref =
                storageRef.child("uploads/" + UUID.randomUUID().toString())
            val uploadTask = ref.putFile(Uri.parse(mediaUri))

            val urlTask =
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation ref.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        //now get the url of the media and add it to the post object
                        mediaUri = downloadUri.toString()
                        Log.d("Firebase", "$downloadUri")

                        //push status to the database
                        val story = Story(
                            getCurrentDateTime().toString("yyyy-MM-dd HH:mm:ss"),
                            mediaURL = mediaUri,
                            mimeType = typeMedia
                        )
                        mainViewModel.setStoryInfo(story)
                        listen = true

                    }
                }.addOnFailureListener {
                    Log.d("Firebase", it!!.localizedMessage)
                }


            Toast.makeText(requireContext(), "Adding Story....", Toast.LENGTH_SHORT).show()
            progress_view.visibility = View.VISIBLE
            view.let { activity?.hideKeyboard(it) }
        }


    }


    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(requireContext())
            .build()
            .also { exoPlayer ->
                videoView.player = exoPlayer
                val mediaItem = MediaItem.fromUri(args.mediaUri)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.playWhenReady = true
                exoPlayer.seekTo(0, 0)
                exoPlayer.prepare()

            }

        //modify video path, add file:// to the beginning
        mediaUri = "file://" + args.mediaUri
    }


    fun playVideo() {
        typeMedia = ".mp4"
        //exoplayer methods if its a video
        //show exoplayer
        videoView.visibility = View.VISIBLE

        Log.d("Play-Video", "reached here")
        initializePlayer()

    }


    fun loadImage() {
        typeMedia = ".jpeg"
        //glide methods if its an image
        Glide.with(this)
            .load(Uri.parse(args.mediaUri))
            .into(pictureView)
        //make imageview visible
        pictureView.visibility = View.VISIBLE
    }

    fun getLastNCharsOfString(str: String, n: Int): String {
        var lastnChars = str
        if (lastnChars.length > n) {
            lastnChars = lastnChars.substring(lastnChars.length - n, lastnChars.length)
        }
        return lastnChars
    }
}


//Extra Methods
fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}

fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}
