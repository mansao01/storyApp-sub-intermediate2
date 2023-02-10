package com.mansao.mystoryapp.ui.maps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.mansao.mystoryapp.R
import com.mansao.mystoryapp.preference.UserPreference
import com.mansao.mystoryapp.viewmodel.MapsViewModel
import com.mansao.mystoryapp.viewmodel.ViewModelFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val mapsViewModel by viewModels<MapsViewModel> {
        ViewModelFactory(UserPreference.getInstance(dataStore))
    }
    private lateinit var mMap: GoogleMap
    private val boundsBuilder = LatLngBounds.builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.story_location)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        getMyLocation()
        setMapStyle()
        getMaps()
    }

    private fun setMapStyle() {
        try {
            val success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success){
                Log.e(TAG, "style parsing failed")
            }
        }catch (e: Resources.NotFoundException){
            e.printStackTrace()
        }
    }

    private fun getMaps() {
        mapsViewModel.apply {
            getUser().observe(this@MapsActivity) {
                val token = it.token
                Log.d(TAG, "token: ${it.token}")
                getMapsLocation(token)
            }
            mapsResponse.observe(this@MapsActivity) {
                val storyWithLocation = it.listStory
                Log.d(TAG, it.listStory.toString())

                storyWithLocation.forEach { item ->
                    val latLng = LatLng(item.lat, item.lon)
                    mMap.addMarker(
                        MarkerOptions().position(latLng).title(item.name).snippet(item.description)
                    )
                    boundsBuilder.include(latLng)
                }
                val firstItemLat = storyWithLocation[0].lat
                val firstItemLon = storyWithLocation[0].lon
                val firstShoot = LatLng(firstItemLat, firstItemLon)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(firstShoot))

                val bound = boundsBuilder.build()
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        bound,
                        resources.displayMetrics.widthPixels,
                        resources.displayMetrics.heightPixels,
                        300
                    )
                )
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    companion object {
        private val TAG = MapsActivity::class.java.simpleName
    }
}