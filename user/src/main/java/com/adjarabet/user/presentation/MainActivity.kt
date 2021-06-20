package com.adjarabet.user.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.adjarabet.basemodule.Constants
import com.adjarabet.user.R
import com.adjarabet.user.WordGameApplication
import com.adjarabet.user.dagger.DaggerMainActivityComponent
import com.adjarabet.user.presentation.fragment.BaseFragment
import com.adjarabet.user.presentation.fragment.MatchFragment
import com.adjarabet.user.presentation.fragment.WelcomeFragment
import com.adjarabet.user.presentation.router.MatchScreen
import com.adjarabet.user.presentation.router.Routes
import com.adjarabet.user.presentation.router.SimpleScreen
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward
import javax.inject.Inject

class MainActivity : AppCompatActivity() {


    @Inject
    lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DaggerMainActivityComponent.factory().create().inject(this)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.baseFragmentContainer)

        if (currentFragment == null)
            navigateToWelcomeFragment()

    }


    private fun navigateToWelcomeFragment() {
        router.navigateTo(SimpleScreen(Routes.WELCOME_SCREEN))
    }


    private val navigator: Navigator? by lazy {
        object : Navigator {
            override fun applyCommands(commands: Array<out Command>?) {
                if (commands?.get(0) is Back) back()
                else if (commands?.get(0) is Forward) forward(commands[0] as Forward)
            }

            private fun back() {
                this@MainActivity.onBackPressed()
            }

            private fun forward(command: Forward) {
                when (command.screen) {
                    is SimpleScreen -> {
                        val currentScreen = command.screen as SimpleScreen

                        when (currentScreen.route) {
                            Routes.WELCOME_SCREEN -> {
                                replaceFragment(WelcomeFragment(), currentScreen.route)
                            }
                        }
                    }
                    is MatchScreen -> {
                        val currentScreen = command.screen as MatchScreen

                        val matchFragment = MatchFragment()
                        val matchFragmentBundle = Bundle().apply {
                            putString(
                                Constants.WHO_STARTS,
                                currentScreen.whoStarts
                            )
                        }
                        matchFragment.arguments = matchFragmentBundle
                        replaceFragment(
                            matchFragment,
                            Routes.WELCOME_SCREEN
                        )
                    }
                }
            }

        }

    }

    private fun <F> replaceFragment(fragment: F, tag: String) where F : Fragment {

        supportFragmentManager.beginTransaction().apply {
            this.replace(R.id.baseFragmentContainer, fragment)
            this.addToBackStack(tag)
            this.setPrimaryNavigationFragment(fragment)
            this.commitAllowingStateLoss()
        }
    }

    override fun onBackPressed() {

        val currentFragment = supportFragmentManager.findFragmentById(R.id.baseFragmentContainer)

        if (currentFragment != null && currentFragment is WelcomeFragment) {
            finish()
        } else if (currentFragment != null) {
            (currentFragment as BaseFragment).onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        (application as WordGameApplication).cicerone.navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        (application as WordGameApplication).cicerone.navigatorHolder.removeNavigator()

    }
}