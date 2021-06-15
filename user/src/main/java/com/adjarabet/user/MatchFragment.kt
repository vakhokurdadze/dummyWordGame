package com.adjarabet.user

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.adjarabet.basemodule.Constants
import com.adjarabet.basemodule.SnackbarProvider
import kotlinx.android.synthetic.main.fragment_match.view.*
import kotlinx.android.synthetic.main.word_sequence_toast.view.*
import kotlinx.coroutines.*
import ru.terrakok.cicerone.Router


class MatchFragment : BaseFragment() {

    private lateinit var matchView: View
    private lateinit var router: Router
    private lateinit var userBroadcastReceiver: BotActionBroadcastReceiver
    private lateinit var viewInflater: LayoutInflater
    private lateinit var exitMatchDialog:AlertDialog
    private lateinit var currentWordSequence:String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        matchView = inflater.inflate(R.layout.fragment_match, container, false)
        currentWordSequence = ""

        viewInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val toolbar: Toolbar = matchView.matchToolBar

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        userBroadcastReceiver = BotActionBroadcastReceiver()
        val filter = IntentFilter(Constants.ACTION_BOT_MOVE)
        activity?.registerReceiver(userBroadcastReceiver, filter)

        router = (activity?.application as WordGameApplication).cicerone.router


        matchView.play.setOnClickListener {


            val wordSequenceInput = matchView.wordSequenceInput.text.toString()
            val inputWordList = if(wordSequenceInput.isNotEmpty())
                wordSequenceInput.split(" ")
            else mutableListOf()
            val currentSequenceWordList = if(currentWordSequence.isNotEmpty())
                currentWordSequence.split(" ")
            else mutableListOf()

            val inputHasNoDuplicate = inputWordList.size == inputWordList.distinct().size
            val isProperWordNumber = (currentSequenceWordList.size + 1) == inputWordList.size

            if(currentWordSequence.isEmpty() &&
                inputWordList.size == 1){
                userMove(wordSequenceInput)

                currentWordSequence = wordSequenceInput
                return@setOnClickListener
            }else if (currentWordSequence.isEmpty() && inputWordList.size > 1){
                SnackbarProvider.newSnackBar(
                    requireContext(),
                    resources.getString(R.string.error),
                    resources.getString(com.adjarabet.basemodule.R.string.improperWordNumber),
                    matchView.matchFragmentRoot,
                    150
                ).show()
            }

            if(wordSequenceInput.isNotEmpty() && inputHasNoDuplicate
                && wordSequenceInput.contains(currentWordSequence)
                && isProperWordNumber){

                userMove(wordSequenceInput)
                currentWordSequence = wordSequenceInput

            } else if(wordSequenceInput.isEmpty()){
                SnackbarProvider.newSnackBar(
                    requireContext(),
                    resources.getString(R.string.error),
                    resources.getString(R.string.emptyInputError),
                    matchView.matchFragmentRoot,
                    150
                ).show()

                return@setOnClickListener
            }else if(!wordSequenceInput.contains(currentWordSequence)){

            }
            else if(!inputHasNoDuplicate){

            }else{

            }

        }

        return matchView
    }

    override fun onBackPressed() {
        quitTestDriveDialogInit()
        if(::exitMatchDialog.isInitialized){
            if(!exitMatchDialog.isShowing){
                exitMatchDialog.show()
            }else{
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun quitTestDriveDialogInit(){

        val exitMatchDialogView = viewInflater.inflate(
           R.layout.match_exit_dialog,
            null)

        val dialogBuilder = AlertDialog.Builder(context).setView(exitMatchDialogView)

        this.exitMatchDialog = dialogBuilder.create()

        val exitMatch = exitMatchDialogView.findViewById<TextView>(R.id.positive)
        val keepPlaying = exitMatchDialogView.findViewById<TextView>(R.id.negative)


        keepPlaying.setOnClickListener {
            exitMatchDialog.dismiss()
        }
        exitMatch.setOnClickListener {
            exitMatchDialog.dismiss()
            endBotService()
            parentFragmentManager.popBackStack()
        }

        exitMatchDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        exitMatchDialog.setOnKeyListener { v, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP){
                endBotService()
                parentFragmentManager.popBackStack()
            }
            return@setOnKeyListener false
        }

    }

    private fun userMove(wordSequenceInput:String){
        Intent().also { intent ->
            intent.action = Constants.ACTION_USER_MOVE
            intent.putExtra(Constants.MOVE, wordSequenceInput)
            activity?.sendBroadcast(intent)

            botsTurnView()
        }
    }

    inner class BotActionBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            userTurnView()

            val currentWordSequence = intent?.getStringExtra(Constants.MOVE) ?: ""

            this@MatchFragment.currentWordSequence = currentWordSequence

            val words = if(currentWordSequence.isNotEmpty())
                currentWordSequence.split(" ")
            else mutableListOf()

            words.forEachIndexed { index, s ->
                showCustomToast(index + 1,s)
            }
        }


        private fun showCustomToast(index:Int,word:String){
            if(::viewInflater.isInitialized && context != null){
                val customWordSequenceToast = viewInflater.inflate(R.layout.word_sequence_toast,null)
                val toast = Toast(context)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -300)
                toast.duration = Toast.LENGTH_SHORT
                toast.view = customWordSequenceToast
                toast.show()
                customWordSequenceToast.toastText.text = "$index. $word"
            }
        }
    }

    private fun botsTurnView(){

        matchView.botThinkingLoader.visibility = View.VISIBLE
        matchView.botTurnIndicator.visibility = View.VISIBLE
        matchView.userTurnIndicator.visibility = View.INVISIBLE
        matchView.wordSequenceInput.text?.clear()
    }
    private fun userTurnView(){
        matchView.botThinkingLoader.visibility = View.INVISIBLE
        matchView.botTurnIndicator.visibility = View.INVISIBLE
        matchView.userTurnIndicator.visibility = View.VISIBLE
    }

    private fun endBotService(){
        Intent().also { intent ->
            intent.action = Constants.ACTION_STOP_BOT_SERVICE
            activity?.sendBroadcast(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(userBroadcastReceiver)
    }


    override fun onPause() {
        super.onPause()
    }
}