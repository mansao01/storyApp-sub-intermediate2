package com.mansao.mystoryapp.viewmodel

import androidx.lifecycle.*
import com.mansao.mystoryapp.data.remote.response.StoryResponse
import com.mansao.mystoryapp.data.remote.retrofit.ApiConfig
import com.mansao.mystoryapp.preference.User
import com.mansao.mystoryapp.preference.UserPreference
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val pref: UserPreference) : ViewModel() {

    private var _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean> = _showLoading

    private var _storyResponse = MutableLiveData<StoryResponse>()
    val storyResponse: LiveData<StoryResponse> = _storyResponse


    fun getUser(): LiveData<User> {
        return pref.getUser().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }

    fun getStories(token:String) {
        _showLoading.value = true
        val service = ApiConfig.getApiService().getStories("Bearer $token")
        service.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    _storyResponse.postValue(responseBody!!)
                    _showLoading.value = false
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                _showLoading.value = false
                t.printStackTrace()
            }

        })
    }
}