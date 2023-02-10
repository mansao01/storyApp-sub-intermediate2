package com.mansao.mystoryapp.ui.add

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.mansao.mystoryapp.R
import com.mansao.mystoryapp.databinding.ActivityAddStoryBinding
import com.mansao.mystoryapp.helper.createCustomTempFile
import com.mansao.mystoryapp.helper.reduceFileImage
import com.mansao.mystoryapp.preference.UserPreference
import com.mansao.mystoryapp.viewmodel.AddStoryViewModel
import com.mansao.mystoryapp.viewmodel.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var currentPhotoPath: String
    private var getFile: File? = null
    private var getToken: String? = null

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            getFile = myFile
            val result = BitmapFactory.decodeFile(myFile.path)
            binding.ivAddPhoto.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val selectedImage: Uri = it.data?.data as Uri
            val myFile = uriToFile(selectedImage, this)
            getFile = myFile
            binding.ivAddPhoto.setImageURI(selectedImage)
        }
    }

    private val addStoryViewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory(UserPreference.getInstance(dataStore))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.add_story)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        showProgressBar(false)

        if (!allPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSION,
                REQUEST_CODE_PERMISSION
            )
        }
        binding.apply {
            btnCamera.setOnClickListener {
                startTakePhoto()
            }
            btnGallery.setOnClickListener {
                startGallery()
            }
            btnAdd.setOnClickListener {
                uploadStory()
            }
        }
    }

    private fun uploadStory() {
        binding.apply {
            val descriptionText = edtAddDescription.text
            when {
                descriptionText.isEmpty() -> {
                    edtAddDescription.error = getString(R.string.empty_description)
                }
                getFile == null -> {
                    Toast.makeText(
                        this@AddStoryActivity,
                        getString(R.string.empty_pict),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    val description =
                        binding.edtAddDescription.text.toString()
                            .toRequestBody("text/plain".toMediaType())
                    val file = reduceFileImage(getFile as File)
                    Log.d(TAG, description.toString())
                    val requestImageFile = file.asRequestBody()
                    val imageMultiPart: MultipartBody.Part = MultipartBody.Part.createFormData(
                        "photo",
                        file.name,
                        requestImageFile
                    )

                    addStoryViewModel.apply {
                        getUser().observe(this@AddStoryActivity) {
                            getToken = it.token
                        }
                        getToken?.let { postStory(imageMultiPart, description, it) }
                        showLoading.observe(this@AddStoryActivity) {
                            showProgressBar(it)
                        }
                        isSucceed.observe(this@AddStoryActivity) {
                            isSucceed(it)
                        }
                    }
                }
            }
        }
    }

    //open camera
    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)
        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddStoryActivity,
                "com.mansao.mystoryapp",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    //open gallery
    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/"
        val chooser = Intent.createChooser(intent, "Choose a picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSION.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (!allPermissionGranted()) {
                Toast.makeText(this, "Tidak mendapatkan permission", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun uriToFile(selectedImage: Uri, context: Context): File {
        val myFile = createCustomTempFile(context)
        val inputStream = contentResolver.openInputStream(selectedImage) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private fun isSucceed(succeed: Boolean) {
        if (succeed) {
            finish()
        } else {
            Toast.makeText(this, "Something is wrong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProgressBar(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()

        return super.onSupportNavigateUp()
    }

    companion object {
        private val TAG = AddStoryActivity::class.java.simpleName
        private val REQUIRED_PERMISSION = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSION = 10
    }
}