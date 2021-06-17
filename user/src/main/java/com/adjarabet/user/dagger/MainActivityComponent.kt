package com.adjarabet.user.dagger

import com.adjarabet.user.presentation.MainActivity
import dagger.Component

@Component(
    modules = [RouterModule::class]
)
interface MainActivityComponent {

    fun inject(mainActivity: MainActivity)

    @Component.Factory
    interface Factory {
        fun create(): MainActivityComponent
    }
}