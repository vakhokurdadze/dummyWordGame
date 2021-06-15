package com.adjarabet.bot

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlin.random.Random

class BotService : Service() {

    private val binder  = LocalBinder()

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    fun userMover(move : String) : String{
        val randomInt:Int = (1..5).random()
        return "$move ${generateRandomWord(randomInt)}"
    }

    fun generateRandomWord(length: Int) : String {
        val charRange = ('A'..'Z') + ('a'..'z')
        return (1..length)
            .map { charRange.random() }
            .joinToString("")
    }

    inner class LocalBinder : Binder() {
        fun getBotService(): BotService = this@BotService
    }
}