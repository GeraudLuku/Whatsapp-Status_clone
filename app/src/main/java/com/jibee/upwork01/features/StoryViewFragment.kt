package com.jibee.upwork01.features

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.jibee.upwork01.MainViewModel
import com.jibee.upwork01.R
import com.jibee.upwork01.models.Stories.Result
import com.jibee.upwork01.models.Stories.Stories
import com.jibee.upwork01.util.TimeAgo
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.fragment_story_view.*
import java.util.*


class StoryViewFragment : Fragment(), StoriesProgressView.StoriesListener {

    private lateinit var navController: NavController
    private val storyArgs: StoryViewFragmentArgs by navArgs()
    private lateinit var mainViewModel: MainViewModel

    private lateinit var storyItem: Stories
    private lateinit var storiesProgressView: StoriesProgressView

    private var isFirstLoad: Boolean = true

    private var currentItem: Int = 0

    private var player: SimpleExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playBackPosition: Long = 0

    private var pressTime = 0L

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener: OnTouchListener = OnTouchListener { view, event ->
        val action = event.action
        val story = storyItem.results[currentItem]
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                storiesProgressView.pause()
                pressTime = System.currentTimeMillis()
                //hide some views
                header.visibility = View.INVISIBLE
                footer.visibility = View.INVISIBLE
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                storiesProgressView.resume()
                val now = System.currentTimeMillis()
                if (story.mimeType != ".txt" || story.mimeType != "text") {
                    //show back the views
                    header.visibility = View.VISIBLE
                    footer.visibility = View.VISIBLE
                }
                return@OnTouchListener 500L < now - pressTime
            }
        }
        false
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_story_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //initialize navcontroller
        navController = findNavController()

        //subscribe to the view modell
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        //get story item from navigation
        storyItem = storyArgs.storyItem

        //init StoriesProgressView
        initializeStoriesProgress()

        //set profile image
        Glide.with(requireContext())
            .load(storyItem.results[0].userViewModel.profilePhoto)
            .into(profile_image)

        //on left or right pressed functions
        skip_backBtn.setOnClickListener {
            //go behind
            storiesProgressView.reverse()
        }
        skip_FrontBtn.setOnClickListener {
            //go infront
            storiesProgressView.skip()
        }
        skip_FrontBtn.setOnTouchListener(onTouchListener)
        skip_backBtn.setOnTouchListener(onTouchListener)

        //back button
        backBtn.setOnClickListener {
            mainViewModel.setStoryKey("test")
            navController.popBackStack()
        }

        //start loading stories
        loadNextMedia(currentItem)

    }

    private fun initializeStoriesProgress() {
        storiesProgressView = stories_progress_view
        storiesProgressView.setStoriesCount(storyItem.totalResults)
        storiesProgressView.setStoriesListener(this)
    }

    private fun loadNextMedia(currentItem: Int) {
        val story = storyItem.results[currentItem]
        when (story.mimeType) {
            "jpeg" -> {
                //display image
                displayImage(story)
            }
            ".jpeg" -> {
                //display image
                displayImage(story)
            }
            "mp4" -> {
                //display video
                displayVideo(story)
            }
            "text" -> {
                //display text
                displayText(story)
            }
            ".txt" -> {
                //display text
                displayText(story)
            }
        }
    }

    private fun displayImage(currentItem: Result) {

        //upload duration time for notification bar
        storiesProgressView.setStoryDuration(10000)

        //make the following views invisible
        video_mode.visibility = View.INVISIBLE
        video_loadind.visibility = View.INVISIBLE
        video_error.visibility = View.INVISIBLE
        text_mode_view.visibility = View.INVISIBLE

        //show image view and other related
        image_mode.visibility = View.VISIBLE
        glide_load.visibility = View.VISIBLE
        footer.visibility = View.VISIBLE

        //load image on image view
        Glide.with(this)
            .load(currentItem.mediaURL)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    glide_load.visibility = View.INVISIBLE
                    //if there is an error and its the first start
                    if (isFirstLoad) {
                        storiesProgressView.startStories()
                        isFirstLoad = false
                    }
                    Toast.makeText(requireContext(), e!!.localizedMessage, Toast.LENGTH_LONG).show()
                    storiesProgressView.skip()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    glide_load.visibility = View.GONE
                    if (isFirstLoad) {
                        storiesProgressView.startStories()
                        isFirstLoad = false
                    } else {
                        storiesProgressView.resume()
                    }
                    return false
                }

            })
            .error(R.drawable.ic_error)//error drawable
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(image_mode)

        //set other related information
        story_name.text = currentItem.userViewModel.userName
        story_time.text = TimeAgo.getTimeAgo(currentItem.getCreatedTime()).toString()
    }

    private var isDurationSet: Boolean = false //check if the video duration has been set
    private fun displayVideo(currentItem: Result) {

        initExoplayer()

        storiesProgressView.setStoryDuration(40000)

        //make the other two views invisible
        image_mode.visibility = View.INVISIBLE
        glide_load.visibility = View.INVISIBLE
        text_mode_view.visibility = View.INVISIBLE


        //upload duration time for notification bar
        video_mode.visibility = View.VISIBLE
        video_loadind?.visibility = View.VISIBLE
        footer.visibility = View.VISIBLE

        //play video
        val mediaItem = MediaItem.fromUri(currentItem.mediaURL)
        player?.setMediaItem(mediaItem, true)
        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playBackPosition)
        player?.prepare()

        //add listener
        player?.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                when (state) {
                    Player.STATE_BUFFERING -> {
                        //pause stories progress
                        storiesProgressView.pause()
                        //show progressbar
                        video_loadind?.visibility = View.VISIBLE
                    }
                    Player.STATE_READY -> {
                        //set video duration if it has not been set on stories progress
                        if (!isDurationSet) {
                            val duration = player!!.duration
                            println("Duration of video: $duration L")
                            storiesProgressView.setStoryDuration(duration)
                            isDurationSet = true
                        }
                        //remove progress bar
                        video_loadind?.visibility = View.INVISIBLE

                        if (isFirstLoad) {
                            storiesProgressView.startStories()
                            isFirstLoad = false
                        } else {
                            storiesProgressView.resume()
                        }
                    }
                    Player.STATE_ENDED -> {
                        //goto next story when video has ended
                        releasePlayer()
                        storiesProgressView.skip()
                    }
                    Player.STATE_IDLE -> {
                    }
                }
            }

