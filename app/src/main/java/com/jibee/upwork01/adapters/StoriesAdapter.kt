package com.jibee.upwork01.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devlomi.circularstatusview.CircularStatusView
import com.jibee.upwork01.R
import com.jibee.upwork01.models.Stories.Stories
import com.jibee.upwork01.util.TimeAgo
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class StoriesAdapter(
    private val itemList: ArrayList<Stories>,
    private var clickListener: OnItemClickedListener,
    private val context: Context
) :
    RecyclerView.Adapter<StoriesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StoriesAdapter.ViewHolder {
        val rootView =
            LayoutInflater.from(parent.context).inflate(R.layout.user_story_item, parent, false)
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: StoriesAdapter.ViewHolder, position: Int) {
        val storyItem = itemList[position]
        //set click listener
        holder.initialize(storyItem, clickListener, holder.adapterPosition)

    }

    override fun getItemCount(): Int {
        return itemList.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView =
            itemView.findViewById(R.id.last_status_image) //just to display the users profile image
        val name: TextView = itemView.findViewById(R.id.name)
        val time: TextView = itemView.findViewById(R.id.time)
        val indicator: CircularStatusView = itemView.findViewById(R.id.status_indicator)

        //init item click listener
        fun initialize(story: Stories, action: OnItemClickedListener, position: Int) {

            //set status count //check if its seen or unseen status
            if (story.results[0].seenStatus) {
                indicator.setPortionsCount(story.totalResults)
                indicator.setPortionsColor(getColor(itemView.context, R.color.custom2))
            } else {
                indicator.setPortionsCount(story.totalResults)
                indicator.setPortionsColor(getColor(itemView.context, R.color.custom1))
            }

            //set name
            name.text = story.results[0].userViewModel.userName

            //set time
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            try {
                val date = format.parse(story.results[story.totalResults - 1].addedDateAndTime)
                time.text = TimeAgo.getTimeAgo(date!!.time).toString()
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            //last status image will be the users profile picture
            Glide.with(context)
                .load(story.results[0].userViewModel.profilePhoto)
                .into(imageView)


            //implement click function
            itemView.setOnClickListener {
                action.onItemCLicked(story)
            }

        }

    }
    interface OnItemClickedListener {
        fun onItemCLicked(story: Stories)
    }
}
