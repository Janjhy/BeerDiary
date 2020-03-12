package com.example.beerdiary

import android.content.Context
import androidx.room.*

@Database(entities =  [(Beer::class), (Review::class)], version = 6, exportSchema = false)
abstract class BeerDB: RoomDatabase() {
    abstract fun beerDao(): BeerDao

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