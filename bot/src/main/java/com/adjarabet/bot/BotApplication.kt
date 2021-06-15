package com.adjarabet.bot

import android.app.Application
import android.content.*
import android.os.IBinder
import android.widget.Toast
import com.adjarabet.basemodule.Constants
import kotlinx.coroutines.*

class BotApplication : Application() {

    private lateinit var userBroadcastReceiver: UserBroadcastReceiver
    private lateinit var botService: BotService
    private var mBound: Boolean = false

    override fun onCreate() {
        super.onCreate()

        userBroadcastReceiver = UserBroadcastReceiver()
        val filter = IntentFilter(Constants.ACTION_USER_MOVE)
        filter.addAction(Constants.ACTION_START_BOT_SERVICE)
        filter.addAction(Constants.ACTION_STOP_BOT_SERVICE)
        registerReceiver(userBroadcastReceiver, filter)
    }

    inner class UserBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val action = intent?.action

            if(action == Constants.ACTION_START_BOT_SERVICE){
                Intent(context, BotService::class.java).also { i ->
                    bindService(i, connection, Context.BIND_AUTO_CREATE)
                }
            }else if(action == Constants.ACTION_STOP_BOT_SERVICE){
                finishBotService()
            }else{
                val inputWord = intent?.getStringExtra(Constants.MOVE)
                if (::botService.isInitialized && inputWord != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(500)
                        withContext(Dispatchers.Main){
                            val userWordSequence = botService.userMover(inputWord)
                            botMoveIntent(context, userWordSequence)
                        }
                    }
                }
            }
        }
    }

    private fun botMoveIntent(context: Context?, userWordSequence: String) {
        Intent().also { i ->
            i.action = Constants.ACTION_BOT_MOVE
            i.putExtra(Constants.MOVE, userWordSequence)
            context?.sendBroadcast(i)
        }
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BotService.LocalBinder
            botService = binder.getBotService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    private fun finishBotService() {
        unbindService(connection)
        mBound = false
    }
}