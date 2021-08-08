package com.jibee.upwork01.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.devlomi.circularstatusview.CircularStatusView
import com.jibee.upwork01.R
import com.jibee.upwork01.models.Src
import com.jibee.upwork01.models.Story

class StoriesAdapter(
    private val itemList: ArrayList<Story>,
    private var clickListener: OnItemClickedListener
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

        val imageView: ImageView = itemView.findViewById(R.id.last_status_image) //just to display the users profile image
        val name: TextView = itemView.findViewById(R.id.name)
        val time: TextView = itemView.findViewById(R.id.time)
        val indicator: CircularStatusView = itemView.findViewById(R.id.status_indicator)

        //init item click listener
        fun initialize(story: Story, action: OnItemClickedListener, position: Int) {

            //set status count
            indicator.setPortionsCount(story.content.size)
            indicator.setPortionsColor(getColor(itemView.context, R.color.custom1))

            //set name
            name.text = story.content[0].uid.substring(0,5)
            //set time
            time.text = story.content[0].time

            //implement click function
            itemView.setOnClickListener {
                action.onItemCLicked(story)
            }

        }

    }

    interface OnItemClickedListener {
        fun onItemCLicked(story: Story)
    }
}
