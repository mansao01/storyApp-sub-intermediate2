package com.mansao.mystoryapp.ui.signup

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import com.mansao.mystoryapp.R
import com.mansao.mystoryapp.databinding.ActivitySignUpBinding
import com.mansao.mystoryapp.preference.UserPreference
import com.mansao.mystoryapp.viewmodel.RegisterViewModel
import com.mansao.mystoryapp.viewmodel.ViewModelFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val registerViewModel by viewModels<RegisterViewModel> {
        ViewModelFactory(UserPreference.getInstance(dataStore))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()


        binding.btnRegister.setOnClickListener {
            actionRegister()
        }

        playAnimation()
    }

    private fun actionRegister() {
        binding.apply {
            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            when {
                name.isEmpty() -> {
                    edtName.error = getString(R.string.empty_name)
                }
                email.isEmpty() -> {
                    edtEmail.error = getString(R.string.empty_email)
                }
                password.isEmpty() -> {
                    edtPassword.error = getString(R.string.empty_password)
                }
                else -> setupRegister(name, email, password)
            }
        }

    }

    private fun setupRegister(name: String, email: String, password: String) {
        Log.d(TAG, name)
        Log.d(TAG, email)
        Log.d(TAG, "pass: $password")
        registerViewModel.apply {
            register(name, email, password)
            showLog(this.logMessage)
            registerStatus.observe(this@SignUpActivity) { status ->
                alertMessage.observe(this@SignUpActivity) {

                    if (status) {
                        AlertDialog.Builder(this@SignUpActivity)
                            .setTitle("Register")
                            .setMessage(it)
                            .setPositiveButton("YES") { _, _ ->
                                finish()
                            }
                            .create()
                            .show()
                    } else {
                        AlertDialog.Builder(this@SignUpActivity)
                            .setTitle("Register")
                            .setMessage(it)
                            .setPositiveButton("YES") { _, _ ->

                            }
                            .create()
                            .show()
                    }
                }

            }
        }
    }

    private  fun playAnimation(){

        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
    }

    private fun showLog(message: LiveData<String>) {
        Log.d(TAG, message.toString())
    }

    companion object {
        private val TAG = SignUpActivity::class.java.simpleName
    }
}