package com.adjarabet.user

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.adjarabet.basemodule.Constants
import kotlinx.android.synthetic.main.fragment_match.view.*
import kotlinx.coroutines.*
import ru.terrakok.cicerone.Router

class MatchFragment : BaseFragment() {

    private lateinit var matchView: View
    private lateinit var router: Router
    private lateinit var userBroadcastReceiver: BotActionBroadcastReceiver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        matchView = inflater.inflate(R.layout.fragment_match, container, false)

        userBroadcastReceiver = BotActionBroadcastReceiver()
        val filter = IntentFilter(Constants.ACTION_BOT_MOVE)
        activity?.registerReceiver(userBroadcastReceiver, filter)

        router = (activity?.application as WordGameApplication).cicerone.router


        matchView.play.setOnClickListener {
            Intent().also { intent ->
                intent.action = Constants.ACTION_USER_MOVE
                intent.putExtra(Constants.MOVE, matchView.wordInput.text.toString())
                activity?.sendBroadcast(intent)
            }
        }

        return matchView
    }

    override fun onBackPressed() {
        parentFragmentManager.popBackStack()
    }

    inner class BotActionBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val currentWordSequence = intent?.getStringExtra(Constants.MOVE) ?: ""
            this@MatchFragment.matchView.userTurnTextIndicator.visibility = View.INVISIBLE
            this@MatchFragment.matchView.opponentTurnTextIndicator.visibility = View.VISIBLE
            this@MatchFragment.matchView.opponentTurnTextIndicator.text = currentWordSequence

            val words = currentWordSequence.split(" ")

            words.forEach {
                Toast.makeText(activity?.applicationContext, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

}