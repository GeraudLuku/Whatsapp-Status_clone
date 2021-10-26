package com.jibee.upwork01.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jibee.upwork01.R
import com.jibee.upwork01.databinding.UserStoryItemBinding
import com.jibee.upwork01.models.Stories.Stories
import com.jibee.upwork01.util.TimeAgo

class StoryAdapter(
    private var clickListener: OnItemClickedListener,
) : ListAdapter<Stories, StoryAdapter.StoriesViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesViewHolder {
        val binding =
            UserStoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoriesViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)

        if (currentItem.stories.isNotEmpty()) {
            holder.itemView.visibility = View.VISIBLE
            holder.itemView.setOnClickListener {
                clickListener.onItemCLicked(currentItem)
            }
        } else {
            holder.itemView.layoutParams.height = 0
            holder.itemView.layoutParams.width = 0
        }
    }

    class StoriesViewHolder(private val binding: UserStoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: Stories) {
            binding.apply {
                //set status count //check if its seen or unseen status
                if (story.stories.isNotEmpty()) {

                    statusIndicator.setPortionsCount(story.stories.size)
                    statusIndicator.setPortionsColor(
                        ContextCompat.getColor(
                            itemView.context,
                            if (story.seen) R.color.custom2 else R.color.custom1
                        )
                    )


                    //set name
                    name.text = story.stories[0].userViewModel.userName

                    //set time
                    time.text =
                        TimeAgo.getTimeAgo(
                            story.stories[story.stories.size - 1].getCreatedTime()
                        ).toString()

                    //last status image will be the users profile picture
                    Glide.with(binding.root)
                        .load(story.stories[0].userViewModel.profilePhoto)
                        .into(lastStatusImage)
                }

            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Stories>() {
        override fun areItemsTheSame(oldItem: Stories, newItem: Stories) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Stories, newItem: Stories) =
            oldItem.results == newItem.results
    }

    interface OnItemClickedListener {
        fun onItemCLicked(story: Stories)
    }
}