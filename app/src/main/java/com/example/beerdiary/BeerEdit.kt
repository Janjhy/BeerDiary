package com.example.beerdiary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
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
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.MapView
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.beer_new.*
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File

class BeerEdit: AppCompatActivity(), MapEventsReceiver {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var marker: Marker
    private val REQUEST_IMAGE_CAPTURE = 1
    private var imageFile: File? = null
    private val sizeItems: Array<Double> = arrayOf(250.0, 330.0, 355.0, 400.0, 500.0, 568.0)
    private var pickedSize: Double = 0.0
    private var beerID: Long = 0
    private var reviewID: Long = 0
    private lateinit var arrayAdapter: ArrayAdapter<Double>
    private lateinit var filledExposedDropdown: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beerID = intent.getLongExtra("BEER_ID", 0)


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

        imageView_new.setOnLongClickListener() {
            Log.d("click", "long click")
            savePhotoIntent()
            true
        }
        setUp()
        marker = Marker(mapView_new)
        btn_add.setOnClickListener {
            update()
        }

        val mapEventsOverlay = MapEventsOverlay(this, this)
        mapView_new.overlays.add(0, mapEventsOverlay)
        mapView_new.controller.setZoom(15.0)

        //Setup dropdown menu
        arrayAdapter = ArrayAdapter(this, R.layout.size_dropdown_popup_item, sizeItems)
        filledExposedDropdown =  this.findViewById(R.id.filled_exposed_dropdown)
        filledExposedDropdown.setAdapter(arrayAdapter)
        filledExposedDropdown.setOnItemClickListener { parent, view, position, id ->  onSizeSelected(parent, view, position, id)}
    }

    private fun onSizeSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.d("dropdown", sizeItems[position].toString())
        pickedSize = sizeItems[position]
    }

    private fun setUp() {
        Thread(Runnable {
            val beerAndReview = BeerDB.get(applicationContext).beerDao().getBeerAndReview(beerID)
            val reviewTemp = beerAndReview.review
            val beerTemp = beerAndReview.beer
            val photoPath = beerTemp?.imagePath
            imageFile = File(photoPath)
            val imageBitmap = BitmapFactory.decodeFile(photoPath)
            val exif = ExifInterface(photoPath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            val angle = rotateImageAngle(orientation)
            latitude = reviewTemp?.latitude!!
            longitude = reviewTemp.longitude
            pickedSize = beerTemp?.beerSize!!
            btn_add.text = getString(R.string.confirm)
            reviewID = reviewTemp.reviewId

            runOnUiThread {
                et_beer_name.setText(beerTemp?.beerName, TextView.BufferType.EDITABLE)
                et_beer_comment.setText(reviewTemp?.comment, TextView.BufferType.EDITABLE)
                et_beer_brewer.setText(beerTemp?.brewer, TextView.BufferType.EDITABLE)
                beer_score_bar.rating = reviewTemp?.score!!
                filledExposedDropdown.hint = beerTemp?.beerSize?.toInt().toString() + " ml"
                imageView_new.rotation = angle.toFloat()
                imageView_new.setImageBitmap(imageBitmap)
                newMarker()
            }
        }).start()
    }

    private fun update() {
        val beerName = et_beer_name.text.toString()
        val brewerName = et_beer_brewer.text.toString()
        val comment = et_beer_comment.text.toString()
        val score = beer_score_bar.rating
        Log.d("on update location","$latitude and $longitude")
        val lat = latitude
        val long = longitude
        if(pickedSize < 250.0) {
            Log.d("insert", "no size")
            return
        }
        val size = pickedSize
        if (imageFile == null) {
            Log.d("insert", "no image")
            return
        }
        val image = imageFile
        val path = image!!.absolutePath
        val db = BeerDB.get(this)
        val id: Long = beerID
        val beer = Beer(id, beerName, brewerName, path, size)
        val review = Review(reviewID, score, comment, id, lat, long)
        val firstThread = Thread(Runnable {
            //db.clearAllTables()
            db.beerDao().updateBeer(beer, review)

            Log.d("insert", "inserted beer id: $id")

            if (db != null) {
                Log.d("contents", "beer " + (db.beerDao().getBeersR()))
            }
        })
        firstThread.start()
        /*firstThread.join()
        val review = Review(0, score, comment, id, lat, long)
        val secondThread = Thread(Runnable {
            val reviewid = db.beerDao().insertReview(review)
            Log.d("insert", "inserted review id: $reviewid")
            Log.d("contents", "review  " + (db.beerDao().getReviews()))
            if (db != null) {
                Log.d("contents", "beer and review  " + (db.beerDao().getBeersAndReviewsR()))
            }
        })
        secondThread.start()*/
        finish()
    }

    private fun newMarker() {
        mapView_new.overlays.remove(marker)
        marker.position = GeoPoint(latitude, longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView_new.overlays.add(marker)
        mapView_new.controller.setCenter(GeoPoint(latitude, longitude))
    }

    private fun savePhotoIntent() {
        var tempImageFile: File?
        val fileName = "temp_photo"
        val imgPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        tempImageFile = File.createTempFile(fileName, ".jpg", imgPath)
        val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "com.example.beerdiary.fileprovider",
            tempImageFile
        )
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

            val imageBitmapNext = BitmapFactory.decodeFile(mCurrentPhotoPath)
            val exif = ExifInterface(mCurrentPhotoPath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)

            val angle = rotateImageAngle(orientation)
            imageView_new.setImageBitmap(imageBitmapNext)
            imageView_new.rotation = angle.toFloat()
        }
    }

    private fun rotateImageAngle(orientation: Int): Int {
        Log.d("exif", "orientation is $orientation")
        return when (orientation) {
            3, 4 -> 180
            5, 6 -> 90
            7, 8 -> 270
            else -> 0
        }
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        return false
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        Log.d("tapped", "at ${p?.latitude} ${p?.longitude}" )
        latitude = p?.latitude!!
        longitude = p.longitude
        Log.d("on tap result","$latitude and $longitude")
        newMarker()
        return true;
    }
}