//            override fun onPlayerError(error: ExoPlaybackException) {
//                super.onPlayerError(error)
//                storiesProgressView.pause()
//                video_loadind.visibility = View.INVISIBLE
//                video_error.visibility = View.VISIBLE
//                Toast.makeText(
//                    requireContext(),
//                    "Exoplayer-error: ${error.localizedMessage}",
//                    Toast.LENGTH_SHORT
//                ).show()
//                //show error image on video view
//
//            }

        })

        //set other related information
        story_name.text = currentItem.userViewModel.userName
        story_time.text = TimeAgo.getTimeAgo(currentItem.getCreatedTime()).toString()
    }

    private fun initExoplayer() {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        video_mode.player = player
    }

    private fun displayText(currentItem: Result) {
        //upload duration time for notification bar
        storiesProgressView.setStoryDuration(6000)

        if (isFirstLoad) {
            storiesProgressView.startStories()
            isFirstLoad = false
        } else {
            storiesProgressView.resume()
        }

        //make the other two views invisible
        video_mode.visibility = View.INVISIBLE
        video_loadind.visibility = View.INVISIBLE
        image_mode.visibility = View.INVISIBLE
        glide_load.visibility = View.INVISIBLE
        footer.visibility = View.INVISIBLE
        video_error.visibility = View.INVISIBLE


        //set text on text view
        text_mode_view.visibility = View.VISIBLE
        text_mode.text = currentItem.mediaURL

        //set other information
        //set other related information
        story_name.text = currentItem.userViewModel.userName
        story_time.text = TimeAgo.getTimeAgo(currentItem.getCreatedTime()).toString()
    }

    //StoriesProgressView Methods
    override fun onNext() {
        val story = storyItem.results[currentItem]
        isDurationSet = false
        releasePlayer()
        //update seen status of story
        if (!story.seenStatus)
            mainViewModel.updateSeenStatus(
                story.iD,
                true
            )
        if (currentItem < storyItem.totalResults) {
            currentItem++
            loadNextMedia(currentItem)
        }
    }

    override fun onPrev() {
        isDurationSet = false
        releasePlayer()
        if (currentItem > 0) {
            currentItem--
            loadNextMedia(currentItem)
        }
    }

    override fun onComplete() {
        val story = storyItem.results[currentItem]
        //update seen status of story
        if (!story.seenStatus)
            mainViewModel.updateSeenStatus(
                story.iD,
                true
            )
        currentItem = 0
        releasePlayer()
        mainViewModel.setStoryKey("test")
        navController.popBackStack()
    }

    override fun onPause() {
        if (Build.VERSION.SDK_INT < 24)
            releasePlayer()
        super.onPause()
    }

    override fun onStop() {
        if (Build.VERSION.SDK_INT < 24)
            releasePlayer()
        super.onStop()
    }

    private fun releasePlayer() {
        if (player != null) {
            playWhenReady = player!!.playWhenReady
            playBackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            player!!.release()
            player = null
        }
    }

}