package com.adjarabet.user.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.adjarabet.user.framework.Interactors
import javax.inject.Inject

class WelcomeViewModel @Inject constructor(val interactors: Interactors) : ViewModel()
