package com.example.beerdiary

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.beer_review_info.*
import kotlinx.android.synthetic.main.beer_review_info.beer_score_bar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        val view = inflater.inflate(R.layout.beer_review_info, container, false)

        mapViewInfo = view.findViewById(R.id.mapView)
        mapViewInfo.onCreate(savedInstanceState)
        mapViewInfo.getMapAsync(this)
        setHasOptionsMenu(true)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = (requireActivity() as AppCompatActivity)
        activity.setSupportActionBar(bar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.info_bottom_menu, menu)
        Log.d("inflate", "menu inflated")
        bar.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.app_bar_delete -> {
                    activity?.let {
                        MaterialAlertDialogBuilder(it).setTitle("Confirm deletion")
                            .setMessage("This will remove the shown beer. This action is irreversible.")
                            .setPositiveButton(R.string.confirm ) { _, _ ->  Thread(Runnable { delete() }).start()
                                activity?.supportFragmentManager?.popBackStack()
                            }
                            .setNegativeButton(R.string.cancel) { _, _ ->
                            }
                    }?.show()
                    true
                }
                R.id.app_bar_edit -> {
                    val intent = Intent(activity, BeerEdit::class.java)
                    intent.putExtra("BEER_ID" , beerAndReview.beer?.beerId)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun delete() {
        Log.d("bottombar", "pressed delete")
        context?.let { beerAndReview.beer?.let { it1 -> BeerDB.get(it).beerDao().deleteBeer(it1) } }
    }



    override fun onResume() {
        super.onResume()
        Log.d("fragment lifecycle", "resume")
        Thread(Runnable { getContent() }).start()
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
        val size = beerAndReview.beer?.beerSize
        size?.let { callWebService(it) }
        val mCurrentPhotoPath = beerAndReview.beer?.imagePath
        val imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
        val exif = ExifInterface(mCurrentPhotoPath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
        val angle = rotateImageAngle(orientation)
        val strength = beerAndReview.beer?.beerStrength

        this.activity?.runOnUiThread {
            beer_name.text = beerAndReview.beer?.beerName
            beer_brewer.text = beerAndReview.beer?.brewer
            beer_score_bar.rating = beerAndReview.review!!.score
            beer_comment.text = beerAndReview.review?.comment
            beer_size.text = activity?.applicationContext?.getString(R.string.beer_size_ml,
                    size
                )
            imageView_info.setImageBitmap(imageBitmap)
            imageView_info.rotation = angle.toFloat()
            beer_type.text = beerAndReview.beer?.beerType
            beer_strength.text = "${strength.toString()}%"
            updateMap()
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

    private fun callWebService(quantity: Double) {
        val call = WebActivity().service.unitConversion(quantity)
        val value = object : Callback<WebActivity.Model.Result> {
            override fun onResponse(call: Call<WebActivity.Model.Result>, response:
            Response<WebActivity.Model.Result>?) {
                if (response != null) {
                    val res: WebActivity.Model.Result = response.body()!!
                    Log.d("DBG()", "${res}")
                    beer_size_oz.text =
                        activity?.applicationContext?.getString(R.string.beer_size_oz,
                            res.UCUMWebServiceResponse.Response.ResultQuantity
                        )
                }
            }
            override fun onFailure(call: Call<WebActivity.Model.Result>, t: Throwable) {
                Log.e("DBG(failure)", t.toString())
            }
        }
        call.enqueue(value) // asynchronous request

    }
}

