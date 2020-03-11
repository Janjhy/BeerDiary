package com.example.beerdiary

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class BeerModel(application: Application) : AndroidViewModel(application) {

    private val beersDB = BeerDB.get(getApplication()).beerDao()
    private val beersName = beersDB.beersByName()
    private val beersBrewer = beersDB.beersByBrewer()
    private val beersScore = beersDB.beersByScore()

    private val beers = MediatorLiveData<List<BeerAndReviews>>()

    private var currentOrder = "Name"

    init {
            beers.addSource(beersName) {
                if(currentOrder == "Name") beers.value = it

            }
            beers.addSource(beersBrewer) {
                if(currentOrder == "Brewer") beers.value = it

            }
            beers.addSource(beersScore) {
                if(currentOrder == "Score") beers.value = it

            }
    }

    fun getBeers(): LiveData<List<BeerAndReviews>> {
        Log.d("getBeers()", beers.toString())
        return beers
    }

    fun sortBeers(order: String) {
        Log.d("Thread viewmodel", Thread.currentThread().toString())
        Log.d("viewmodel", "called $order")
        when(order) {
            "Name" -> beersName.value?.let {
                beers.value = it
            }
            "Brewer" -> beersBrewer.value?.let {
                beers.value = it
            }
            "Score" -> beersScore.value?.let {
                beers.value = it
            }
        }
        //Log.d("viewmodel", beers.value.toString())
        currentOrder = order
    }
}