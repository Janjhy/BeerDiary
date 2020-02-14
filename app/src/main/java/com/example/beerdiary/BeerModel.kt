package com.example.beerdiary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class BeerModel (application: Application): AndroidViewModel(application) {

    private val beers: LiveData<List<Beer>> = BeerDB.get(getApplication()).beerDao().getBeers()

    fun getBeers() = beers
}