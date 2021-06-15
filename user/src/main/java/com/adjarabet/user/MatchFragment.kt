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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        matchView = inflater.inflate(R.layout.fragment_match, container, false)

        viewInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val toolbar: Toolbar = matchView.matchToolBar

        toolbar.title = resources.getString(R.string.match)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)

        toolbar.setTitleTextColor(resources.getColor(R.color.white))
        (requireActivity() as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.match)
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
            Intent().also { intent ->
                intent.action = Constants.ACTION_USER_MOVE
                intent.putExtra(Constants.MOVE, matchView.wordInput.text.toString())
                activity?.sendBroadcast(intent)
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
            parentFragmentManager.popBackStack()
        }

        exitMatchDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        exitMatchDialog.setOnKeyListener { v, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP){
                parentFragmentManager.popBackStack()
            }
            return@setOnKeyListener false
        }

    }

    inner class BotActionBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val currentWordSequence = intent?.getStringExtra(Constants.MOVE) ?: ""

            val words = currentWordSequence.split(" ")

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

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(userBroadcastReceiver)
    }

    override fun onPause() {
        super.onPause()
    }
}