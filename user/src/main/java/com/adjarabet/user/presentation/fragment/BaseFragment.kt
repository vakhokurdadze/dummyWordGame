package com.adjarabet.user.presentation.fragment

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    abstract fun onBackPressed()
}