package com.adjarabet.user.framework

import com.adjarabet.basemodule.interactors.EndBotServiceIntent
import com.adjarabet.basemodule.interactors.StartBotServiceIntent

data class Interactors(
    val startBotServiceIntent: StartBotServiceIntent,
    val endBotServiceIntent: EndBotServiceIntent,
)