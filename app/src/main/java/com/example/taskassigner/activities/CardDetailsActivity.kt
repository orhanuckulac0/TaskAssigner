package com.example.taskassigner.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityCardDetailsBinding

class CardDetailsActivity : AppCompatActivity() {
    private var binding: ActivityCardDetailsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarCardDetailsActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_arrow_24dp)
            actionBar.title = "Card Name"

            binding?.toolbarCardDetailsActivity?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (binding != null){
            binding = null
        }
    }
}