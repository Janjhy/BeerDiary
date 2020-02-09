package com.example.androiddb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), BeerListFragment.BeerFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().add(R.id.fragment_container, BeerListFragment.newInstance(), "beerList").commit()

    }

    override fun onButtonClick(position: Int) {
        Log.d("onbtnclick", "clicked at pos $position")
        val detailFragment = FragmentBeerInfo

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, detailFragment.newInstance(position)).addToBackStack(null).commit()
    }
}
