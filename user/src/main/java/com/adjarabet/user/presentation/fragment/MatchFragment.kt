package com.adjarabet.user.presentation.fragment

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
import com.adjarabet.user.*
import com.adjarabet.user.model.LastMove
import com.adjarabet.user.model.LostReason
import com.adjarabet.user.model.MatchResult
import com.adjarabet.user.model.Player
import com.adjarabet.user.presentation.MoveRecyclerAdapter
import com.adjarabet.user.dagger.DaggerMatchFragmentComponent
import com.adjarabet.user.presentation.viewmodel.MatchViewModel
import kotlinx.android.synthetic.main.fragment_match.view.*
import kotlinx.android.synthetic.main.match_result_dialog.view.*
import kotlinx.android.synthetic.main.word_sequence_toast.view.*
import kotlinx.coroutines.*
import ru.terrakok.cicerone.Router
import javax.inject.Inject


class MatchFragment : BaseFragment() {

    private lateinit var matchView: View
    private lateinit var userBroadcastReceiver: BotActionBroadcastReceiver
    private lateinit var viewInflater: LayoutInflater
    private lateinit var exitMatchDialog:AlertDialog
    private lateinit var matchResultDialog:AlertDialog
    private lateinit var currentWordSequence:String

    @Inject
    lateinit var matchViewModel: MatchViewModel

    @Inject
    lateinit var router: Router

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        matchView = inflater.inflate(R.layout.fragment_match, container, false)
        currentWordSequence = ""


        DaggerMatchFragmentComponent.factory().create().inject(this)


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


        matchViewModel.lastMove.observe(viewLifecycleOwner, {
            val adapter = MoveRecyclerAdapter(it.lastMoveBy,it.lastMove)
            matchView.matchInfoRecycler.adapter = adapter

            if(matchView.matchInfoRecycler.layoutManager == null){

                val layoutManager = GridLayoutManager(context, 2)

                layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        val type = adapter.getItemViewType(position)
                        return if (type == MoveRecyclerAdapter.HEADER_VIEW
                        ) 2 else 1
                    }

                }
                matchView.matchInfoRecycler.layoutManager = layoutManager
            }
        })

        initMatch()




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
                matchViewModel.lastMove.value = LastMove(Player.USER,inputWordList.last())

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
                matchViewModel.lastMove.value = LastMove(Player.USER,inputWordList.last())
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
                    currentWordSequence, wordSequenceInput, LostReason.DUPLICATE_WORD
                )
                matchResultDialogInit(matchResult)
                matchResultDialog.show()
            } else if(!isProperWordNumber){
                val matchResult = MatchResult.UserLost(
                    currentWordSequence, wordSequenceInput, LostReason.IMPROPER_WORD_NUMBER
                )
                matchResultDialogInit(matchResult)
                matchResultDialog.show()

            } else{
                val matchResult = MatchResult.UserLost(
                    currentWordSequence, wordSequenceInput, LostReason.NO_MATCHING
                )
                matchResultDialogInit(matchResult)
                matchResultDialog.show()
            }
        }

        return matchView
    }


    private fun initMatch(){
        val whoStartsBundle = arguments?.getString(Constants.WHO_STARTS)
        val whoStarts = if(whoStartsBundle != null && whoStartsBundle.isNotEmpty())
            Player.valueOf(whoStartsBundle)
        else Player.USER

        if(whoStarts == Player.BOT){
            matchViewModel.lastMove.value = LastMove(Player.BOT,"")
            userMove(Constants.BOT_STARTS_THE_MATCH)
        }else{
            matchViewModel.lastMove.value = LastMove(Player.USER,"")
        }
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
        val rematch = matchResultDialogView.findViewById<TextView>(R.id.rematch)

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


        rematch.setOnClickListener {
            matchResultDialog.dismiss()
            matchView.wordSequenceInput.text?.clear()

            startBotService()
            initMatch()
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
                        matchViewModel.lastMove.value = LastMove(Player.BOT,words.last())
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
        matchView.play.isEnabled = false
        matchView.wordSequenceInput.text?.clear()
    }
    private fun userTurnView(){
        matchView.botThinkingLoader.visibility = View.INVISIBLE
        matchView.botTurnIndicator.visibility = View.INVISIBLE
        matchView.userTurnIndicator.visibility = View.VISIBLE
        matchView.play.isEnabled = true
    }

    private fun endBotService(){
        val endBotServiceIntent = matchViewModel.interactors.endBotServiceIntent()
        activity?.sendBroadcast(endBotServiceIntent)
    }
    private fun startBotService(){
        val startBotServiceIntent = matchViewModel.interactors.startBotServiceIntent()
        activity?.sendBroadcast(startBotServiceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(userBroadcastReceiver)
    }

}