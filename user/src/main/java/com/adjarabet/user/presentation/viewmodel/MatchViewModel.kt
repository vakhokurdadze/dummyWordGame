package com.adjarabet.user.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adjarabet.user.framework.Interactors
import com.adjarabet.user.model.LastMove

class MatchViewModel(val interactors:Interactors) : ViewModel() {

    val lastMove = MutableLiveData<LastMove>()
}