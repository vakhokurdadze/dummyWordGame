package com.adjarabet.user.presentation.fragment

import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.adjarabet.basemodule.Constants
import com.adjarabet.basemodule.SnackbarProvider
import com.adjarabet.user.*
import com.adjarabet.user.dagger.DaggerMatchFragmentComponent
import com.adjarabet.user.model.MatchResult
import com.adjarabet.user.presentation.CustomDialogProvider
import com.adjarabet.user.presentation.MoveRecyclerAdapter
import com.adjarabet.user.presentation.showCustomToast
import com.adjarabet.user.presentation.viewmodel.MatchViewModel
import kotlinx.android.synthetic.main.fragment_match.view.*
import kotlinx.android.synthetic.main.word_sequence_toast.view.*
import ru.terrakok.cicerone.Router
import javax.inject.Inject


class MatchFragment : BaseFragment() {

    private lateinit var matchView: View
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
            showCustomToast("${it.index}. ${it.word}")
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
        exitMatchDialog = CustomDialogProvider.createExitMatchDialog(
            requireContext(),
            this::exitMatch,
        )
        exitMatchDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun exitMatch() {
        endBotService()
        parentFragmentManager.popBackStack()
    }

    private fun matchHasEnded() {
        endBotService()
        userTurnView()
        matchViewModel.clearExpectedWordSequence()
    }

    private fun matchResultDialogInit(matchResult: MatchResult) {
        matchHasEnded()

        this.matchResultDialog = CustomDialogProvider.createMatchResultDialog(
            requireContext(), matchResult, this::rematch, this::goToMenu, this::goBack
        )
        matchResultDialog.setCanceledOnTouchOutside(false)
        matchResultDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }

    private fun rematch() {
        matchResultDialog.dismiss()
        matchView.wordSequenceInput.text?.clear()
        startBotService()
    }

    private fun goToMenu() {
        matchResultDialog.dismiss()
        parentFragmentManager.popBackStack()
    }

    private fun goBack() {
        endBotService()
        parentFragmentManager.popBackStack()
    }

    private fun botsTurnView() {
        matchView.apply {
            botThinkingLoader.visibility = View.VISIBLE
            botTurnIndicator.visibility = View.VISIBLE
            userTurnIndicator.visibility = View.INVISIBLE
            play.isEnabled = false
            wordSequenceInput.text?.clear()
        }
    }

    private fun userTurnView() {
        matchView.apply {
            botThinkingLoader.visibility = View.INVISIBLE
            botTurnIndicator.visibility = View.INVISIBLE
            userTurnIndicator.visibility = View.VISIBLE
            play.isEnabled = true
        }
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

    class IncomingHandler(private val botHasPlayedCallBack: (String) -> Unit) :
        Handler(Looper.getMainLooper()) {

        //receiving moves(messages) played by bot
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