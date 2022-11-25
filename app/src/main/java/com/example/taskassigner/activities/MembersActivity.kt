package com.example.taskassigner.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityMembersBinding

class MembersActivity : AppCompatActivity() {
    private var binding: ActivityMembersBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarMembersActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_arrow_24dp)
            actionBar.title = resources.getString(R.string.members)

            binding?.toolbarMembersActivity?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}