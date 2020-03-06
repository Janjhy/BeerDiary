package com.example.beerdiary

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.set_location.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class FragmentLocation : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(id: Long): FragmentLocation {
            val locationFragment = FragmentLocation()
            val args = Bundle()
            args.putLong("id", id)
            locationFragment.arguments = args
            return locationFragment
        }
    }

    private lateinit var locationManager: LocationManager
    private lateinit var location: Location
    private var id: Long = -1

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getLong("id")?.let { id = it }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.set_location, container, false)
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val contxt = activity?.applicationContext
        Configuration.getInstance()
            .load(contxt, PreferenceManager.getDefaultSharedPreferences(contxt))
        map.setTileSource(TileSourceFactory.MAPNIK)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)

        if ((activity?.applicationContext?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED)
        ) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    0
                )
            }
        }

        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        map.controller.setCenter(GeoPoint(location.latitude, location.longitude))

        val locationListener = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                makeUseOfNewLocation(location)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                Log.d("location", "onStatusChanged")
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }
        }

        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            5000,
            5f,
            locationListener
        )
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000,
            5f,
            locationListener
        )

        btn_save_location.setOnClickListener {
            saveLocation()
        }
    }

    private fun makeUseOfNewLocation(location: Location) {
        this.location = location
    }

    private fun saveLocation() {
        Thread(Runnable {
            val db = context?.let { it -> BeerDB.get(it) }

            if (db != null && id > -1) {
                db.beerDao().updateLocation(location.latitude, location.latitude, id)
                Log.d(
                    "update",
                    "location (${location.latitude}, ${location.latitude}) of review $id updated"
                )
            }
        }).start()
        activity?.supportFragmentManager?.popBackStack()
    }

}