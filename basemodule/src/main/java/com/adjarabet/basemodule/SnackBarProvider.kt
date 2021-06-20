package com.adjarabet.basemodule

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

object SnackbarProvider {


    fun newSnackBar(
        context: Context,
        title: String,
        description: String,
        rootLayout: CoordinatorLayout,
        marginFromTop: Int,
        isSuccessMessage: Boolean = false
    ): Snackbar {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val messageView = inflater.inflate(R.layout.message_view, null)

        val snackbar: Snackbar = Snackbar.make(rootLayout, "", Snackbar.LENGTH_SHORT)
        val snackbarLayout: Snackbar.SnackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        val snackBarDefaultText = snackbarLayout.findViewById(R.id.snackbar_text) as TextView
        snackBarDefaultText.visibility = View.INVISIBLE
        val params = snackbarLayout.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.TOP
        params.topMargin = marginFromTop
        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.layoutParams = params
        snackbarLayout.addView(messageView)

        snackbar.view.background = ColorDrawable(Color.TRANSPARENT)

        val errorMessageTitle = messageView.findViewById<TextView>(R.id.messageTitle)
        val errorMessageDesc = messageView.findViewById<TextView>(R.id.messageDesc)
        val closeTextView = messageView.findViewById<TextView>(R.id.close)

        if (isSuccessMessage) {
            val messageTypeIndicator =
                messageView.findViewById<ImageView>(R.id.messageTypeIndicator)
            val messageIcon = messageView.findViewById<ImageView>(R.id.messageIcon)
            messageTypeIndicator.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.colorAccent)
            messageIcon.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.colorAccent)

        }

        errorMessageTitle.text = title
        errorMessageDesc.text = description

        closeTextView.setOnClickListener { snackbar.dismiss() }

        return snackbar
    }

}