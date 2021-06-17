package com.adjarabet.basemodule.data.repo

import android.content.Context
import com.adjarabet.basemodule.data.datasource.BotServiceDataSource
import javax.inject.Inject


class BotServiceRepository @Inject constructor(private val botServiceDataSource: BotServiceDataSource) {

     fun startBotServiceIntent() = botServiceDataSource.startBotServiceIntent()
     fun endBotServiceIntent() = botServiceDataSource.endBotServiceIntent()

}