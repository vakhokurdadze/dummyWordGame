package com.adjarabet.user.presentation.viewmodel.match

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adjarabet.user.framework.Interactors
import com.adjarabet.user.model.LastMove
import javax.inject.Inject


class MatchViewModel @Inject constructor(val interactors:Interactors) : ViewModel() {

    val lastMove = MutableLiveData<LastMove>()
}