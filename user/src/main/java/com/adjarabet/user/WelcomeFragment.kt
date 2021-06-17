package com.adjarabet.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.adjarabet.basemodule.Constants
import com.adjarabet.basemodule.SnackbarProvider
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

            val whoStartsId = welcomeView.whoStartsRadioButtonGroup.checkedRadioButtonId
            if (whoStartsId == -1) {

                SnackbarProvider.newSnackBar(
                    requireContext(),
                    resources.getString(R.string.whoStartsNotSelectedErrorHeader),
                    resources.getString(R.string.whoStartsNotSelectedErrorDesc),
                    welcomeView.welcomeFragmentRoot,
                    150
                ).show()
                return@setOnClickListener
            } else {
                val whoStartsSelected = welcomeView.findViewById<RadioButton>(
                    whoStartsId
                )
                val whoStarts = if(whoStartsSelected.id == R.id.botStartsRadioButton)
                    Player.BOT.name else Player.USER.name

                Intent().also { intent ->
                    intent.action = Constants.ACTION_START_BOT_SERVICE
                    activity?.sendBroadcast(intent)
                }
                router.navigateTo(MatchScreen(whoStarts))
            }

        }
        return welcomeView
    }

}