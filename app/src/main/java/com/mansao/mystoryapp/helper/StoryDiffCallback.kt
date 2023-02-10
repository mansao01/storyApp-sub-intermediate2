package com.mansao.mystoryapp.helper

import androidx.recyclerview.widget.DiffUtil
import com.mansao.mystoryapp.data.remote.response.StoryItem

class StoryDiffCallback(
    private val mOldStoryList: List<StoryItem>,
    private val mNewStoryList: List<StoryItem>,
):DiffUtil.Callback() {
    override fun getOldListSize()=mOldStoryList.size

    override fun getNewListSize()=mNewStoryList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldStoryList[oldItemPosition].id == mNewStoryList[newItemPosition].id

    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldEmployee = mOldStoryList[oldItemPosition]
        val newEmployee = mNewStoryList[newItemPosition]
        return oldEmployee.name ==  newEmployee.name && oldEmployee.description ==  newEmployee.description
    }
}