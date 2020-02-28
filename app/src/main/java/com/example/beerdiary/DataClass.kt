package com.example.beerdiary

import androidx.room.*

@Entity(tableName = "beers")
data class Beer(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "beer_id") val beerId: Long,
    @ColumnInfo(name = "beer_name") val beerName: String,
    @ColumnInfo(name = "beer_brewer") val brewer: String
)

@Entity(tableName = "reviews",
    foreignKeys = [(ForeignKey(entity = Beer::class,
        parentColumns = ["beer_id"],
        childColumns = ["review_beer_id"],
        onDelete = ForeignKey.CASCADE
    ))]
)
data class Review(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "review_id") val reviewId: Long,
    @ColumnInfo(name = "review_score", defaultValue = "-1") val score: Int,
    @ColumnInfo(name = "review_comment", defaultValue = "no comment") val comment: String,
    @ColumnInfo(name = "review_beer_id") val beerIdReview: Long
)

data class BeerAndReviews(
    @Embedded
    val beer: Beer?,
    @Relation(
        parentColumn = "beer_id",
        entityColumn = "review_beer_id"
    )
    val review: List<Review>? = null
)