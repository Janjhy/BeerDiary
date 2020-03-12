package com.example.beerdiary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class BeerDiaryBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "Change in airplane mode, values might be shown incorrectly.", Toast.LENGTH_SHORT).show()
    }
}