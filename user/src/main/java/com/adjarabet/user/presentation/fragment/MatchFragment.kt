package com.adjarabet.user.presentation.fragment

import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.adjarabet.basemodule.Constants
import com.adjarabet.basemodule.SnackbarProvider
import com.adjarabet.user.*
import com.adjarabet.user.dagger.DaggerMatchFragmentComponent
import com.adjarabet.user.model.LostReason
import com.adjarabet.user.model.MatchResult
import com.adjarabet.user.presentation.MoveRecyclerAdapter
import com.adjarabet.user.presentation.viewmodel.MatchViewModel
import kotlinx.android.synthetic.main.fragment_match.view.*
import kotlinx.android.synthetic.main.match_result_dialog.view.*
import kotlinx.android.synthetic.main.word_sequence_toast.view.*
import kotlinx.coroutines.*
import ru.terrakok.cicerone.Router
import javax.inject.Inject


class MatchFragment : BaseFragment() {

    private lateinit var matchView: View
    private lateinit var viewInflater: LayoutInflater
    private lateinit var exitMatchDialog: AlertDialog
    private lateinit var matchResultDialog: AlertDialog
    private var botService: Messenger? = null
    private var bound: Boolean = false
    private val messenger = Messenger(IncomingHandler(this::botHasPlayedCallBack))

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

        DaggerMatchFragmentComponent.factory().create().inject(this)


        viewInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        toolBarConfig()
        startBotService()

