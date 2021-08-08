package com.jibee.upwork01.features

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.firebase.auth.FirebaseAuth
import com.jibee.upwork01.R
import com.jibee.upwork01.models.Src
import com.jibee.upwork01.repo.StoriesViewModel
import kotlinx.android.synthetic.main.fragment_detail.*
import java.net.URLConnection

class DetailFragment : Fragment() {

    private lateinit var navController: NavController
    private val args: DetailFragmentArgs by navArgs()

    private lateinit var storiesViewModel: StoriesViewModel

    private var typeMedia: String = ""


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
        storiesViewModel = ViewModelProvider(requireActivity()).get(StoriesViewModel::class.java)

        Log.d("MediaUri1", args.mediaUri)


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
            val userId = FirebaseAuth.getInstance().currentUser?.uid!!
            val item = Src(userId, typeMedia, captionTxt.text.toString(), "12:30", args.mediaUri)
            storiesViewModel.addStory(item)
            Toast.makeText(requireContext(),"Adding Story....",Toast.LENGTH_LONG).show()
            navController.popBackStack()
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