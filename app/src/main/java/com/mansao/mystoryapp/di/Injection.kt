package com.mansao.mystoryapp.di

import android.content.Context
import com.mansao.mystoryapp.data.StoryRepository
import com.mansao.mystoryapp.data.remote.retrofit.ApiConfig

object Injection {
    fun provideRepository(context: Context): StoryRepository{
        val apiService = ApiConfig.getApiService()
        return  StoryRepository(apiService)
    }
}