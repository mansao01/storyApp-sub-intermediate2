package com.mansao.mystoryapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mansao.mystoryapp.data.remote.response.RegisterResponse
import com.mansao.mystoryapp.data.remote.retrofit.ApiConfig
import com.mansao.mystoryapp.preference.User
import com.mansao.mystoryapp.preference.UserPreference
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel(private val pref: UserPreference) : ViewModel() {

    private var _logMessage = MutableLiveData<String>()
    val logMessage: LiveData<String> = _logMessage

    private var _alertMessage = MutableLiveData<String>()
    val alertMessage: LiveData<String> = _alertMessage

    private var _registerStatus = MutableLiveData<Boolean>()
    val registerStatus: LiveData<Boolean> = _registerStatus

    fun saveUser(user: User) {
        viewModelScope.launch {
            pref.saveUser(user)
        }
    }

    fun register(name: String, email: String, password: String) {
        val service = ApiConfig.getApiService().register(name, email, password)
        service.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    _logMessage.value = "onSuccess: ${responseBody.message}"
                    if (responseBody.message != "Email not available") {
                        saveUser(User(name, email, password, false, ""))
                        _alertMessage.value = "Register Success!"
                        _registerStatus.value = true
                    }
                } else {
                    if (responseBody?.message == "Email not available") {
                        saveUser(User(name, email, password, false, ""))
                        _alertMessage.value =
                            "Ops!, this email already used\n please try another email"
                        _registerStatus.value = false

                    }
                }
                _logMessage.value = "onFailure: ${response.message()}"
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }
}