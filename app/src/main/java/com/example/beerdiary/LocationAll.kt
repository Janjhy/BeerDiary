package com.example.beerdiary

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.location_all.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class LocationAll : AppCompatActivity() {

    private lateinit var list: List<BeerAndReviews>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contxt = applicationContext
        Configuration.getInstance()
            .load(contxt, PreferenceManager.getDefaultSharedPreferences(contxt))
        setContentView(R.layout.location_all)
        map_all.setTileSource(TileSourceFactory.MAPNIK)
        map_all.setBuiltInZoomControls(true)
        map_all.setMultiTouchControls(true)
        map_all.controller.setZoom(12.0)

        Thread(Runnable { getList() }).start()
    }

    private fun setMarkers() {
        val avLat = mutableListOf<Double>()
        val avLong = mutableListOf<Double>()
        for (item in list) {
            if (item.review != null && item.beer != null) {
                avLat.add(item.review.latitude)
                avLong.add(item.review.longitude)
                addMarker(item)
            }
        }
        if (avLat.size != 0 && avLong.size != 0) {
            val latAverage = avLat.average()
            val longAverage = avLong.average()
            runOnUiThread { map_all.controller.setCenter(GeoPoint(latAverage, longAverage)) }
        }
    }

    private fun getList() {
        list = BeerDB.get(applicationContext).beerDao().getBeersAndReviewsR()
        setMarkers()
    }

    private fun addMarker(beer: BeerAndReviews) {
        val marker = Marker(map_all)
        marker.position = GeoPoint(beer.review!!.latitude, beer.review.longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = beer.beer?.beerName
        map_all.overlays.add(marker)
    }
}