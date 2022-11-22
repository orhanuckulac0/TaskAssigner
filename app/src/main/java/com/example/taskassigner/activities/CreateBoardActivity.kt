package com.example.taskassigner.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityCreateBoardBinding

class CreateBoardActivity : AppCompatActivity() {
    private var binding: ActivityCreateBoardBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarCreateBoardActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_arrow_24dp)
            actionBar.title = resources.getString(R.string.create_board_title)
            binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

}