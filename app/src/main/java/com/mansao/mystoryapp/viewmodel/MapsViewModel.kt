package com.mansao.mystoryapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mansao.mystoryapp.data.remote.response.MapsResponse
import com.mansao.mystoryapp.data.remote.response.StoryResponse
import com.mansao.mystoryapp.data.remote.retrofit.ApiConfig
import com.mansao.mystoryapp.preference.User
import com.mansao.mystoryapp.preference.UserPreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsViewModel(private val pref: UserPreference):ViewModel() {
    private var _mapsResponse = MutableLiveData<MapsResponse>()
    val mapsResponse: LiveData<MapsResponse> = _mapsResponse

    private var _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean> = _showLoading


    fun getUser(): LiveData<User> {
        return pref.getUser().asLiveData()
    }

    fun getMapsLocation(token: String){
        _showLoading.value = true
        val service = ApiConfig.getApiService().getStoryWithLocation("Bearer $token")
        service.enqueue(object : Callback<MapsResponse> {
            override fun onResponse(call: Call<MapsResponse>, response: Response<MapsResponse>) {
                if (response.isSuccessful){
                    val responseBody = response.body()
                    _mapsResponse.postValue(responseBody!!)
                    _showLoading.value = false

                }
            }

            override fun onFailure(call: Call<MapsResponse>, t: Throwable) {
                _showLoading.value = false
                t.printStackTrace()
            }

        })
    }
}