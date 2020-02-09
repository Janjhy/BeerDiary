package com.example.androiddb

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Entity(tableName = "beers")
data class Beer(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "beer_id") val beerId: Long,
    @ColumnInfo(name = "beer_name") val beerName: String,
    val brewer: String
)

@Entity(
    foreignKeys = [(ForeignKey(
        entity = Beer::class,
        parentColumns = ["beerId"],
        childColumns = ["reviewId"]
    ))]
)
data class Review(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "review_id") val reviewId: Long,
    val score: Int,
    val comment: String,
    val date: Calendar,
    val beerIdReview: String
)

data class BeerAndReview(
    @Embedded
    val beer: Beer?,
    @Relation(
        parentColumn = "beerId",
        entityColumn = "beerIdReview"
    )
    val review: Review
)

@Dao
interface  BeerDao {

    @Query("SELECT * FROM beers")
    fun getBeersAndReviews() : LiveData<List<BeerAndReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBeer(beer: Beer)
}