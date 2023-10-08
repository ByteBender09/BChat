package com.example.bluetoothapp.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

fun isAndroidVersion12OrHigher(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}