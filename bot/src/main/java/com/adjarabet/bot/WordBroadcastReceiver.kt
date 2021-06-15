package com.adjarabet.bot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class WordBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val inputWord = intent?.getStringExtra("data")
        Toast.makeText(context,inputWord, Toast.LENGTH_SHORT).show()
    }
}