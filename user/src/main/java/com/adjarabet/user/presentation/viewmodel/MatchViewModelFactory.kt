package com.adjarabet.user.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adjarabet.user.framework.Interactors

class MatchViewModelFactory(
 private val interactors:Interactors
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MatchViewModel(interactors) as T
    }
}