package com.adjarabet.user.framework

import android.content.Intent
import com.adjarabet.basemodule.Constants
import com.adjarabet.basemodule.data.datasource.BotServiceDataSource

class BotServiceDataSourceImpl : BotServiceDataSource {
    override fun startBotServiceIntent(): Intent =

        Intent().also { intent ->
            intent.action = Constants.ACTION_START_BOT_SERVICE
        }

    override fun endBotServiceIntent(): Intent =
        Intent().also { intent ->
            intent.action = Constants.ACTION_STOP_BOT_SERVICE
        }

}