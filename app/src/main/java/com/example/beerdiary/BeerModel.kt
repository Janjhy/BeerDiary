package com.example.beerdiary

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class BeerModel(application: Application) : AndroidViewModel(application) {

    private val beerDB = BeerDB.get(getApplication())

    private val beers: LiveData<List<BeerAndReviews>> = beerDB.beerDao().getBeersAndReviews()

    fun getBeers(): LiveData<List<BeerAndReviews>> {
        Log.d("getBeers()", beers.toString())
        return beers
    }
}