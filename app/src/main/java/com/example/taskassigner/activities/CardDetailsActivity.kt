package com.example.taskassigner.activities

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityCardDetailsBinding
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.Board
import com.example.taskassigner.models.Card
import com.example.taskassigner.models.Task
import com.example.taskassigner.utils.Constants
import java.io.IOException

class CardDetailsActivity : BaseActivity(), FirestoreClass.AddUpdateTaskListCallback {
    private var binding: ActivityCardDetailsBinding? = null
    private lateinit var mBoardDetails: Board
    private lateinit var mCurrentCard: Card
    private var mTaskListPosition = -1
    private var mCardPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        getIntentData()
        setupActionBar()

        // populate edit text with current card name
        if (mBoardDetails != null) {
            binding?.etCardNameDetails?.setText(mCurrentCard.name)
        }

        binding?.btnUpdateCardDetails?.setOnClickListener {
            if (binding?.etCardNameDetails?.text.toString().isNotEmpty()){
                showProgressDialog(resources.getString(R.string.please_wait))
                updateCardDetails()
            }else{
                Toast.makeText(
                    this,
                    "Please enter a card name.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateCardDetails(){
        // this below can be improved
        val updatedCard = Card(
            binding?.etCardNameDetails?.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo
        )

        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardPosition)

        cardsList.add(updatedCard)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        // get rid of he Add Card btn which is a member of taskList
        taskList.removeAt(taskList.size - 1)

        taskList[mTaskListPosition].cards = cardsList

        // make the callback for updating the TaskList and finish activity
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    private fun deleteCard(){
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        // get rid of he Add Card btn which is a member of taskList
        taskList.removeAt(taskList.size - 1)

        taskList[mTaskListPosition].cards = cardsList

        // start the callback for updating tasklist with removed card
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
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

        try {
            mCurrentCard = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition]
        }catch (e: IOException){
            e.printStackTrace()
            Log.e("Error occurred", e.toString())
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarCardDetailsActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_arrow_24dp)
            actionBar.title = mCurrentCard.name

            binding?.toolbarCardDetailsActivity?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun alertDialogForDeleteCard(cardTitle: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete '$cardTitle'?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setCancelable(false)

        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            deleteCard()
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
            .create()
            .show()
    }

    // setup the menu item for deleting card
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.actionDeleteCard ->{
                alertDialogForDeleteCard(mCurrentCard.name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun addUpdateTaskListSuccess() {
        Toast.makeText(this, "Success!", Toast.LENGTH_LONG).show()
        cancelProgressDialog()
        finish()

    }

    override fun addUpdateTaskListFailed(error: String?) {
        Toast.makeText(this, "An error has occurred, please try again.", Toast.LENGTH_LONG).show()
        cancelProgressDialog()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding != null){
            binding = null
        }
    }
}