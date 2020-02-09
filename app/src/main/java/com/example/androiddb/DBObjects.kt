package com.example.androiddb

import android.content.Context
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

@Entity(tableName = "reviews"
)
data class Review(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "review_id") val reviewId: Long,
    val score: Int,
    val comment: String,
    val beerIdReview: Long
)

data class BeerAndReview(
    @Embedded
    val beer: Beer?,
    @Relation(
        parentColumn = "beer_id",
        entityColumn = "beerIdReview"
    )
    val review: Review
)

@Dao
interface BeerDao {

    @Transaction
    @Query("SELECT * FROM beers")
    fun getBeersAndReviews() : LiveData<List<BeerAndReview>>

    @Transaction
    @Query("SELECT * FROM beers")
    fun getBeersR() : List<Beer>

    @Transaction
    @Query("SELECT * FROM beers")
    fun getBeers() : LiveData<List<Beer>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBeer(beer: Beer): Long
}

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReview(review: Review)

    @Query("SELECT * FROM reviews WHERE beerIdReview = :beerID")
    fun getBeerReview(beerID: Long): Review
}

@Database(entities =  [(Beer::class), (Review::class)], version = 1, exportSchema = false)
abstract class BeerDB: RoomDatabase() {
    abstract fun beerDao(): BeerDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        private var singleInstance: BeerDB? = null

        @Synchronized
        fun get(context: Context): BeerDB {
            if (singleInstance == null) {
                singleInstance = Room.databaseBuilder(context.applicationContext, BeerDB::class.java, "beer.db").fallbackToDestructiveMigration().build()
            }
            return singleInstance!!
        }
    }
}