package com.example.bluetoothapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bluetoothapp.databinding.ActivityMainBinding
import com.example.bluetoothapp.ui.RegisterActivity
import com.example.bluetoothapp.utils.isAndroidVersion12OrHigher


class MainActivity : AppCompatActivity() {
    //Properties
    private val delaySeconds: Long = 2000 //Time for animation completed
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            navigateToRegister()
        }else{
            finishAffinity()
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

    //Change to register screen
    private fun navigateToRegister(){
        Handler().postDelayed({
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }, delaySeconds)
    }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Hide address bar
        supportActionBar?.hide()

        //Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("InfoUser", Context.MODE_PRIVATE)

        //Splash logo animation
        val slideAnimation = AnimationUtils.loadAnimation(this, R.xml.slide)
        binding.splashLogo.startAnimation(slideAnimation)

        //Init bluetooth
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        if (isAndroidVersion12OrHigher()) {
            requestMultiplePermissions.launch(arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    //Clear temporarily data
    override fun onDestroy() {
        super.onDestroy()
        //Clear sharePreferences store
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}