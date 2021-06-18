package com.adjarabet.user.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.adjarabet.basemodule.Constants
import com.adjarabet.basemodule.SnackbarProvider
import com.adjarabet.user.presentation.router.MatchScreen
import com.adjarabet.user.model.Player
import com.adjarabet.user.R
import com.adjarabet.user.dagger.DaggerWelcomeFragmentComponent
import com.adjarabet.user.presentation.viewmodel.WelcomeViewModel
import kotlinx.android.synthetic.main.fragment_match.view.*
import kotlinx.android.synthetic.main.fragment_welcome.view.*
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class WelcomeFragment : Fragment(){


    private lateinit var welcomeView: View

    @Inject
    lateinit var router : Router

    @Inject
    lateinit var welcomeViewModel: WelcomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        welcomeView = inflater.inflate(R.layout.fragment_welcome,container,false)


        DaggerWelcomeFragmentComponent.factory().create().inject(this)


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

                val startBotServiceIntent =
                    welcomeViewModel.interactors.startBotServiceIntent(
                        Constants.ACTION_START_BOT_SERVICE
                    )

                activity?.sendBroadcast(startBotServiceIntent)

                router.navigateTo(MatchScreen(whoStarts))
            }

        }
        return welcomeView
    }

}