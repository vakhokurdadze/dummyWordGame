package com.adjarabet.user.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adjarabet.user.model.LastMove
import javax.inject.Inject


class MatchViewModel @Inject constructor() : ViewModel() {

    val lastMove = MutableLiveData<LastMove>()
}