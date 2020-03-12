package com.example.beerdiary

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), BeerListFragment.BeerFragmentListener {

    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, BeerListFragment.newInstance(), "beerList").commit()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION).apply {
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        broadcastReceiver = BeerDiaryBroadcastReceiver()
        registerReceiver(broadcastReceiver, filter)
    }

    override fun onButtonClick(position: Int) {
        Log.d("onbtnclick", "clicked at pos $position")
        val detailFragment = FragmentBeerInfo
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, detailFragment.newInstance(position)).addToBackStack(null).commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
}
