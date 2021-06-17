package com.adjarabet.basemodule.interactors

import com.adjarabet.basemodule.data.repo.BotServiceRepository

class StartBotServiceIntent (private val botServiceRepo: BotServiceRepository) {
    operator fun invoke() = botServiceRepo.startBotServiceIntent()
}