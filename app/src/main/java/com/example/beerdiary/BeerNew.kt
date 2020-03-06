package com.example.beerdiary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.beer_new.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.io.File

class BeerNew : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var location: Location
    private val REQUEST_IMAGE_CAPTURE = 1
    var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if ((ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }

        val contxt = applicationContext

        Configuration.getInstance()
            .load(contxt, PreferenceManager.getDefaultSharedPreferences(contxt))


        setContentView(R.layout.beer_new)
        mapView_new.setTileSource(TileSourceFactory.MAPNIK)
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        mapView_new.controller.setCenter(GeoPoint(location.latitude, location.longitude))
        mapView_new.controller.setZoom(15.0)
        newMarker()

        imageView_new.setOnLongClickListener() {
            Log.d("click", "long click")
            savePhotoIntent()
            true
        }

        btn_add.setOnClickListener {
            val beerName = et_beer_name.text.toString()
            val brewerName = et_beer_brewer.text.toString()
            val comment = et_beer_comment.text.toString()
            val score = beer_score_bar.rating
            val lat = location.latitude
            val long = location.longitude
            if(imageFile == null) {
                Log.d("insert", "no image")
                return@setOnClickListener
            }
            val image = imageFile
            val path = image!!.absolutePath
            Log.d("info", "name and brewer $beerName $brewerName")
            val beer = Beer(0, beerName, brewerName, path)
            val db = BeerDB.get(this)
            var id: Long = 0

            val firstThread = Thread(Runnable {
                //db.clearAllTables()
                id = db.beerDao().insertBeer(beer)

                Log.d("insert", "inserted beer id: $id")

                if (db != null) {
                    Log.d("contents", "beer " + (db.beerDao().getBeersR()))
                }
            })
            firstThread.start()
            firstThread.join()
            val review = Review(0, score, comment, id, lat, long)
            val secondThread = Thread(Runnable {
                val reviewid = db.beerDao().insertReview(review)
                Log.d("insert", "inserted review id: $reviewid")
                Log.d("contents", "review  " + (db.beerDao().getReviews()))
                if (db != null) {
                    Log.d("contents", "beer and review  " + (db.beerDao().getBeersAndReviewsR()))
                }
            })
            secondThread.start()
            finish()
        }

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
    }

    private fun newMarker() {
        mapView_new.overlays.clear()
        val marker = Marker(mapView_new)
        marker.position = GeoPoint(location.latitude, location.longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView_new.overlays.add(marker)
    }

    private fun makeUseOfNewLocation(newLocation: Location) {
        location = newLocation
        mapView_new.controller.setCenter(GeoPoint(location.latitude, location.longitude))
        newMarker()
    }

    private fun savePhotoIntent() {
        var tempImageFile: File?
        val fileName = "temp_photo"
        val imgPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        tempImageFile = File.createTempFile(fileName, ".jpg", imgPath )
        val photoURI: Uri = FileProvider.getUriForFile(this,
            "com.example.beerdiary.fileprovider",
            tempImageFile)
        imageFile = tempImageFile
        val saveIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (saveIntent.resolveActivity(packageManager) != null) {
            saveIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(saveIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val mCurrentPhotoPath = imageFile!!.absolutePath
            //val extras = data!!.extras
            //val imageBitmap = extras!!.get("data") as Bitmap

            val imageBitmapNext = BitmapFactory.decodeFile(mCurrentPhotoPath)
            val exif = ExifInterface(mCurrentPhotoPath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)

            val angle = rotateImageAngle(orientation)
            imageView_new.setImageBitmap(imageBitmapNext)
            imageView_new.rotation = angle.toFloat()
            val imageBitmap1 = Bitmap.createScaledBitmap(imageBitmapNext, 128, 128, false)
            //imageView_new.setImageBitmap(imageBitmap1)
        }
    }

    private fun rotateImageAngle(orientation: Int): Int {
        Log.d("exif", "orientation is $orientation")
        return when(orientation) {
            3, 4 -> 180
            5, 6 -> 90
            7, 8 -> 270
            else -> 0
        }
    }
}

