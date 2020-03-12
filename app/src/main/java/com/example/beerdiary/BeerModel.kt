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
            if (currentOrder == "Name") beers.value = it

        }
        beers.addSource(beersBrewer) {
            if (currentOrder == "Brewer") beers.value = it

        }
        beers.addSource(beersScore) {
            if (currentOrder == "Score") beers.value = it

        }
    }

    fun getBeers(): LiveData<List<BeerAndReviews>> {
        return beers
    }

    //Gets the desired order and sets the ViewModel value to the correct list
    fun sortBeers(order: String) {
        when (order) {
            "Name" -> beersName.value?.let {
                Log.d("name sort", it.toString())
                beers.value = it
            }
            "Brewer" -> beersBrewer.value?.let {
                Log.d("brewer sort", it.toString())
                beers.value = it
            }
            "Score" -> beersScore.value?.let {
                Log.d("score sort", it.toString())
                beers.value = it
            }
        }
        currentOrder = order
    }
}