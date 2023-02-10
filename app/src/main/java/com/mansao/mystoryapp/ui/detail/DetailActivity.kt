package com.mansao.mystoryapp.ui.detail

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mansao.mystoryapp.R
import com.mansao.mystoryapp.data.remote.response.StoryItem
import com.mansao.mystoryapp.databinding.ActivityDetailBinding


class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showDetailData()

        supportActionBar?.title = getString(R.string.detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun showDetailData() {

        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_DATA, StoryItem::class.java)
        } else {
            intent.getParcelableExtra<StoryItem>(EXTRA_DATA)
        }
        binding.apply {
            tvDetailName.text = data?.name
            tvDetailDescription.text = data?.description
            Glide.with(this@DetailActivity)
                .load(data?.photoUrl)
                .centerCrop()
                .into(ivDetailPhoto)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()

        return super.onSupportNavigateUp()
    }

    companion object {
        const val EXTRA_DATA = "extra_data"
    }
}