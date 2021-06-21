package com.adjarabet.user.presentation

import android.app.AlertDialog
import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.TextView
import com.adjarabet.user.R
import com.adjarabet.user.model.LostReason
import com.adjarabet.user.model.MatchResult
import kotlinx.android.synthetic.main.fragment_match.view.*
import kotlinx.android.synthetic.main.match_result_dialog.view.*

object CustomDialogProvider {


    fun createExitMatchDialog(
        context: Context,
        exitMatchCallBack: () -> Unit,
    ): AlertDialog {
        val exitMatchDialogView = LayoutInflater.from(context).inflate(
            R.layout.match_exit_dialog,
            null
        )

        val exitMatchDialog = AlertDialog.Builder(context).setView(exitMatchDialogView).create()

        val exitMatch = exitMatchDialogView.findViewById<TextView>(R.id.positive)
        val keepPlaying = exitMatchDialogView.findViewById<TextView>(R.id.negative)

        keepPlaying.setOnClickListener {
            exitMatchDialog.dismiss()
        }
        exitMatch.setOnClickListener {
            exitMatchDialog.dismiss()
            exitMatchCallBack()
        }

        exitMatchDialog.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                exitMatchCallBack()
            }
            return@setOnKeyListener false
        }

        return exitMatchDialog
    }


    fun createMatchResultDialog(
        context: Context,
        matchResult: MatchResult,
        startRematch: () -> Unit,
        goToMenu: () -> Unit,
        goBack: () -> Unit,
    ): AlertDialog {
        val matchResultDialogView = LayoutInflater.from(context).inflate(
            R.layout.match_result_dialog,
            null
        )

        val dialogBuilder = AlertDialog.Builder(context).setView(matchResultDialogView)

        val menu = matchResultDialogView.findViewById<TextView>(R.id.menu)
        val rematch = matchResultDialogView.findViewById<TextView>(R.id.rematch)

        if (matchResult is MatchResult.UserLost) {
            matchResultDialogView.matchResultHeader.text =
                context.resources.getString(com.adjarabet.basemodule.R.string.botWon)

            matchResultDialogView.matchResultDescription.text = when (matchResult.reason) {
                LostReason.IMPROPER_WORD_NUMBER -> {
                    "${context.resources.getString(R.string.lostReason)} ${
                        context.resources.getString(
                            R.string.no_matching
                        )
                    }.\n\nYour words : ${matchResult.userWordSequence}\nExpected words : ${matchResult.properWordSequence} {additional word}\n"
                }
                LostReason.NO_MATCHING -> {
                    "${context.resources.getString(R.string.lostReason)} ${
                        context.resources.getString(
                            R.string.no_matching
                        )
                    }.\n\nYour words : ${matchResult.userWordSequence}\nExpected words : ${matchResult.properWordSequence} {additional word}\n"
                }
                else -> {
                    "${context.resources.getString(R.string.lostReason)} ${
                        context.resources.getString(
                            R.string.duplicate_words
                        )
                    }.\n\nYour words : ${matchResult.userWordSequence}\nExpected words : ${matchResult.properWordSequence} {additional word}\n"
                }
            }
        } else {
            matchResultDialogView.matchResultDescription.text =
                "${context.resources.getString(R.string.too_much_for_bot)}"
            matchResultDialogView.matchResultHeader.text =
                context.resources.getString(com.adjarabet.basemodule.R.string.userWon)
        }

        rematch.setOnClickListener { startRematch() }
        menu.setOnClickListener { goToMenu() }

        dialogBuilder.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                goBack()
            }

            return@setOnKeyListener false
        }

        return dialogBuilder.create()
    }
}