        matchViewModel.lastMove.observe(viewLifecycleOwner, {
            val adapter = MoveRecyclerAdapter(it.lastMoveBy, it.lastMove)
            matchView.matchInfoRecycler.adapter = adapter

            if (matchView.matchInfoRecycler.layoutManager == null) {

                val layoutManager = GridLayoutManager(context, 2)

                layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        val type = adapter.getItemViewType(position)
                        return if (type == MoveRecyclerAdapter.HEADER_VIEW) 2 else 1
                    }
                }
                matchView.matchInfoRecycler.layoutManager = layoutManager
            }
        })

        matchViewModel.userMove.observe(viewLifecycleOwner, Observer {

            val userMoveBundle = Bundle().apply {
                putString(Constants.USER_MOVE_BUNDLE, it)
            }
            val userMove = Message.obtain(
                null, Constants.MESSAGE_USER_MOVE, 0, 0, userMoveBundle
            )
            userMove.replyTo = messenger
            botService?.send(userMove)
            botsTurnView()
        })

        matchViewModel.messagePopUp.observe(viewLifecycleOwner, Observer {

            SnackbarProvider.newSnackBar(
                requireContext(),
                resources.getString(it.titleResourceId),
                resources.getString(it.messageResourceId),
                matchView.matchFragmentRoot,
                150
            ).show()
        })

        matchViewModel.matchResult.observe(viewLifecycleOwner, Observer {
            matchResultDialogInit(it)
            matchResultDialog.show()
        })

        matchViewModel.toastPopUp.observe(viewLifecycleOwner, Observer {
            showCustomToast(it.index, it.word)
        })


        matchView.play.setOnClickListener {
            val wordSequenceInput = matchView.wordSequenceInput.text.toString()
            matchViewModel.play(wordSequenceInput)
        }

        return matchView
    }

    private val botServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            botService = Messenger(service)
            bound = true
            val whoStartsBundle = arguments?.getString(Constants.WHO_STARTS)
            matchViewModel.initMatch(whoStartsBundle)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            botService = null
            bound = false
        }
    }

    private fun toolBarConfig() {

        val toolbar: Toolbar = matchView.matchToolBar
        val supportActionBar = (requireActivity() as AppCompatActivity)

        supportActionBar.apply {
            this.setSupportActionBar(toolbar)
            this.supportActionBar?.setDisplayShowTitleEnabled(true)
            this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            this.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        exitMatchDialogInit()
        if (::exitMatchDialog.isInitialized) {
            if (!exitMatchDialog.isShowing) {
                exitMatchDialog.show()
            } else {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun exitMatchDialogInit() {

        val exitMatchDialogView = viewInflater.inflate(
            R.layout.match_exit_dialog,
            null
        )

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
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                endBotService()
                parentFragmentManager.popBackStack()
            }
            return@setOnKeyListener false
        }

    }

    private fun matchHasEnded() {
        endBotService()
        userTurnView()
        matchViewModel.clearExpectedWordSequence()
    }

    private fun matchResultDialogInit(matchResult: MatchResult) {

        matchHasEnded()
        val matchResultDialogView = viewInflater.inflate(
            R.layout.match_result_dialog,
            null
        )

        val dialogBuilder = AlertDialog.Builder(context).setView(matchResultDialogView)

        this.matchResultDialog = dialogBuilder.create()

        val menu = matchResultDialogView.findViewById<TextView>(R.id.menu)
        val rematch = matchResultDialogView.findViewById<TextView>(R.id.rematch)

        if (matchResult is MatchResult.UserLost) {
            matchResultDialogView.matchResultHeader.text =
                resources.getString(com.adjarabet.basemodule.R.string.botWon)

            matchResultDialogView.matchResultDescription.text = when (matchResult.reason) {
                LostReason.IMPROPER_WORD_NUMBER -> {
                    "${resources.getString(R.string.lostReason)} ${resources.getString(R.string.no_matching)}.\n\nYour words : ${matchResult.userWordSequence}\nExpected words : ${matchResult.properWordSequence} {additional word}\n"
                }
                LostReason.NO_MATCHING -> {
                    "${resources.getString(R.string.lostReason)} ${resources.getString(R.string.no_matching)}.\n\nYour words : ${matchResult.userWordSequence}\nExpected words : ${matchResult.properWordSequence} {additional word}\n"

                }
                else -> {
                    "${resources.getString(R.string.lostReason)} ${resources.getString(R.string.duplicate_words)}.\n\nYour words : ${matchResult.userWordSequence}\nExpected words : ${matchResult.properWordSequence} {additional word}\n"
                }
            }
        } else {
            matchResultDialogView.matchResultDescription.text =
                "${resources.getString(R.string.too_much_for_bot)}"
            matchResultDialogView.matchResultHeader.text =
                resources.getString(com.adjarabet.basemodule.R.string.userWon)
        }
        matchResultDialog.setCanceledOnTouchOutside(false)


        rematch.setOnClickListener {
            matchResultDialog.dismiss()
            matchView.wordSequenceInput.text?.clear()
            startBotService()
        }
        menu.setOnClickListener {
            matchResultDialog.dismiss()
            parentFragmentManager.popBackStack()
        }

        matchResultDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        matchResultDialog.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                endBotService()
                parentFragmentManager.popBackStack()
            }
            return@setOnKeyListener false
        }
    }

    private fun botsTurnView() {

        matchView.botThinkingLoader.visibility = View.VISIBLE
        matchView.botTurnIndicator.visibility = View.VISIBLE
        matchView.userTurnIndicator.visibility = View.INVISIBLE
        matchView.play.isEnabled = false
        matchView.wordSequenceInput.text?.clear()
    }

    private fun userTurnView() {
        matchView.botThinkingLoader.visibility = View.INVISIBLE
        matchView.botTurnIndicator.visibility = View.INVISIBLE
        matchView.userTurnIndicator.visibility = View.VISIBLE
        matchView.play.isEnabled = true
    }

    private fun endBotService() {
        if (bound) {
            context?.unbindService(botServiceConnection)
            bound = false
        }
    }

    private fun startBotService() {

        Intent().also { intent ->
            intent.setClassName(
                Constants.BOT_SERVICE_PACKAGE_NAME,
                Constants.BOT_SERVICE_CLASS_NAME
            )
            context?.bindService(intent, botServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        endBotService()
    }

    private fun botHasPlayedCallBack(move: String) {
        userTurnView()
        matchViewModel.botHasPlayed(move)
    }

    private fun showCustomToast(index: Int, word: String) {
        if (::viewInflater.isInitialized && context != null) {
            val customWordSequenceToast = viewInflater.inflate(R.layout.word_sequence_toast, null)
            val toast = Toast(context)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -580)
            toast.duration = Toast.LENGTH_SHORT
            toast.view = customWordSequenceToast
            toast.show()
            customWordSequenceToast.toastText.text = "$index. $word"
        }
    }

    class IncomingHandler(private val botHasPlayedCallBack: (String) -> Unit) :
        Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_BOT_MOVE -> {
                    val botMove = (msg.obj as Bundle).getString(Constants.BOT_MOVE_BUNDLE) ?: ""
                    botHasPlayedCallBack(botMove)
                }
                else -> super.handleMessage(msg)
            }
        }
    }

}