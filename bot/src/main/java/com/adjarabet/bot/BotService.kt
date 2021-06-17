package com.adjarabet.bot

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.adjarabet.basemodule.Constants
import kotlin.random.Random

class BotService : Service() {

    private val binder  = LocalBinder()

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    fun userMover(move : String) : String{

        val randomWordLengthRange:Int = (1..5).random()
        val randomNumberInRange = (1..100).random()

        val userMoveWordCount = if(move.isNotEmpty()) move.split(" ").size else 0

        val botWordSequence = if(move == Constants.BOT_STARTS_THE_MATCH)
            generateRandomWord(randomWordLengthRange)
        else
            "$move ${generateRandomWord(randomWordLengthRange)}"

        val botMove = if(randomNumberInRange <=3 || userMoveWordCount > 100)
            Constants.TOO_MUCH_FOR_ME
        else botWordSequence

        return botMove
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