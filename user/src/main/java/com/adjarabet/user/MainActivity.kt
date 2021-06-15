package com.adjarabet.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward

class MainActivity : AppCompatActivity() {


    private lateinit var router : Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        router = (application as WordGameApplication).cicerone.router

        val currentFragment = supportFragmentManager.findFragmentById(R.id.baseFragmentContainer)

        if(currentFragment == null){
            navigateToWelcomeFragment()
        }

    }


    private fun navigateToWelcomeFragment(){
        router.navigateTo(SimpleScreen(Routes.WELCOME_SCREEN))
    }



    private val navigator : Navigator? by lazy {
        object : Navigator{
            override fun applyCommands(commands: Array<out Command>?) {
                if (commands?.get(0) is Back) back()
                else if (commands?.get(0) is Forward) forward(commands[0] as Forward)
            }

            private fun back(){
                this@MainActivity.onBackPressed()
            }

            private fun forward(command:Forward){
                when(command.screen){
                    is SimpleScreen ->{
                        val currentScreen = command.screen as SimpleScreen

                        when(currentScreen.route){
                            Routes.WELCOME_SCREEN -> {
                                replaceFragment(
                                    WelcomeFragment(),
                                    currentScreen.route
                                )
                            }
                            Routes.MATCH_SCREEN -> {
                                replaceFragment(
                                    MatchFragment(),
                                    currentScreen.route
                                )
                            }
                        }
                    }
                }
            }

        }

    }

    private fun <F> replaceFragment(fragment: F, tag: String) where F : Fragment {

        val fragTransaction = supportFragmentManager.beginTransaction()
        fragTransaction.replace(R.id.baseFragmentContainer, fragment)
        fragTransaction.addToBackStack(tag)
        fragTransaction.setPrimaryNavigationFragment(fragment)
        fragTransaction.commitAllowingStateLoss()

    }

    override fun onBackPressed() {

        val currentFragment = supportFragmentManager.findFragmentById(R.id.baseFragmentContainer)

        if(currentFragment != null && currentFragment is WelcomeFragment){
            finish()
        }else if(currentFragment != null){
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