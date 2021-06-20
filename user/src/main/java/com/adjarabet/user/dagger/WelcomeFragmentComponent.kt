package com.adjarabet.user.dagger;


import com.adjarabet.user.presentation.fragment.WelcomeFragment
import dagger.Component;
import ru.terrakok.cicerone.Router;

@Component(
        modules = [RouterModule::class]
)
interface WelcomeFragmentComponent {

        fun inject(welcomeFragment: WelcomeFragment)

        @Component.Factory
        interface Factory {
                fun create(): WelcomeFragmentComponent
        }
}
