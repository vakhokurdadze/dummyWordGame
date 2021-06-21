package com.adjarabet.user.presentation

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.adjarabet.user.R
import kotlinx.android.synthetic.main.word_sequence_toast.view.*

fun Fragment.showCustomToast(text: String){
    val inflater = requireContext().inflater
    val customWordSequenceToast = inflater.inflate(R.layout.word_sequence_toast, null)
    val toast = Toast(context)
    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -580)
    toast.duration = Toast.LENGTH_SHORT
    toast.view = customWordSequenceToast
    toast.show()
    customWordSequenceToast.toastText.text = text
}

val Context.inflater
    get() = LayoutInflater.from(this)