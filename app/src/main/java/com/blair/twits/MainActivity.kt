package com.blair.twits

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blair.twits.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var theButton = binding.firstText
        theButton.text = "Bye Hello"
    }
}