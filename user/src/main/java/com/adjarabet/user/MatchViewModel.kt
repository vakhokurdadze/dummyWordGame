package com.adjarabet.user

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MatchViewModel : ViewModel() {

    val lastMove = MutableLiveData<LastMove>()
}