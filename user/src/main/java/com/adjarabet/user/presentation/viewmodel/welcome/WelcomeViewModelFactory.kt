package com.adjarabet.user.presentation.viewmodel.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adjarabet.user.framework.Interactors

class WelcomeViewModelFactory(
    private val interactors: Interactors
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WelcomeViewModel(interactors) as T
    }
}