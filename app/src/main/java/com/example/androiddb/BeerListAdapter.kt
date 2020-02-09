package com.example.androiddb

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MyAdapter( val context: Context, val listener: BeerListFragment.BeerFragmentListener) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.beer_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder.presidentName.text = presidents[position].getName()
        holder.itemView.setOnClickListener{
            listener.onButtonClick(position)
        }
    }

    override fun getItemCount(): Int {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    //val presidentName = view.president_name
}