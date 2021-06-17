package com.adjarabet.basemodule.data.datasource

import android.content.Intent

interface BotServiceDataSource {
    fun startBotServiceIntent():Intent
    fun endBotServiceIntent():Intent
}