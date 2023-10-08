package com.example.bluetoothapp.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import com.example.bluetoothapp.R

class LoadingScreen(private val activity: Activity) {
    private lateinit var dialog: AlertDialog

    @SuppressLint("InflateParams")
    fun startLoadingDialog() {
        val builder = AlertDialog.Builder(activity, R.style.full_screen_alert)

        val inflater = activity.layoutInflater
        val dialogView = inflater.inflate(R.layout.activity_loading, null)
        builder.setView(dialogView)
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()
    }

    fun dismissDialog() {
        if (::dialog.isInitialized && dialog.isShowing) {
            Log.d("DS", "DISMISS")
            dialog.dismiss()
        }
    }
}

