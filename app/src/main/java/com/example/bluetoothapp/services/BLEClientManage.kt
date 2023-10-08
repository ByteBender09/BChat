package com.example.bluetoothapp.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import com.example.bluetoothapp.ui.BoxChatActivity
import com.example.bluetoothapp.ui.ScanActivity
import com.example.bluetoothapp.classes.DataManager
import com.example.bluetoothapp.classes.User
import com.example.bluetoothapp.interfaces.ChatsListener
import com.example.bluetoothapp.interfaces.StatusListener
import com.example.bluetoothapp.utils.isAndroidVersion12OrHigher
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*

object BluetoothClientManager {
    private var uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private lateinit var sharedPreferences: SharedPreferences
    private var chatMessageListener: ChatsListener? = null
    private var chatStatusListener: StatusListener?= null
    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var inputStream: InputStream
    private val buffer = ByteArray(1024)
    private var isListening = true //flag listen with client role

    //Send Avatar and Navigate to BoxChat
    private var nameUser = ""
    private var addressUser = ""
    private lateinit var navigateIntent: Intent
    private var byte64String = ""

    //Interact with BoxChat Activity
    fun setChatMessageListener(listener: ChatsListener) {
        chatMessageListener = listener
    }

    private fun handleMessageReceived(time: String, message: String) {
        chatMessageListener?.onMessageReceived(time, message)
    }

    fun setChatStatusListener(listener: StatusListener) {
        chatStatusListener = listener
    }

    private fun handleStatusReceived(status: Boolean) {
        chatStatusListener?.onStatusReceived(status)
    }

    fun setSocket(socket: BluetoothSocket){
        bluetoothSocket = socket
    }

//    private var loadingScreen: LoadingScreen? = null
//    private lateinit var loadingDialogJob: Job

    @SuppressLint("ResourceType", "SuspiciousIndentation", "MissingPermission")
    fun initialize(deviceAddress: String, context: Context, bluetoothAdapter: BluetoothAdapter) {
//        loadingScreen = LoadingScreen(context as Activity)
//        CoroutineScope(Dispatchers.Main).launch {
//            loadingScreen!!.startLoadingDialog()
//        }
        val remoteDevice: BluetoothDevice? = bluetoothAdapter.getRemoteDevice(deviceAddress)
        try {
            if (isAndroidVersion12OrHigher() && !hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            } else if (!isAndroidVersion12OrHigher() && !hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                throw SecurityException("No ACCESS_FINE_LOCATION permission")
            }

            //Create socket connecting to Server
            bluetoothSocket = remoteDevice?.createRfcommSocketToServiceRecord(uuid)

            bluetoothSocket?.let { socket ->
//                loadingDialogJob?.cancel()
//                loadingScreen?.dismissDialog()
                try {
                    socket.connect()

                    //Save connect address
                    sharedPreferences = context.getSharedPreferences("InfoUser", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("OtherClient", deviceAddress)
                    editor.apply()

                    //Set address
                    addressUser = deviceAddress

                    //Create User333
                    if(!DataManager.getInstance().isExistUser(deviceAddress))
                    DataManager.getInstance().addUser(User("", deviceAddress, "DEFAULT", emptyArray()))

                    listenMessage(socket, context)
                } catch (e: IOException) {
                    Log.d("CLIENT", "Error Connecting")
                    e.printStackTrace()
                    socket.close()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun sendMessage(time: String, message: String) {
        //Create Json to define type of value
        val jsonMessage = JSONObject()
        jsonMessage.put("time", time)
        jsonMessage.put("message", message)

        //Convert json to string
        val jsonString = jsonMessage.toString()

        //Send to server
        try {
            val outputStream = bluetoothSocket?.outputStream
            val byteArray = jsonString.toByteArray()
            outputStream?.write(byteArray)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //Send name to another client
    fun sendInfo(context: Context) {
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences = context.getSharedPreferences("InfoUser", Context.MODE_PRIVATE)
        }
        val jsonMessage = JSONObject()
        jsonMessage.put("name", sharedPreferences.getString("name", ""))

        //Convert json to string
        val jsonString = jsonMessage.toString()

        //Send to server
        try {
            val outputStream = bluetoothSocket?.outputStream
            val byteArray = jsonString.toByteArray()
            outputStream?.write(byteArray)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //Send avatar image
    fun sendImage(context: Context) {
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences = context.getSharedPreferences("InfoUser", Context.MODE_PRIVATE)
        }

        val imagePath = sharedPreferences.getString("image", "")
        val cleanedImagePath = if (imagePath?.startsWith("file:/") == true) {
            imagePath.substring("file:/".length)
        } else {
            imagePath
        }
        val imageFile = cleanedImagePath?.let { File(it) }

        val finalBase64: String = if (imageFile != null && imageFile.exists()) {
            val imageByteArray = readImageFile(imageFile)
            android.util.Base64.encodeToString(imageByteArray, android.util.Base64.DEFAULT) + "[IMG]"
        } else {
            "[IMG]"
        }


        try {
            val outputStream = bluetoothSocket?.outputStream
            val byteArray = finalBase64.toByteArray()
            outputStream?.write(byteArray)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun readImageFile(file: File): ByteArray {
        val inputStream = FileInputStream(file)
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var bytesRead: Int

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }

        return outputStream.toByteArray()
    }

    @SuppressLint("SuspiciousIndentation")
    @OptIn(DelicateCoroutinesApi::class)
    fun listenMessage(socket: BluetoothSocket, context: Context) {
        Log.d("CLIENT", "Listening Message")
        inputStream = socket.inputStream
        isListening = true

        GlobalScope.launch(Dispatchers.IO) {
            while (isListening) {
                try {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead != -1) {
                        handleStatusReceived(true)
                        val receivedData = String(buffer, 0, bytesRead)
                        try {
                            if(receivedData.contains("{"))
                            {
                                val jsonMessage = JSONObject(receivedData)
                                //Listen messages
                                if(jsonMessage.has("time") && jsonMessage.has("message")) {
                                    val time = jsonMessage.getString("time")
                                    val message = jsonMessage.getString("message")
                                    handleMessageReceived(time, message)
                                } else {
                                    //Listen for name and address of client
                                    nameUser = jsonMessage.getString("name")
                                    navigateIntent = Intent(context, BoxChatActivity::class.java)
                                    navigateIntent.putExtra("name", nameUser)

                                    if(receivedData.endsWith("[IMG]")){
                                        context.startActivity(navigateIntent)
                                        if(context is ScanActivity)
                                        (context as Activity).finish()
                                    }
                                }
                            }
                            else{
                                //Listen for avatar user
                                byte64String+=receivedData
                                if(receivedData.endsWith("[IMG]") || receivedData == "[IMG]"){
                                    navigateIntent.putExtra("avatar", byte64String.substring(0, byte64String.length - 5))
                                    context.startActivity(navigateIntent)
                                    if(context is ScanActivity)
                                        (context as Activity).finish()
                                }
                            }

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: IOException) {
//                    e.printStackTrace()
                    isListening = false
                    handleStatusReceived(false)
                }
            }
        }
    }

    private fun hasPermission(context: Context, permission: String): Boolean =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    fun close() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
            isListening = false
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
