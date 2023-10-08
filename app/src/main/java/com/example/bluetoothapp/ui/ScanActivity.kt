package com.example.bluetoothapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothapp.adapters.ScanAdapter
import com.example.bluetoothapp.classes.Device
import com.example.bluetoothapp.databinding.ActivityScanBinding
import com.example.bluetoothapp.services.BluetoothClientManager
import com.example.bluetoothapp.utils.isAndroidVersion12OrHigher
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.io.*
import java.util.*

class ScanActivity: AppCompatActivity() {
    private lateinit var deviceAdapter: ScanAdapter
    private val listDevices = mutableListOf<Device>() //Init data listView
    private val listBluetoothDevice = mutableListOf<BluetoothDevice>()
    private lateinit var binding: ActivityScanBinding

    //Bluetooth
    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var isScanning = false //flag scanning
    private var isRegistered = false //flag register Broadcast Change of discover status
    private var isRegistered2 = false //flag register Broadcast Discover

    private lateinit var btnScan: Button
    private lateinit var btnStop: Button
    private lateinit var loadingIcon: CircularProgressIndicator
    private lateinit var listDevice: RecyclerView
    private lateinit var noDeviceTemp: LinearLayout

    //Listener change to stop scan when finish discovery
    private val discoveryFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == intent?.action) {
                loadingIcon.isIndeterminate = false
                loadingIcon.visibility = View.INVISIBLE
                val newItemPosition = listDevices.size
                deviceAdapter.notifyItemInserted(newItemPosition)

                isScanning = false
                btnScan.isEnabled = true
                btnStop.isEnabled = false

                //Set color for Button
                btnStop.setBackgroundColor(Color.WHITE)
                btnStop.setTextColor(Color.parseColor("#34C759"))
                btnScan.setBackgroundColor(Color.parseColor("#34C759"))
                btnScan.setTextColor(Color.WHITE)

                //Set template
                if(listDevices.size == 0){
                    listDevice.visibility = View.GONE
                    noDeviceTemp.visibility = View.VISIBLE
                }
            }
        }
    }

    //Listener discover new bluetooth devices
    private val discoveryStartFindDevices = object : BroadcastReceiver(){
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                val device: BluetoothDevice? = intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (device != null && device.name != null && !listBluetoothDevice.contains(device)) {
                    listDevices.add(Device(device.name, device.address))
                    listBluetoothDevice.add(device)
                    val newItemPosition = listDevices.size
                    deviceAdapter.notifyItemInserted(newItemPosition)
                }
            }
        }
    }

    @SuppressLint("HardwareIds")
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Bound View
        bindViews()

        //Handle button actions
        setButtonsAction()

        //Init and set actions for adapter
        setUpAdapter()
    }

    //Bound Views
    private fun bindViews(){
        listDevice = binding.scanListDevices
        listDevice.layoutManager = LinearLayoutManager(this)
        loadingIcon = binding.scanLoading
        btnScan = binding.scanBtnStart
        btnStop = binding.scanBtnStop
        noDeviceTemp = binding.scanNoDevices
    }

    //Handle buttons action
    private fun setButtonsAction(){
        binding.scanBtnBack.setOnClickListener {
            finish()
        }

        btnScan.setOnClickListener {
            startScan()
        }

        btnStop.setOnClickListener {
            stopScan()
        }
    }

    //Handler adapter
    private fun setUpAdapter(){
        deviceAdapter = ScanAdapter(listDevices)
        listDevice.adapter = deviceAdapter
        deviceAdapter.setOnItemClickListener { view ->
            val position = listDevice.getChildAdapterPosition(view)
            if (position != RecyclerView.NO_POSITION) {
                val selectedDevice = listBluetoothDevice[position]
                BluetoothClientManager.initialize(selectedDevice.address, this, bluetoothAdapter)
                BluetoothClientManager.sendInfo(this)
                BluetoothClientManager.sendImage(this)
            }
        }
    }

    //Handle buttons color
    private fun updateButtonState(isScanning: Boolean) {
        val scanEnabled = !isScanning
        val stopEnabled = isScanning

        val colorEnabled = Color.parseColor("#34C759")
        val colorDisabled = Color.WHITE

        btnScan.isEnabled = scanEnabled
        btnStop.isEnabled = stopEnabled

        btnScan.setBackgroundColor(if (scanEnabled) colorEnabled else colorDisabled)
        btnScan.setTextColor(if (scanEnabled) colorDisabled else colorEnabled)
        btnStop.setBackgroundColor(if (stopEnabled) colorEnabled else colorDisabled)
        btnStop.setTextColor(if (stopEnabled) colorDisabled else colorEnabled)
    }

    //Handle UI scanning
    private fun updateUIScan(isScanning: Boolean){
        if(isScanning){
            listDevice.visibility = View.VISIBLE
            noDeviceTemp.visibility = View.GONE
            loadingIcon.visibility = View.VISIBLE
            loadingIcon.isIndeterminate = true
        } else
        {
            loadingIcon.isIndeterminate = false
            loadingIcon.visibility = View.INVISIBLE
        }
    }

    //Discovery bluetooth devices
    @SuppressLint("MissingPermission")
    private fun startScan() {
        if (isAndroidVersion12OrHigher() && checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        //Set status ui for circle progress
        updateUIScan(true)

        //Start finding bluetooth devices
        bluetoothAdapter.startDiscovery()

        //Register 2 broadcasts
        val discoveryFinishedIntentFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(discoveryFinishedReceiver, discoveryFinishedIntentFilter)
        isRegistered = true

        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoveryStartFindDevices, intentFilter)
        isRegistered2 = true

        //end added
        isScanning = true
        updateButtonState(true)
    }

    //Stop discovering
    @SuppressLint("MissingPermission")
    private fun stopScan() {
        if (isAndroidVersion12OrHigher() && checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        updateUIScan(false)

        //Cancel finding
        bluetoothAdapter.cancelDiscovery()

        if(isRegistered2) //Unregister broadcast find
        {
            unregisterReceiver(discoveryStartFindDevices)
        }

        //Update list devices
        val newItemPosition = listDevices.size
        deviceAdapter.notifyItemInserted(newItemPosition)

        isScanning = false
        updateButtonState(false)

        //Set template after found nothing
        if(listDevices.size == 0){
            listDevice.visibility = View.GONE
            noDeviceTemp.visibility = View.VISIBLE
        }
    }

    //Unregister when destroy activity
    override fun onDestroy() {
        super.onDestroy()
        if(isRegistered){
            unregisterReceiver(discoveryFinishedReceiver)
        }
        if (isScanning) {
            stopScan()
        }
    }
}