package com.adjarabet.user.framework

import com.adjarabet.basemodule.interactors.EndBotServiceIntent
import com.adjarabet.basemodule.interactors.StartBotServiceIntent
import javax.inject.Inject

data class Interactors @Inject constructor(
    val startBotServiceIntent: StartBotServiceIntent,
    val endBotServiceIntent: EndBotServiceIntent,
)