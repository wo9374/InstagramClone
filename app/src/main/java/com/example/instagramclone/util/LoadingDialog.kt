package com.example.instagramclone.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.example.instagramclone.R

object LoadingDialog {
    private var dialog: Dialog? = null

    val isShowing
        get() = dialog?.isShowing == true

    fun show(context: Context) {
        if (dialog == null) {
            dialog = Dialog(context).apply {
                setContentView(R.layout.dialog_loading)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}