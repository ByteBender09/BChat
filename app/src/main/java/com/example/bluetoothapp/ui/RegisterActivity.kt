package com.example.bluetoothapp.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.bluetoothapp.databinding.ActivityRegisterBinding
import com.github.dhaval2404.imagepicker.ImagePicker

@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityRegisterBinding
    private var urlAvatar: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("InfoUser", Context.MODE_PRIVATE)

        //Set up View Elements
        setUpView()

        //Pick and handle Image
        binding.registerBtn.setOnClickListener {
            ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(100)	    //Final image size will be less than 1 MB(Optional)
                .maxResultSize(100, 100)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        binding.registerBtnNext.setOnClickListener {
            if(binding.registerInput.text?.isEmpty() == true)
            {
                binding.registerAlert.isVisible = true //Show alert wrong input
            }
            else{
                saveUserData()
                navigateToMessage()
            }
        }
    }

    private fun setUpView(){
        //Initially, alert is hidden when false -> show
        binding.registerAlert.isVisible = false

        //Not allowed to double line
        binding.registerInput.isSingleLine = true
        binding.registerInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
    }

    //Save name and image user input into user storage
    private fun saveUserData(){
        val editor = sharedPreferences.edit()
        editor.putString("name", binding.registerInput.text.toString())
        editor.putString("image", urlAvatar)
        editor.apply()
    }

    //Navigate to Message Screen (Main Screen)
    private fun navigateToMessage(){
        val intent = Intent(this, MessageActivity::class.java)
        startActivity(intent)
        finish()
    }

    //Set Image when finish choosing
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            urlAvatar = data.data.toString()
            binding.registerAvatar.setImageURI(data.data)
        }
    }
}