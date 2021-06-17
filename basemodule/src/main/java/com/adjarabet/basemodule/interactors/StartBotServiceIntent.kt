package com.adjarabet.basemodule.interactors

import com.adjarabet.basemodule.data.repo.BotServiceRepository
import javax.inject.Inject

class StartBotServiceIntent @Inject constructor(private val botServiceRepo: BotServiceRepository) {
    operator fun invoke() = botServiceRepo.startBotServiceIntent()
}