package com.example.bluetoothapp.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.bluetoothapp.classes.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothapp.adapters.MessageAdapter
import com.example.bluetoothapp.classes.DataManager
import com.example.bluetoothapp.databinding.ActivityMessageBinding
import com.example.bluetoothapp.services.BluetoothClientManager
import com.example.bluetoothapp.services.BluetoothServerManager

class MessageActivity: AppCompatActivity() {
    private lateinit var messageAdapter: MessageAdapter
    private var listMessage = mutableListOf<Message>()
    private lateinit var binding: ActivityMessageBinding

    //Bluetooth
    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    //Handle counter exit
    private var numbExit: Int = 0

    private fun startServer() {
        BluetoothServerManager.initialize(this@MessageActivity, bluetoothAdapter)
        BluetoothServerManager.startListening(this@MessageActivity)
    }

    @SuppressLint("CommitTransaction", "HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startServer()

        //Set Adapter
        messageAdapter = MessageAdapter(listMessage)

        //Handle Adapter
        setUpAdapter()

        //Open Scan Activity
        binding.messageBtnAdd.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }
    }

    //Init and set actions for adapter
    private fun setUpAdapter(){
        binding.messageList.layoutManager = LinearLayoutManager(this)
        binding.messageList.adapter = messageAdapter
        messageAdapter.setOnItemClickListener { view ->
            val position = binding.messageList.getChildAdapterPosition(view)
            if (position != RecyclerView.NO_POSITION) {
                val selectedDevice = listMessage[position]
                BluetoothClientManager.initialize(selectedDevice.address, this, bluetoothAdapter)
                BluetoothClientManager.sendInfo(this)
                BluetoothClientManager.sendImage(this)
            }
        }
    }

    //Handle data when back from BoxChat screen
    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
    override fun onResume() {
        super.onResume()
        if(!BluetoothServerManager.getServerStatus())
        {
            startServer()
        }

        //Clear to update total new messages status
        listMessage.clear()

        //Get users and messages from storage
        for(user in DataManager.getInstance().getUsers())
        {
            if(user.listMessage.isNotEmpty())
            {
                val latestMessage = user.listMessage[user.listMessage.size - 1].content
                val latestMessageTime = user.listMessage[user.listMessage.size - 1].time
                listMessage.add(Message(user.image, user.name, user.address, latestMessage, latestMessageTime))
            }
        }

        //Update data
        messageAdapter.notifyDataSetChanged()
    }

    //Handle press 2 times to exit app
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        numbExit++
        if(numbExit == 1)
            Toast.makeText(this, "Enter again to exit app", Toast.LENGTH_LONG).show()
        else
            finishAffinity()
    }
}