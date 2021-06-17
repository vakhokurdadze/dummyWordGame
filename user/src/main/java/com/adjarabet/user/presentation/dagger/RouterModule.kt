package com.adjarabet.user.presentation.dagger

import com.adjarabet.basemodule.data.datasource.BotServiceDataSource
import com.adjarabet.user.WordGameApplication
import com.adjarabet.user.framework.BotServiceDataSourceImpl
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.Router


@Module
class RouterModule {

    @Provides
    fun provideBotDataSourceImpl(): Router {
        return WordGameApplication.INSTANCE.cicerone.router
    }
}