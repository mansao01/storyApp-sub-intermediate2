package com.mansao.mystoryapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mansao.mystoryapp.preference.User
import com.mansao.mystoryapp.preference.UserPreference
import kotlinx.coroutines.launch

class LoginViewModel(private val pref: UserPreference) : ViewModel() {


    fun getUser(): LiveData<User> {
        return pref.getUser().asLiveData()
    }

    fun login() {
        viewModelScope.launch {
            pref.login()
        }
    }

    fun token(user: User) {
        viewModelScope.launch {
            pref.token(user)
        }
    }


}