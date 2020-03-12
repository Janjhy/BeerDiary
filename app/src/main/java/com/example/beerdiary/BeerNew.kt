package com.example.beerdiary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.beer_new.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

class BeerNew : AppCompatActivity(), SensorEventListener {
    private lateinit var location: Location
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_CHECK_SETTINGS = 2
    private var imageFile: File? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var request: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var gettingLocation = false
    private var pickedSize: Double = 0.0
    private var pickedType: String = ""
    private lateinit var typesArray: Array<String>
    private var goodPhoto: Boolean = false
    private lateinit var light: Sensor
    private lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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

        createLocationRequest()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(request)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                location = it
                Log.d("location", it.toString())
            }
        }

        task.addOnFailureListener {
                exception ->
            if(exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendException: IntentSender.SendIntentException) {

                }
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                result ?: return
                for (locationTemp in result.locations) {
                    location = locationTemp
                    makeUseOfNewLocation()
                }
            }
        }
        beginLocationUpdates()
        mapView_new.controller.setZoom(15.0)

        //Setup light sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_FASTEST)

        imageView_new.setOnLongClickListener() {
            savePhotoIntent()
            true
        }

        btn_add.setOnClickListener {
            add()
        }

        button_add_image.setOnClickListener {
            savePhotoIntent()
        }

        //Setup size dropdown menu
        val arrayAdapterSize = ArrayAdapter(this, R.layout.size_dropdown_popup_item, sizeItems)
        val filledExposedDropdown =  this.findViewById<AutoCompleteTextView>(R.id.filled_exposed_dropdown)
        filledExposedDropdown.setAdapter(arrayAdapterSize)
        filledExposedDropdown.setOnItemClickListener { _, _, position, _ ->  onSizeSelected(
            position
        )}

        //Setup type dropdown menu
        typesArray = beerTypes.keys.toTypedArray()
        val arrayAdapterType = ArrayAdapter(this, R.layout.type_dropdown_pop_item, typesArray)
        val typeExposedDropdown =  this.findViewById<AutoCompleteTextView>(R.id.type_exposed_dropdown)
        typeExposedDropdown.setAdapter(arrayAdapterType)
        typeExposedDropdown.setOnItemClickListener { _, _, position, _ ->  onTypeSelected(
            position
        )}
    }

    private fun onTypeSelected(position: Int) {
        pickedType = typesArray[position]
    }

    private fun onSizeSelected(position: Int) {
        Log.d("dropdown", sizeItems[position].toString())
        pickedSize = sizeItems[position]
    }

    private fun add() {
        if(!validate()) {
            return
        }
        val beerName = et_beer_name.text.toString()
        val brewerName = et_beer_brewer.text.toString()
        val comment = et_beer_comment.text.toString()
        val score = beer_score_bar.rating
        val lat = location.latitude
        val long = location.longitude
        val strengthTemp = et_beer_strength.text.toString().toDouble()
        val strength = BigDecimal(strengthTemp).setScale(1, RoundingMode.HALF_EVEN).toDouble()
        val size = pickedSize
        val type = pickedType
        val image = imageFile
        val path = image!!.absolutePath
        val beer = Beer(0, beerName, brewerName, path, size, type, strength)
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

    override fun onResume() {
        super.onResume()
        if(!gettingLocation) beginLocationUpdates()
    }

    private fun createLocationRequest() {
        request = LocationRequest.create().apply {
            interval = 15000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun beginLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
        gettingLocation = true
    }

    private fun endLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        gettingLocation = false
    }

    override fun onPause() {
        super.onPause()
        endLocationUpdates()
        sensorManager.unregisterListener(this)
    }

    private fun newMarker() {
        mapView_new.overlays.clear()
        val marker = Marker(mapView_new)
        marker.position = GeoPoint(location.latitude, location.longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView_new.overlays.add(marker)
    }

    private fun makeUseOfNewLocation() {
        mapView_new.controller.setCenter(GeoPoint(location.latitude, location.longitude))
        newMarker()
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
            goodPhoto = true
            Log.d("success", "result ok, goodphoto is $goodPhoto")
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_CANCELED) {
            Log.d("error", "result canceled")
            goodPhoto = false
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

    private fun validate(): Boolean {
        if(et_beer_name.text.toString().trim().equals("", true)) {
            et_beer_name.error = "Name can not be blank."
            return false
        }
        if(et_beer_brewer.text.toString().trim().equals("", true)) {
            et_beer_brewer.error = "Brewer can not be blank."
            return false
        }
        if(pickedSize < 250.0) {
            Snackbar.make(findViewById(R.id.view_new_beer), "Please pick a size.", Snackbar.LENGTH_SHORT).setAnchorView(R.id.btn_add).show()
            return false
        }
        if (imageFile == null) {
            Snackbar.make(findViewById(R.id.view_new_beer), "Please take a picture.", Snackbar.LENGTH_SHORT).setAnchorView(R.id.btn_add).show()
            return false
        }
        if(!goodPhoto) {
            Log.d("error", "goodphoto is $goodPhoto")
            Snackbar.make(findViewById(R.id.view_new_beer), "Please take a picture.", Snackbar.LENGTH_SHORT).setAnchorView(R.id.btn_add).show()
            return false
        }
        if(et_beer_strength.text.toString().toDoubleOrNull() == null) {
            et_beer_strength.error = "Value is not a valid number."
            return false
        }
        if(pickedType == "") {
            Snackbar.make(findViewById(R.id.view_new_beer), "Please pick a type.", Snackbar.LENGTH_SHORT).setAnchorView(R.id.btn_add).show()
            return false
        }
        return true
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d("sensor event", event?.values?.get(0)?.toString())
    }
}

