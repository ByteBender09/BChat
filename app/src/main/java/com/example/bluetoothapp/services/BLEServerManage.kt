package com.example.bluetoothapp.services

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import com.example.bluetoothapp.classes.DataManager
import com.example.bluetoothapp.classes.User
import com.example.bluetoothapp.utils.isAndroidVersion12OrHigher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

object BluetoothServerManager {
    private var serverSocket: BluetoothServerSocket? = null
    private var uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var isListening = false
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("MissingPermission")
    fun initialize(context: Context, bluetoothAdapter: BluetoothAdapter?) {
        try {
            if (isAndroidVersion12OrHigher() && !hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            } else if (!isAndroidVersion12OrHigher() && !hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                throw SecurityException("No ACCESS_FINE_LOCATION permission")
            }
            serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("ServerBluetooth", uuid)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("HardwareIds")
    @OptIn(DelicateCoroutinesApi::class)
    fun startListening(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            var currentSocketClient: BluetoothSocket?
            isListening = true
            while (isListening) {
                Log.d("SERVER", "Listening")
                currentSocketClient = try {
                    serverSocket?.accept()
                }
                catch (e: Exception) {
                    println(e.stackTrace)
                    isListening = false
                    null
                }
                currentSocketClient?.let { socket ->
                    serverSocket?.close()
                    serverSocket = null
                    isListening = false

                    //Save connect address
                    sharedPreferences = context.getSharedPreferences("InfoUser", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("OtherClient", socket.remoteDevice.address)
                    editor.apply()

                    //Create User
                    if(!DataManager.getInstance().isExistUser(socket.remoteDevice.address))
                        DataManager.getInstance().addUser(User("", socket.remoteDevice.address, "DEFAULT", emptyArray()))

                    BluetoothClientManager.setSocket(socket)
                    BluetoothClientManager.listenMessage(socket, context)
                    BluetoothClientManager.sendInfo(context) //Info first device with server role
                    BluetoothClientManager.sendImage(context)
                }
            }
        }
    }

    fun getServerStatus(): Boolean{
        return isListening
    }

    private fun hasPermission(context: Context, permission: String): Boolean =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}
