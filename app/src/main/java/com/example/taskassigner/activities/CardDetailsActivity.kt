package com.example.taskassigner.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.taskassigner.R
import com.example.taskassigner.adapters.CardMemberListItemsAdapter
import com.example.taskassigner.databinding.ActivityCardDetailsBinding
import com.example.taskassigner.dialogs.LabelColorListDialog
import com.example.taskassigner.dialogs.MembersListDialog
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.*
import com.example.taskassigner.utils.Constants
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity(),
    FirestoreClass.AddUpdateTaskListCallback {

    private var binding: ActivityCardDetailsBinding? = null

    private lateinit var mBoardDetails: Board
    private lateinit var mCurrentCard: Card
    private lateinit var mMembersDetailList: ArrayList<User>
    private lateinit var mCurrentUserID: String

    private var mTaskListPosition = -1
    private var mCardPosition = -1
    private var mSelectedColor = ""

    private var mSelectedDueDate = ""
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        getIntentData()
        setupActionBar()

        mCurrentUserID = FirestoreClass().getCurrentUserId()

        // populate edit text with current card name
        if (mBoardDetails != null) {
            val mCurrentCardName = mCurrentCard.name
            binding?.etCardNameDetails?.setText(mCurrentCardName)

            if (mCurrentUserID != mBoardDetails.createdByID){
                binding?.etCardNameDetails?.isFocusable = false
                binding?.etCardDescriptionDetails?.isFocusable = false
            }

            // if color is not selected yet, don't set mSelectedColor
            if (mCurrentCard.labelColor != ""){
                mSelectedColor = mCurrentCard.labelColor
                setColor()
            }

            if (mCurrentCard.description != ""){
                binding?.etCardDescriptionDetails?.setText(mCurrentCard.description)
            }else{
                binding?.etCardDescriptionDetails?.setText(getString(R.string.no_description_available))
            }
        }

        if (mCurrentUserID == mBoardDetails.createdByID){
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

            binding?.tvSelectLabelColor?.setOnClickListener {
                showLabelColorsListDialog()
            }

            binding?.tvSelectMembers?.setOnClickListener{
                membersListDialog()
            }

            dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

            binding?.tvSelectDueDate?.setOnClickListener {
                createDatePicker()
            }
        }else{
            binding?.btnUpdateCardDetails?.visibility = View.GONE
        }

        // get the current card' selected members
        setupSelectedMembersList()
    }

    private fun createDatePicker(){
        DatePickerDialog(this,
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())

        binding?.tvSelectDueDate?.text = sdf.format(cal.time).toString()

        mSelectedDueDate = binding?.tvSelectDueDate?.text.toString()
    }

    // get members list and create custom dialog to show members
    private fun membersListDialog(){
        // get the string ID's of the assigned users
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        // if there are any users in assignedTo
        if (cardAssignedMembersList.size > 0){
            // for member user in mMembersDetailList
            for (i in mMembersDetailList.indices) {
                // for assigned user in cardAssignedMembersList
                for (j in cardAssignedMembersList) {
                    // if member user's id == string ID of the assignedTo user,
                    if (mMembersDetailList[i].id == j) {
                        // set member user's selected value to true because It's the same user
                        mMembersDetailList[i].selected = true
                    }
                }
            }

        }else{
            // set members' selected values to false
            for (i in mMembersDetailList.indices) {
                mMembersDetailList[i].selected = false
            }
        }

        // create MembersListDialog object
        val memberListDialog = object: MembersListDialog(
            this,
            mMembersDetailList,
            resources.getString(R.string.select_members)
        ){
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT){
                    // if the user not in current assignedTo list, add
                    if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(user.id)){
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(user.id)
                    }
                }else{
                    // remove user from current selected users and set its selected value to false
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(user.id)

                    for (i in mMembersDetailList.indices){
                        if (mMembersDetailList[i].id == user.id){
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                // refresh
                setupSelectedMembersList()
            }
        }
        memberListDialog.show()
    }

    private fun setupSelectedMembersList(){
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailList.indices) {
            for (j in cardAssignedMembersList) {
                if (mMembersDetailList[i].id == j) {
                    // create a new model for SelectedMembers model
                    // add it to the selectedMembersList
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )

                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0){
            // this below makes adding member image visible to the UI
            selectedMembersList.add(SelectedMembers(""," "))

            binding?.tvSelectMembers?.visibility = View.GONE
            binding?.rvSelectedMembersList?.visibility = View.VISIBLE
            binding?.rvSelectedMembersList?.layoutManager = GridLayoutManager(this, 6)

            val adapter = CardMemberListItemsAdapter(this, selectedMembersList, true)
            binding?.rvSelectedMembersList?.adapter = adapter

            if (mCurrentUserID == mBoardDetails.createdByID){
                adapter.setOnClickListener(
                    object: CardMemberListItemsAdapter.OnCLickListener{
                        override fun onClick() {
                            membersListDialog()
                        }
                    }
                )
            }
        }else{
            binding?.tvSelectMembers?.visibility = View.VISIBLE
            binding?.rvSelectedMembersList?.visibility = View.GONE
        }
    }

    private fun updateCardDetails(){
        val updatedCard = Card(
            binding?.etCardNameDetails?.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDate,
            binding?.etCardDescriptionDetails?.text.toString()
        )

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = updatedCard
        // this below line is essential
        // this prevents firestore to create empty list on db
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1 )

        // make the callback for updating the TaskList and finish activity
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    private fun deleteCard(){
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
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

        // get the current members of this list, whom can be assigned to the card
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            if (Build.VERSION.SDK_INT >= 33) {
                mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST, User::class.java)!!
                Log.i("Users", "$mMembersDetailList")
            }else {
                mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
                Log.i("Users", "$mMembersDetailList")
            }
        }

        try {
            mCurrentCard = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition]
            mSelectedDueDate = mCurrentCard.dueDate

            if (mSelectedDueDate != ""){
                binding?.tvSelectDueDate?.text = mSelectedDueDate
            }

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

    // return list of color strings
    private fun colorsList(): ArrayList<String>{
        return ArrayList(resources.getStringArray(R.array.label_colors).asList())
    }

    // set color of the background on TaskListActivity for cards
    private fun setColor(){
        binding?.tvSelectLabelColor?.text = ""
        binding?.tvSelectLabelColor?.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    // create an object of the LalColorListDialog class
    // show the custom dialog for label color picking for cards
    private fun showLabelColorsListDialog(){
        val colorsList: ArrayList<String> = colorsList()
        val listDialog = object : LabelColorListDialog(this,
            colorsList,
            resources.getString(R.string.select_color),
            mSelectedColor
        ){
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.create()
        listDialog.show()
    }

    // setup the menu item for deleting card
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        // allow only board creator to delete cards
        if (mCurrentUserID != mBoardDetails.createdByID){
            menu!!.findItem(R.id.actionDeleteCard).isVisible = false
        }
        return super.onPrepareOptionsMenu(menu)
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