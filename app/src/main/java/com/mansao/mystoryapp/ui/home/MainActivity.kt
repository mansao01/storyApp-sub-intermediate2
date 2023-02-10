package com.mansao.mystoryapp.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.recyclerview.widget.LinearLayoutManager
import com.mansao.mystoryapp.R
import com.mansao.mystoryapp.adapter.StoryListAdapter
import com.mansao.mystoryapp.databinding.ActivityMainBinding
import com.mansao.mystoryapp.preference.UserPreference
import com.mansao.mystoryapp.ui.add.AddStoryActivity
import com.mansao.mystoryapp.ui.login.LoginActivity
import com.mansao.mystoryapp.ui.maps.MapsActivity
import com.mansao.mystoryapp.viewmodel.MainViewModel
import com.mansao.mystoryapp.viewmodel.ViewModelFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: StoryListAdapter
    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory(UserPreference.getInstance(dataStore))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = StoryListAdapter()
        addStory()
        checkIsLogin()
        showStories()
    }

    private fun showStories() {
        mainViewModel.getUser().observe(this) {
            val token = it.token
            Log.d(TAG, "token: ${it.token}")
            mainViewModel.getStories(token)
        }
        mainViewModel.apply {
            storyResponse.observe(this@MainActivity) {
                adapter.setListStory(it.listStory)
                binding.apply {
                    rvStory.adapter = adapter
                    rvStory.layoutManager = LinearLayoutManager(this@MainActivity)
                    rvStory.setHasFixedSize(true)
                }
            }
            showLoading.observe(this@MainActivity) {
                showProgressBar(it)
            }

        }
    }

    private fun checkIsLogin() {
        mainViewModel.getUser().observe(this) { user ->
            if (!user.isLogin) {
                Intent(this, LoginActivity::class.java).also {
                    startActivity(it)
                }
                finish()
            }
        }
    }


    private fun addStory() {
        binding.fabAdd.setOnClickListener {
            Intent(this, AddStoryActivity::class.java).also {
                startActivity(it)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> mainViewModel.logout()
            R.id.action_setting -> {
                Intent(Settings.ACTION_LOCALE_SETTINGS).also {
                    startActivity(it)
                }
            }
            R.id.action_maps ->{
                Intent(this, MapsActivity::class.java).also {
                    startActivity(it)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showProgressBar(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

}