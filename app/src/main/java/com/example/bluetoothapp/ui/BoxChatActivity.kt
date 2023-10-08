package com.example.bluetoothapp.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothapp.R
import com.example.bluetoothapp.adapters.ChatAdapter
import com.example.bluetoothapp.classes.Chat
import com.example.bluetoothapp.interfaces.ChatsListener
import com.example.bluetoothapp.interfaces.StatusListener
import com.example.bluetoothapp.services.BluetoothClientManager
import com.example.bluetoothapp.classes.DataManager
import com.example.bluetoothapp.classes.User
import com.example.bluetoothapp.databinding.ActivityBoxChatBinding
import java.util.*

class BoxChatActivity : AppCompatActivity(), ChatsListener, StatusListener {
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var nameSender: TextView
    private lateinit var avatarSender: ImageView
    private lateinit var textInput: EditText
    private lateinit var statusTxt: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityBoxChatBinding

    private var addressSender = ""
    private lateinit var user: User
    private lateinit var nameProps: String
    private var listChat = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoxChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Register listener for box chat
        setListeners()

        //Init share preferences
        sharedPreferences = getSharedPreferences("InfoUser", Context.MODE_PRIVATE)

        // Get address device and get user with that address
        addressSender = sharedPreferences.getString("OtherClient", "").toString()
        user = DataManager.getInstance().getUserByAddress(addressSender)!!

        //Bound View
       bindView()

        //Get props from box chat
        nameProps = intent.getStringExtra("name").toString()
        val avatarProps = intent.getStringExtra("avatar")

        //Trim name to solve error much blank space
        nameSender.text = nameProps.trim()

        //Handle show avatar
        showAvatar(avatarProps)

        //Update name in data store
        val name = nameProps.trim()
        DataManager.getInstance().updateNameByAddress(addressSender, name)

        //Init List Chat (With case there have messages with that address before)
        listChat = DataManager.getInstance().getUserMessages(addressSender).toMutableList()
        chatAdapter = ChatAdapter(listChat)
        binding.boxListMessages.adapter = chatAdapter

        //Handle with edit Text
        handleInput()

        //Handle with buttons
        handleButtons()
    }

    //Set actions for message input
    private fun handleInput(){
        //Hide Buttons when focus EditText
        textInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.boxModalLocation.isVisible = false
                binding.boxModalFile.isVisible = false
            } else {
                binding.boxModalLocation.isVisible = true
                binding.boxModalFile.isVisible = true
            }
        }

        //Enter input in virtual keyboard
        textInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    //Set actions for button
    private fun handleButtons(){
        //Handle click send
        binding.boxModalSend.setOnClickListener {
            sendMessage()
        }

        //Handle close activity
        binding.boxBtnBack.setOnClickListener {
            finish()
        }
    }

    //Handle avatar
    private fun showAvatar(avatarProps: String?){
        if (avatarProps != null && avatarProps.isNotEmpty()) {
            if(user.image == "DEFAULT")
            {
                DataManager.getInstance().updateImageByAddress(addressSender, avatarProps)
                val imageBytes = android.util.Base64.decode(avatarProps, android.util.Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                avatarSender.setImageBitmap(decodedImage)
            } else {
                val imageBytes = android.util.Base64.decode(user.image, android.util.Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                avatarSender.setImageBitmap(decodedImage)
            }
        } else {
            avatarSender.setImageResource(R.drawable.ic_account)
        }
    }

    //Register Listener
    private fun setListeners(){
        BluetoothClientManager.setChatMessageListener(this)
        BluetoothClientManager.setChatStatusListener(this)
    }

    //Binding View
    private fun bindView() {
        with(binding) {
            textInput = boxModalInput
            nameSender = boxBannerName
            avatarSender = boxBannerAvatar
            statusTxt = boxBannerStatus
            boxListMessages.layoutManager = LinearLayoutManager(this@BoxChatActivity)
        }
    }

    //Get current time
    private fun getTimeString(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val period = if (hour < 12) "AM" else "PM"
        return String.format("%02d:%02d %s", hour, minute, period) //EX: 15:30 PM
    }

    //Handle chatting
    private fun sendMessage() {
        val timeString = getTimeString()
        val chatItem = Chat(textInput.text.toString(), timeString, 0)
        listChat.add(chatItem)
        DataManager.getInstance().addMessageToUser(addressSender, chatItem)
        BluetoothClientManager.sendMessage(timeString, textInput.text.toString())
        handleAfterInput()
    }

    //Close keyboard and clear input after send message
    private fun handleAfterInput() {
        textInput.text.clear()
        textInput.clearFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(textInput.windowToken, 0)
        }
    }

    //Message from server received
    override fun onMessageReceived(time: String, message: String) {
        runOnUiThread {
            val newItemPosition = listChat.size
            listChat.add(Chat(message, time, 1))
            DataManager.getInstance().addMessageToUser(addressSender, Chat(message, time, 1))
            chatAdapter.notifyItemInserted(newItemPosition)
        }
    }

    //When connection status changes
    @SuppressLint("ResourceAsColor")
    override fun onStatusReceived(status: Boolean) {
        if(!status){
            statusTxt.text = getString(R.string.box_disconnected)
            statusTxt.setTextColor(ContextCompat.getColor(this, R.color.text_disconnected))
        } else{
            statusTxt.text = getString(R.string.box_connected)
            statusTxt.setTextColor(ContextCompat.getColor(this, R.color.text_connected))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothClientManager.close()
    }
}