package com.example.beerdiary

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.beer_new.*
import kotlinx.android.synthetic.main.beer_review_info.*
import kotlinx.android.synthetic.main.beer_review_info.beer_score_bar

class FragmentBeerInfo : Fragment(), OnMapReadyCallback {

    companion object {
        @JvmStatic
        fun newInstance(position: Int): FragmentBeerInfo {
            val detailFragment = FragmentBeerInfo()
            val args = Bundle()
            args.putInt("pos", position)
            detailFragment.arguments = args
            return detailFragment
        }
    }

    private lateinit var beerAndReview: BeerAndReviews
    private var position: Int = 0
    private lateinit var mapViewInfo: com.google.android.gms.maps.MapView
    private lateinit var mMap: GoogleMap

    override fun onAttach(context: Context) {
        context.let { super.onAttach(it) }
        arguments?.getInt("pos")?.let { position = it }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Thread(Runnable { getContent() }).start()
        val view = inflater.inflate(R.layout.beer_review_info, container, false)

        if ((ContextCompat.checkSelfPermission(
                activity!!.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }

        mapViewInfo = view.findViewById(R.id.mapView)
        mapViewInfo.onCreate(savedInstanceState)
        mapViewInfo.getMapAsync(this)
        mapViewInfo.setOnLongClickListener() {
            beerAndReview.review?.reviewId?.let { it1 -> FragmentLocation.newInstance(it1) }
                ?.let { it2 ->
                    activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(
                            R.id.fragment_container,
                            it2
                        )
                        ?.addToBackStack(null)?.commit()
                }
            true
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if ((ContextCompat.checkSelfPermission(
                activity!!.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
    }

    override fun onResume() {
        super.onResume()
        mapViewInfo.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapViewInfo.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapViewInfo.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapViewInfo.onLowMemory()
    }

    override fun onMapReady(gMap: GoogleMap?) {
        if (gMap != null) {
            mMap = gMap
            //val googleMapOptions = GoogleMapOptions().liteMode(true)
            //mMap.mapType = googleMapOptions.mapType
            updateMap()
        }
    }

    private fun updateMap() {
        if (beerAndReview.review != null) {
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        beerAndReview.review!!.latitude,
                        beerAndReview.review!!.longitude
                    ),
                    15.0f
                )
            )
            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.addMarker(MarkerOptions().position(
                LatLng(beerAndReview.review!!.latitude,
                    beerAndReview.review!!.longitude)
            ))
        }
    }

    private fun getContent() {
        val db = activity?.applicationContext?.let { BeerDB.get(it) }
        val temp = db?.beerDao()?.getBeersAndReviewsR()?.get(position)
        Log.d("get", temp.toString())
        if (temp != null) {
            beerAndReview = temp
        }
        update()
    }

    private fun update() {
        this.activity?.runOnUiThread {
            beer_name.text = beerAndReview.beer?.beerName
            beer_brewer.text = beerAndReview.beer?.brewer
            beer_score_bar.rating = beerAndReview.review!!.score
            beer_comment.text = beerAndReview.review?.comment

            val mCurrentPhotoPath = beerAndReview.beer?.imagePath
            val imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
            imageView_info.setImageBitmap(imageBitmap)
            val exif = ExifInterface(mCurrentPhotoPath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            val angle = rotateImageAngle(orientation)
            imageView_info.rotation = angle.toFloat()
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
