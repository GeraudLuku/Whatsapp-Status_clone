package com.jibee.upwork01.adapters

import android.view.LayoutInflater
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

class StoryAdapter : ListAdapter<Stories, StoryAdapter.StoriesViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesViewHolder {
        val binding =
            UserStoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoriesViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class StoriesViewHolder(private val binding: UserStoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stories: Stories) {
            binding.apply {
                //set status count //check if its seen or unseen status
                if (stories.seen) {
                    statusIndicator.setPortionsCount(stories.totalResults)
                    statusIndicator.setPortionsColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.custom2
                        )
                    )
                } else {
                    statusIndicator.setPortionsCount(stories.totalResults)
                    statusIndicator.setPortionsColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.custom1
                        )
                    )
                }

                //set name
                name.text = stories.results[0].userViewModel.userName

                //set time
                time.text =
                    TimeAgo.getTimeAgo(
                        stories.results.get(stories.results.size - 1).getCreatedTime()
                    )
                        .toString()

                //last status image will be the users profile picture
                Glide.with(binding.root)
                    .load(stories.results[0].userViewModel.profilePhoto)
                    .into(lastStatusImage)

            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Stories>() {
        override fun areItemsTheSame(oldItem: Stories, newItem: Stories) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Stories, newItem: Stories) =
            oldItem.results == newItem.results
    }
}