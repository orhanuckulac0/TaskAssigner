package com.example.taskassigner.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityMembersBinding
import com.example.taskassigner.models.Board
import com.example.taskassigner.utils.Constants

class MembersActivity : AppCompatActivity() {
    private var binding: ActivityMembersBinding? = null
    private lateinit var mBoarDetails: Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            if (Build.VERSION.SDK_INT >= 33) {
                mBoarDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL, Board::class.java)!!
            }else {
                mBoarDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
            }

        }
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