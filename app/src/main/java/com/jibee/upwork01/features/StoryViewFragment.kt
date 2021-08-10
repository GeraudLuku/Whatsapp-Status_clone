package com.jibee.upwork01.features

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.jibee.upwork01.R
import com.jibee.upwork01.models.Story
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_story_view.*


class StoryViewFragment : Fragment(), StoriesProgressView.StoriesListener {

    private lateinit var navController: NavController
    private val storyArgs: StoryViewFragmentArgs by navArgs()

    private lateinit var storyItem: Story
    private lateinit var storiesProgressView: StoriesProgressView

    private var isFirstLoad: Boolean = true

    private var currentItem: Int = 0

    private var player: SimpleExoPlayer? = null

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

        //get story item from navigation
        storyItem = storyArgs.storyItem

        //init StoriesProgressView
        storiesProgressView = view.findViewById(R.id.stories_progress_view)
        storiesProgressView.setStoriesCount(storyItem.content.size)
        storiesProgressView.setStoriesListener(this)


        //start loading stories
        loadNextMedia(currentItem)


        //on left or right pressed functions
        skip_backBtn.setOnClickListener {
            //go behind
            storiesProgressView.reverse()
        }
        skip_FrontBtn.setOnClickListener {
            //go infront
            storiesProgressView.skip()
        }

        //back button
        backBtn.setOnClickListener {
            navController.popBackStack()
        }

    }

    private fun loadNextMedia(currentItem: Int) {
        val story = storyItem.content[currentItem]
        when (story.type) {
            "image" -> {
                //display image
                displayImage(currentItem)
            }
            "video" -> {
                //display video
                displayVideo(currentItem)
            }
            "text" -> {
                //display text
                displayText(currentItem)
            }
        }
    }

    private fun displayImage(currentItem: Int) {
        if (isFirstLoad) {
            storiesProgressView.startStories()
            isFirstLoad = false
            //upload duration time for notification bar
            storiesProgressView.setStoryDuration(10000)
        }
        //upload duration time for notification bar
        storiesProgressView.setStoryDuration(10000)

        //make the other two views invisible
        video_mode.visibility = View.INVISIBLE
        text_mode_view.visibility = View.INVISIBLE
        //show imageview
        image_mode.visibility = View.VISIBLE
        footer.visibility = View.VISIBLE
        //load image on imageview
        Glide.with(this)
            .load(storyItem.content[currentItem].src)
            .into(image_mode)
        //set description and other information
        story_description.text = storyItem.content[currentItem].description
        story_name.text = storyItem.content[currentItem].uid.substring(0, 5)
        story_time.text = storyItem.content[currentItem].time
    }

    private fun displayText(currentItem: Int) {
        if (isFirstLoad) {
            //upload duration time for notification bar
            storiesProgressView.setStoryDuration(6000)
            storiesProgressView.startStories()
            isFirstLoad = false
        }
        //upload duration time for notification bar
        storiesProgressView.setStoryDuration(6000)
        //make the other two views invisible
        video_mode.visibility = View.INVISIBLE
        image_mode.visibility = View.INVISIBLE
        footer.visibility = View.INVISIBLE
        //show imageview

        //set random color on the text view backgroung like whartsapp
        text_mode_view.visibility = View.VISIBLE
        //set text on TextView
        text_mode.text = storyItem.content[currentItem].description
        //set other informations
        story_name.text = storyItem.content[currentItem].uid.substring(0, 5)
        story_time.text = storyItem.content[currentItem].time
    }

    private fun displayVideo(currentItem: Int) {
        if (isFirstLoad) {
            storiesProgressView.setStoryDuration(50000)
            storiesProgressView.startStories()
            isFirstLoad = false
        }
        storiesProgressView.setStoryDuration(50000)
        //make the other two views invisible
        image_mode.visibility = View.INVISIBLE
        text_mode_view.visibility = View.INVISIBLE

        footer.visibility = View.VISIBLE

        //upload duration time for notification bar
        video_mode.visibility = View.VISIBLE

        story_description.text = storyItem.content[currentItem].description

        story_name.text = storyItem.content[currentItem].uid.substring(0, 5)
        story_time.text = storyItem.content[currentItem].time

        //play video
        player = SimpleExoPlayer.Builder(requireContext())
            .build()
            .also { exoPlayer ->
                video_mode.player = exoPlayer
                val mediaItem = MediaItem.fromUri(storyItem.content[currentItem].src)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.playWhenReady = true
                exoPlayer.seekTo(0, 0)
                exoPlayer.prepare()

            }
    }


    //StoriesProgressView Methods
    override fun onNext() {
        if (currentItem < storyItem.content.size) {
            currentItem++
            loadNextMedia(currentItem)
        }
    }

    override fun onPrev() {
        if (currentItem > 0) {
            currentItem--
            loadNextMedia(currentItem)
        }
    }

    override fun onComplete() {
        currentItem = 0
        navController.popBackStack()
    }
}