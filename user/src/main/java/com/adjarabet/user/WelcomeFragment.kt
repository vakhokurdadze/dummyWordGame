package com.adjarabet.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adjarabet.basemodule.Constants
import kotlinx.android.synthetic.main.fragment_match.view.*
import kotlinx.android.synthetic.main.fragment_welcome.view.*
import ru.terrakok.cicerone.Router

class WelcomeFragment : Fragment(){


    private lateinit var welcomeView: View
    private lateinit var router : Router

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        welcomeView = inflater.inflate(R.layout.fragment_welcome,container,false)

        router = (activity?.application as WordGameApplication).cicerone.router

        welcomeView.start.setOnClickListener {
            Intent().also { intent ->
                intent.action = Constants.ACTION_START_BOT_SERVICE
                activity?.sendBroadcast(intent)
            }
            router.navigateTo(SimpleScreen(Routes.MATCH_SCREEN))
        }
        return welcomeView
    }

}