package com.mansao.mystoryapp.data

import com.mansao.mystoryapp.data.remote.response.StoryResponse
import com.mansao.mystoryapp.data.remote.retrofit.ApiService
import retrofit2.Call

class StoryRepository(private val apiService: ApiService) {
    fun getStories(token: String): Call<StoryResponse> {
        return apiService.getStories(token)
    }
}