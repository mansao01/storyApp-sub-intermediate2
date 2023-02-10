package com.mansao.mystoryapp.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mansao.mystoryapp.data.remote.response.StoryItem
import com.mansao.mystoryapp.databinding.ItemStoryListBinding
import com.mansao.mystoryapp.helper.StoryDiffCallback
import com.mansao.mystoryapp.ui.detail.DetailActivity

class StoryListAdapter : RecyclerView.Adapter<StoryListAdapter.StoryListViewHolder>() {

    private val listStory = ArrayList<StoryItem>()

    fun setListStory(listStory: List<StoryItem>) {
        val diffCallback = StoryDiffCallback(this.listStory, listStory)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.listStory.clear()
        this.listStory.addAll(listStory)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryListViewHolder {
        val binding =
            ItemStoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryListViewHolder(binding)
    }


    override fun onBindViewHolder(holder: StoryListViewHolder, position: Int) {
        holder.bind(listStory[position])
    }

    override fun getItemCount() = listStory.size

    class StoryListViewHolder(private val binding: ItemStoryListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryItem) {
            binding.apply {
                tvItemName.text = story.name
                tvItemDescription.text = story.description
                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .centerCrop()
                    .into(ivItemPhoto)

                cardView.setOnClickListener {
                    val intent = Intent(itemView.context, DetailActivity::class.java)
                    intent.putExtra(DetailActivity.EXTRA_DATA, story)

                    val optionsCompat:ActivityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(ivItemPhoto, "avatar"),
                        Pair(tvItemName, "name"),
                        Pair(tvItemDescription, "description")
                    )
                    itemView.context.startActivity(intent, optionsCompat.toBundle())
                }
            }
        }
    }

}