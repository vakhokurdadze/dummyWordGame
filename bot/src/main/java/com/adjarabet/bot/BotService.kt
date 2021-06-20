package com.adjarabet.bot

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import com.adjarabet.basemodule.Constants
import kotlinx.coroutines.*
import kotlin.random.Random

class BotService : Service() {


     private var messenger = Messenger(IncomingHandler(this::userHasPlayedCallBack))

    override fun onBind(p0: Intent?): IBinder {
        return messenger.binder
    }

     class IncomingHandler(
         private val userHasPlayedCallBack:(move :String,messenger:Messenger) -> Unit
     ) : Handler(){

        override fun handleMessage(msg: Message) {
            when(msg.what){
                Constants.MESSAGE_USER_MOVE -> {
                    val userMove = (msg.obj as Bundle).getString(Constants.USER_MOVE_BUNDLE) ?: ""
                    userHasPlayedCallBack(userMove,msg.replyTo)
                }
                else -> super.handleMessage(msg)
            }
        }


    }

    private fun userHasPlayedCallBack(move:String,messenger: Messenger){

        CoroutineScope(Dispatchers.IO).launch {
            delay(500)
            withContext(Dispatchers.Main){
                val botMove = obtainBotMove(move)

                val botMoveBundle = Bundle().apply {
                    putString(Constants.BOT_MOVE_BUNDLE,botMove)
                }
                val botMoveMessage = Message.obtain(
                    null,Constants.MESSAGE_BOT_MOVE,0,0,botMoveBundle,
                )
                messenger.send(botMoveMessage)
            }
        }
    }


    fun obtainBotMove(userCurrentMove : String) : String{

        val randomWordLengthRange:Int = (1..5).random()
        val randomNumberInRange = (1..100).random()

        val userMoveWordCount = if(userCurrentMove.isNotEmpty()) userCurrentMove.split(" ").size else 0

        val botWordSequence = if(userCurrentMove == Constants.BOT_STARTS_THE_MATCH)
            generateRandomWord(randomWordLengthRange)
        else
            "$userCurrentMove ${generateRandomWord(randomWordLengthRange)}"

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