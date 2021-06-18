package com.adjarabet.basemodule.interactors

import com.adjarabet.basemodule.data.repo.BotServiceRepository
import javax.inject.Inject

class EndBotServiceIntent @Inject constructor(private val botServiceRepo: BotServiceRepository) {
     operator fun invoke(action:String) = botServiceRepo.endBotServiceIntent(action)
}