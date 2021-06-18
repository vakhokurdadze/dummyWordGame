package com.adjarabet.basemodule.data.datasource

import android.content.Intent

interface BotServiceDataSource {
    fun startBotServiceIntent(action:String):Intent
    fun endBotServiceIntent(action:String):Intent
}