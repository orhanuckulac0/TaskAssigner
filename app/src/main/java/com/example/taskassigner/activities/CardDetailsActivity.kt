package com.example.taskassigner.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityCardDetailsBinding
import com.example.taskassigner.models.Board
import com.example.taskassigner.utils.Constants

class CardDetailsActivity : AppCompatActivity() {
    private var binding: ActivityCardDetailsBinding? = null
    private lateinit var mBoardDetails: Board
    private var mTaskListPosition = -1
    private var mCardPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        getIntentData()
        setupActionBar()
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarCardDetailsActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_arrow_24dp)
            actionBar.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name

            binding?.toolbarCardDetailsActivity?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun getIntentData(){
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            if (Build.VERSION.SDK_INT >= 33) {
                mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL, Board::class.java)!!
            } else {
                mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
            }
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding != null){
            binding = null
        }
    }
}