package com.adjarabet.user.framework

import android.content.Intent
import com.adjarabet.basemodule.Constants
import com.adjarabet.basemodule.data.datasource.BotServiceDataSource

class BotServiceDataSourceImpl : BotServiceDataSource {
    override fun startBotServiceIntent(action:String): Intent =

        Intent().also { intent ->
            intent.action = action
        }

    override fun endBotServiceIntent(action:String): Intent =
        Intent().also { intent ->
            intent.action = action
        }

}