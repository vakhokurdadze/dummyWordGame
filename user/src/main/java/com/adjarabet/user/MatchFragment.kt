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
import androidx.recyclerview.widget.GridLayoutManager
import com.adjarabet.basemodule.Constants
import com.adjarabet.basemodule.SnackbarProvider
import kotlinx.android.synthetic.main.fragment_match.view.*
import kotlinx.android.synthetic.main.match_result_dialog.view.*
import kotlinx.android.synthetic.main.word_sequence_toast.view.*
import kotlinx.coroutines.*
import ru.terrakok.cicerone.Router


class MatchFragment : BaseFragment() {

    private lateinit var matchView: View
    private lateinit var router: Router
    private lateinit var userBroadcastReceiver: BotActionBroadcastReceiver
    private lateinit var viewInflater: LayoutInflater
    private lateinit var exitMatchDialog:AlertDialog
    private lateinit var matchResultDialog:AlertDialog
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

        updateMoveRecycler(Player.USER,"")
        val adapter = matchView.matchInfoRecycler.adapter as MoveRecyclerAdapter
        val layoutManager = GridLayoutManager(context, 2)

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val type = adapter.getItemViewType(position)
                return if (type == MoveRecyclerAdapter.HEADER_VIEW
                ) 2 else 1
            }

        }

        matchView.matchInfoRecycler.layoutManager = layoutManager

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
                updateMoveRecycler(Player.USER,inputWordList.last())

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
                && inputWordList.containsAll(currentSequenceWordList)
                && isProperWordNumber){

                userMove(wordSequenceInput)
                updateMoveRecycler(Player.USER,inputWordList.last())
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
            } else if(!inputHasNoDuplicate){

                val matchResult = MatchResult.UserLost(
                    currentWordSequence,wordSequenceInput,LostReason.DUPLICATE_WORD
                )
                matchResultDialogInit(matchResult)
                matchResultDialog.show()
            } else if(!isProperWordNumber){
                val matchResult = MatchResult.UserLost(
                    currentWordSequence,wordSequenceInput,LostReason.IMPROPER_WORD_NUMBER
                )
                matchResultDialogInit(matchResult)
                matchResultDialog.show()

            } else{
                val matchResult = MatchResult.UserLost(
                    currentWordSequence,wordSequenceInput,LostReason.NO_MATCHING
                )
                matchResultDialogInit(matchResult)
                matchResultDialog.show()
            }
        }

        return matchView
    }

    private fun updateMoveRecycler(lastMoveBy:Player,lastmove:String){

        val adapter = MoveRecyclerAdapter(lastMoveBy,lastmove)

        matchView.matchInfoRecycler.adapter = adapter

    }

    override fun onBackPressed() {
        exitMatchDialogInit()
        if(::exitMatchDialog.isInitialized){
            if(!exitMatchDialog.isShowing){
                exitMatchDialog.show()
            }else{
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun exitMatchDialogInit(){

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
    private fun matchHasEnded(){
        endBotService()
        userTurnView()
        this.currentWordSequence = ""
    }

    private fun matchResultDialogInit(matchResult: MatchResult){

        matchHasEnded()
        val matchResultDialogView = viewInflater.inflate(
            R.layout.match_result_dialog,
            null)

        val dialogBuilder = AlertDialog.Builder(context).setView(matchResultDialogView)

        this.matchResultDialog = dialogBuilder.create()

        val menu = matchResultDialogView.findViewById<TextView>(R.id.menu)
        val newGame = matchResultDialogView.findViewById<TextView>(R.id.newGame)

        if(matchResult is MatchResult.UserLost){
            matchResultDialogView.matchResultHeader.text = resources.getString(com.adjarabet.basemodule.R.string.botWon)

          matchResultDialogView.matchResultDescription.text =  when(matchResult.reason){
                LostReason.IMPROPER_WORD_NUMBER ->{
                    "${resources.getString(R.string.lostReason)} ${resources.getString(R.string.no_matching)}.\n\nYour words : ${matchResult.userWordSequence}\nExpected words : ${matchResult.properWordSequence} {additional word}\n"
                }
                LostReason.NO_MATCHING -> {
                    "${resources.getString(R.string.lostReason)} ${resources.getString(R.string.no_matching)}.\n\nYour words : ${matchResult.userWordSequence}\nExpected words : ${matchResult.properWordSequence} {additional word}\n"

                }
                else ->{
                    "${resources.getString(R.string.lostReason)} ${resources.getString(R.string.duplicate_words)}.\n\nYour words : ${matchResult.userWordSequence}\nExpected words : ${matchResult.properWordSequence} {additional word}\n"
                }
            }
        }else{
            matchResultDialogView.matchResultDescription.text = "${resources.getString(R.string.too_much_for_bot)}"
            matchResultDialogView.matchResultHeader.text = resources.getString(com.adjarabet.basemodule.R.string.userWon)
        }
        matchResultDialog.setCanceledOnTouchOutside(false)


        newGame.setOnClickListener {
            matchResultDialog.dismiss()
            matchView.wordSequenceInput.text?.clear()
            updateMoveRecycler(Player.USER,"")
            startBotService()
        }
        menu.setOnClickListener {
            matchResultDialog.dismiss()
            parentFragmentManager.popBackStack()
        }

        matchResultDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        matchResultDialog.setOnKeyListener { v, keyCode, event ->
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

            if(currentWordSequence == Constants.TOO_MUCH_FOR_ME){
                val matchResult = MatchResult.UserWon
                matchResultDialogInit(matchResult)
                matchResultDialog.show()
            }else{
                val words = if(currentWordSequence.isNotEmpty())
                    currentWordSequence.split(" ")
                else mutableListOf()

                words.forEachIndexed { index, s ->
                    showCustomToast(index + 1,s)
                    if(index == words.size - 1)
                        updateMoveRecycler(Player.BOT,words.last())
                }
            }
        }


        private fun showCustomToast(index:Int,word:String){
            if(::viewInflater.isInitialized && context != null){
                val customWordSequenceToast = viewInflater.inflate(R.layout.word_sequence_toast,null)
                val toast = Toast(context)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -580)
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
    private fun startBotService(){
        Intent().also { intent ->
            intent.action = Constants.ACTION_START_BOT_SERVICE
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