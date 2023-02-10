package com.mansao.mystoryapp.ui.login

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.mansao.mystoryapp.R
import com.mansao.mystoryapp.data.remote.response.LoginResponses
import com.mansao.mystoryapp.data.remote.retrofit.ApiConfig
import com.mansao.mystoryapp.databinding.ActivityLoginBinding
import com.mansao.mystoryapp.preference.User
import com.mansao.mystoryapp.preference.UserPreference
import com.mansao.mystoryapp.ui.home.MainActivity
import com.mansao.mystoryapp.ui.signup.SignUpActivity
import com.mansao.mystoryapp.viewmodel.LoginViewModel
import com.mansao.mystoryapp.viewmodel.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var user: User
    private val loginViewModel by viewModels<LoginViewModel> {
        ViewModelFactory(UserPreference.getInstance(dataStore))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


        showProgressBar(false)

        loginViewModel.getUser().observe(this) {
            this.user = it
        }
        toRegister()

        setupAction()

        playAnimation()
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            when {
                email.isEmpty() -> {
                    binding.edtEmail.error = getString(R.string.empty_email)
                }
                password.isEmpty() -> {
                    binding.edtPassword.error = getString(R.string.empty_password)
                }
                else -> {
                    loginData(email, password)
                    loginViewModel.login()
                }
            }
        }

    }


    private fun loginData(email: String, password: String) {
        showProgressBar(true)

        val service = ApiConfig.getApiService().login(email, password)
        service.enqueue(object : Callback<LoginResponses> {

            override fun onResponse(
                call: Call<LoginResponses>,
                response: Response<LoginResponses>
            ) {
                showProgressBar(false)

                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Log.e(TAG, "onSuccess: ${response.message()}")
                    loginViewModel.token(
                        User(
                            user.name,
                            user.email,
                            user.password,
                            false,
                            responseBody.loginResult.token
                        )
                    )
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResponses>, t: Throwable) {
                showProgressBar(false)
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun toRegister() {
        binding.tvRegister.setOnClickListener {
            Intent(this, SignUpActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
    }

    private fun showProgressBar(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }


    companion object {
        private val TAG = LoginActivity::class.java.simpleName
    }

}