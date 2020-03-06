package com.example.beerdiary

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BeerDao {
    @Transaction
    @Query("SELECT * FROM beers, reviews WHERE beers.beer_id = reviews.review_beer_id")
    fun getBeersAndReviews() : LiveData<List<BeerAndReviews>>

    @Query("SELECT * FROM beers, reviews WHERE beers.beer_id = reviews.review_beer_id")
    fun getBeersAndReviewsR() : List<BeerAndReviews>

    @Transaction
    @Query("SELECT * FROM beers")
    fun getBeersR() : List<Beer>

    @Transaction
    @Query("SELECT * FROM reviews")
    fun getReviews() : List<Review>

    @Transaction
    @Query("SELECT * FROM beers")
    fun getBeers() : LiveData<List<Beer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBeer(beer: Beer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReview(review: Review): Long

    @Query("SELECT * FROM reviews WHERE review_beer_id = :beerID")
    fun getBeerReview(beerID: Long): Review

    @Query("UPDATE reviews SET review_latitude = :latitude, review_longitude = :longitude WHERE review_id =:id")
    fun updateLocation(latitude: Double, longitude: Double, id: Long)
}