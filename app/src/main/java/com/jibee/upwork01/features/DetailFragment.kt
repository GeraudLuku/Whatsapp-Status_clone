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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.firebase.auth.FirebaseAuth
import com.jibee.upwork01.MainViewModel
import com.jibee.upwork01.R
import com.jibee.upwork01.models.Src
import com.jibee.upwork01.models.postStory.PostStory
import com.jibee.upwork01.repo.StoriesViewModel
import kotlinx.android.synthetic.main.fragment_detail.*
import java.io.File
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*

class DetailFragment : Fragment() {

    private lateinit var navController: NavController
    private val args: DetailFragmentArgs by navArgs()

    private lateinit var mainViewModel: MainViewModel

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

        //configure nav controller and get uri from intent
        navController = findNavController()

        //subscribe to the view model
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        Log.d("MediaUri1", args.mediaUri)
        mediaUri = args.mediaUri

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

            //push status to the database
            val story = PostStory(
                getCurrentDateTime().toString("yyyy/MM/dd HH:mm:ss"),
                mediaURL = mediaUri,
                mimeType = "media" //set media first so we know if its just plain text or a file
            )
            mainViewModel.setStoryInfo(story)
//            storiesViewModel.addStory(item)
//            Toast.makeText(requireContext(), "Adding Story....", Toast.LENGTH_LONG).show()
//            view.let { activity?.hideKeyboard(it) }
//            navController.popBackStack()
        }

        //listen to add story return
        mainViewModel.postStoryResponse.observe(viewLifecycleOwner,Observer{

        })

    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
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
        typeMedia = "video"
        //exoplayer methods if its a video
        //show exoplayer
        videoView.visibility = View.VISIBLE

        Log.d("Play-Video", "reached here")
        initializePlayer()

    }


    fun loadImage(): Unit {
        typeMedia = "image"
        //glide methods if its an image
        Glide.with(this)
            .load(Uri.parse(args.mediaUri))
            .into(pictureView)
        //make imageview visible
        pictureView.visibility = View.VISIBLE
    }

    fun isImageFile(path: String?): Boolean {
        path.let {
            val mimeType = URLConnection.guessContentTypeFromName(it)
            return mimeType != null && mimeType.startsWith("image")
        }

    }

    fun getLastNCharsOfString(str: String, n: Int): String? {
        var lastnChars = str
        if (lastnChars.length > n) {
            lastnChars = lastnChars.substring(lastnChars.length - n, lastnChars.length)
        }
        return lastnChars
    }
}

fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}

fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}