package com.example.beerdiary

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.beer_card.view.*

class BeerListAdapter(
    private val items: List<BeerAndReviews>,
    private val context: Context,
    private val listener: BeerListFragment.BeerFragmentListener
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.beer_card, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.beerName.text = item.beer?.beerName
        holder.itemView.setOnClickListener {
            listener.onButtonClick(position)
        }
        var score = context.getString(R.string.card_beer_score, item.review?.score)
        if (score.contains("0")) {
            score = score.replace(".0", "")
        }

        holder.cardScore.text = score
        val photoPath = item.beer?.imagePath
        val imageBitmap = BitmapFactory.decodeFile(photoPath)
        val exif = ExifInterface(photoPath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
        val angle = rotateImageAngle(orientation)
        holder.beerImage.rotation = angle.toFloat()
        holder.beerImage.setImageBitmap(imageBitmap)
        holder.beerBrewer.text = item.beer?.brewer
        if (beerTypes.containsKey(item.beer?.beerType)) {
            val color = beerTypes[item.beer?.beerType]
            holder.cardColorBox.setBackgroundColor(Color.parseColor(color))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun rotateImageAngle(orientation: Int): Int {
        return when (orientation) {
            3, 4 -> 180
            5, 6 -> 90
            7, 8 -> 270
            else -> 0
        }
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val beerName: TextView = view.beerName
    val cardScore: TextView = view.cardScore
    val beerImage: ImageView = view.beerImage
    val beerBrewer: TextView = view.beerBrewery
    val cardColorBox: TextView = view.cardColorBox
}