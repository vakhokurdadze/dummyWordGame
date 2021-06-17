package com.adjarabet.user.dagger

import com.adjarabet.basemodule.data.datasource.BotServiceDataSource
import com.adjarabet.user.framework.BotServiceDataSourceImpl
import dagger.Module
import dagger.Provides

@Module
class BotServiceProviderModule {

    @Provides
    fun provideBotDataSourceImpl():BotServiceDataSource {
        return BotServiceDataSourceImpl()
    }

}