package com.mansao.mystoryapp.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun login() {
        dataStore.edit {
            it[STATE] = true
        }
    }

    suspend fun logout() {
        dataStore.edit {
            it[STATE] = false
            it.clear()
        }
    }

    suspend fun token(user: User) {
        dataStore.edit {
            it[TOKEN] = user.token
        }
    }

    suspend fun saveUser(user: User) {
        dataStore.edit {
            it[NAME] = user.name
            it[EMAIL] = user.email
            it[PASSWORD] = user.password
            it[STATE] = user.isLogin
        }
    }

    fun getUser(): Flow<User> {
        return dataStore.data.map {
            User(
                it[NAME] ?: "",
                it[EMAIL] ?: "",
                it[PASSWORD] ?: "",
                it[STATE] ?: false,
                it[TOKEN] ?: ""
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null
        private val NAME = stringPreferencesKey("name")
        private val EMAIL = stringPreferencesKey("email")
        private val PASSWORD = stringPreferencesKey("password")
        private val TOKEN = stringPreferencesKey("token")
        private val STATE = booleanPreferencesKey("state")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }

    }
}