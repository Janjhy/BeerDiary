package com.example.beerdiary

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BeerDao {
    @Transaction
    @Query("SELECT * FROM beers")
    fun getBeersAndReviews() : LiveData<List<BeerAndReviews>>

    @Transaction
    @Query("SELECT * FROM beers")
    fun getBeersR() : List<Beer>

    @Transaction
    @Query("SELECT * FROM beers")
    fun getBeers() : LiveData<List<Beer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBeer(beer: Beer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReview(review: Review)

    @Query("SELECT * FROM reviews WHERE review_beer_id = :beerID")
    fun getBeerReview(beerID: Long): Review

    @Transaction
    @Insert
    fun insertBeerAndReviews(beer: Beer, review: Review)
}