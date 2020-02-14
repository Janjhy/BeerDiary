package com.example.beerdiary

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.beer_item.view.*

class MyAdapter(val items: List<Beer>, val context: Context, val listener: BeerListFragment.BeerFragmentListener) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.beer_card, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.beerName.text = items[position].beerName
        holder.itemView.setOnClickListener{
            listener.onButtonClick(position)
        }
    }

    override fun getItemCount(): Int {
        Log.d("items size", items.size.toString())
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val beerName = view.beerName
}