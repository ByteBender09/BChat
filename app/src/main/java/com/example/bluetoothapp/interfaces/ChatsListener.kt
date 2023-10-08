package com.example.bluetoothapp.interfaces

interface ChatsListener {
    fun onMessageReceived(time: String, message: String)
}