package com.adjarabet.basemodule.data.repo

import android.content.Context
import com.adjarabet.basemodule.data.datasource.BotServiceDataSource

class BotServiceRepository(private val botServiceDataSource: BotServiceDataSource) {

     fun startBotServiceIntent() = botServiceDataSource.startBotServiceIntent()
     fun endBotServiceIntent() = botServiceDataSource.endBotServiceIntent()
}