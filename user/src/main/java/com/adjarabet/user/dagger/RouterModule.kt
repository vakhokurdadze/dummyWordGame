package com.adjarabet.user.dagger

import com.adjarabet.user.WordGameApplication
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