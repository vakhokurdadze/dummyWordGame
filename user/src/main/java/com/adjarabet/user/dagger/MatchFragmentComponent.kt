package com.adjarabet.user.dagger

import com.adjarabet.user.presentation.fragment.MatchFragment
import dagger.Component

@Component(
    modules = [RouterModule::class]
)
interface MatchFragmentComponent {

    fun inject(matchFragment: MatchFragment)

    @Component.Factory
    interface Factory {
        fun create(): MatchFragmentComponent
    }
}