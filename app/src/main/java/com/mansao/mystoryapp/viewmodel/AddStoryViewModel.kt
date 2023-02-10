package com.mansao.mystoryapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mansao.mystoryapp.data.remote.response.PostStoryResponse
import com.mansao.mystoryapp.data.remote.retrofit.ApiConfig
import com.mansao.mystoryapp.preference.User
import com.mansao.mystoryapp.preference.UserPreference
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddStoryViewModel(private val pref: UserPreference) : ViewModel() {
    private var _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private var _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean> = _showLoading

    private var _isSucceed = MutableLiveData<Boolean>()
    val isSucceed: LiveData<Boolean> = _isSucceed


    fun getUser(): LiveData<User> {
        return pref.getUser().asLiveData()
    }

    fun postStory(file: MultipartBody.Part, description: RequestBody, token: String) {
        _showLoading.value = true
        val service = ApiConfig.getApiService().postStory(file, description, "Bearer $token")
        service.enqueue(object : Callback<PostStoryResponse> {
            override fun onResponse(
                call: Call<PostStoryResponse>,
                response: Response<PostStoryResponse>
            ) {
                if (response.isSuccessful) {
                    _showLoading.value = false
                    _isSucceed.value = true
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.error) {
                        _message.postValue(responseBody.message)
                    } else {
                        _message.postValue(responseBody?.message)
                    }
                }
            }

            override fun onFailure(call: Call<PostStoryResponse>, t: Throwable) {
                _message.postValue(t.message)
                _showLoading.value = false
                _isSucceed.value = false


            }

        })
    }
}