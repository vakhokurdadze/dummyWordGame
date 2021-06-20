package com.adjarabet.user.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adjarabet.basemodule.Constants
import com.adjarabet.user.R
import com.adjarabet.user.model.*
import com.adjarabet.user.presentation.SingleLiveEvent
import javax.inject.Inject


class MatchViewModel @Inject constructor() : ViewModel() {

    val _lastMove = MutableLiveData<LastMove>()
    val lastMove : LiveData<LastMove> get() = _lastMove

    val _expectedWordSequence = SingleLiveEvent<String>()

    val _matchResult = SingleLiveEvent<MatchResult>()
    val matchResult : LiveData<MatchResult> get() = _matchResult

    val _userMove = SingleLiveEvent<String>()
    val userMove : LiveData<String> get() = _userMove

    val _messagePopUp = SingleLiveEvent<SnackBarMessage>()
    val messagePopUp : LiveData<SnackBarMessage> get() = _messagePopUp

    val _toastPopUp = SingleLiveEvent<BotMoveToast>()
    val toastPopUp : LiveData<BotMoveToast> get() = _toastPopUp

    init {
        _expectedWordSequence.value = ""
    }


    fun clearExpectedWordSequence(){
        _expectedWordSequence.value = ""
    }

    fun play(wordSequenceInput:String){

        val currentWordSequence = _expectedWordSequence.value ?: ""

        val inputWordList = if (wordSequenceInput.isNotEmpty())
            wordSequenceInput.split(" ")
        else mutableListOf()
        val currentSequenceWordList = if (currentWordSequence.isNotEmpty())
            currentWordSequence.split(" ")
        else mutableListOf()

        val inputHasNoDuplicate = inputWordList.size == inputWordList.distinct().size
        val isProperWordNumber = (currentSequenceWordList.size + 1) == inputWordList.size

        if (currentWordSequence.isEmpty() &&
            inputWordList.size == 1
        ) {
            _userMove.value = wordSequenceInput
            _lastMove.value = LastMove(Player.USER, inputWordList.last())

            _expectedWordSequence.value = wordSequenceInput
            return
        } else if (currentWordSequence.isEmpty() && inputWordList.size > 1) {
            _messagePopUp.value = SnackBarMessage(
                R.string.error,
                com.adjarabet.basemodule.R.string.improperWordNumber
            )
        }

        if (wordSequenceInput.isNotEmpty() && inputHasNoDuplicate
            && inputWordList.containsAll(currentSequenceWordList)
            && isProperWordNumber
        ) {

            _userMove.value = wordSequenceInput
            _lastMove.value = LastMove(Player.USER, inputWordList.last())
            _expectedWordSequence.value = wordSequenceInput

        } else if (wordSequenceInput.isEmpty()) {

            _messagePopUp.value = SnackBarMessage(
                R.string.error,
                R.string.emptyInputError
            )

            return
        } else if (!inputHasNoDuplicate) {

            val matchResult = MatchResult.UserLost(
                currentWordSequence, wordSequenceInput, LostReason.DUPLICATE_WORD
            )

            _matchResult.value = matchResult

        } else if (!isProperWordNumber) {
            val matchResult = MatchResult.UserLost(
                currentWordSequence, wordSequenceInput, LostReason.IMPROPER_WORD_NUMBER
            )
            _matchResult.value = matchResult


        } else {
            val matchResult = MatchResult.UserLost(
                currentWordSequence, wordSequenceInput, LostReason.NO_MATCHING
            )
            _matchResult.value = matchResult

        }
    }


     fun initMatch(whoStartsBundle:String?) {

        val whoStarts = if (whoStartsBundle != null && whoStartsBundle.isNotEmpty())
            Player.valueOf(whoStartsBundle)
        else Player.USER

        if (whoStarts == Player.BOT) {
            _lastMove.value = LastMove(Player.BOT, "")
            _userMove.value = Constants.BOT_STARTS_THE_MATCH
        } else {
            _lastMove.value = LastMove(Player.USER, "")
        }
    }


    fun botHasPlayed(move:String){

       _expectedWordSequence.value = move

        if (move == Constants.TOO_MUCH_FOR_ME) {
            _matchResult.value = MatchResult.UserWon
        } else {
            val words = if (move.isNotEmpty())
                move.split(" ")
            else mutableListOf()

            words.forEachIndexed { index, s ->
                _toastPopUp.value = BotMoveToast(index + 1, s)
                if (index == words.size - 1)
                    _lastMove.value = LastMove(Player.BOT,words.last())
            }
        }
    }

}