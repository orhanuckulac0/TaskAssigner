package com.example.taskassigner.activities

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityBoardDetailsBinding
import com.example.taskassigner.dialogs.LabelColorListDialog
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.Board
import com.example.taskassigner.models.User
import com.example.taskassigner.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class BoardDetailsActivity : BaseActivity(),
    FirestoreClass.GetSingleUserDataCallback,
    FirestoreClass.UpdateBoardCallback {
    private var binding: ActivityBoardDetailsBinding? = null

    private var mSelectedColor = ""
    private lateinit var mBoardDetails: Board
    private lateinit var mCreatedByUser: User

    private var mSelectedDueDate = ""
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // get the intent and make a callback to get the creator user of this board
        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            if (Build.VERSION.SDK_INT >= 33) {
                mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL, Board::class.java)!!
                FirestoreClass().getSingleUserData(this, mBoardDetails.createdByID)

            }else {
                mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
                FirestoreClass().getSingleUserData(this, mBoardDetails.createdByID)
            }
        }

        setupActionBar()

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()  // update view when user selects a date
        }


        binding?.tvSelectLabelColor?.setOnClickListener {
            showLabelColorsListDialog()
        }

        binding?.tvSelectDueDate?.setOnClickListener {
            createDatePicker()
        }

        binding?.btnUpdateBoardDetails?.setOnClickListener {
            updateBoard()
        }
    }

    private fun updateBoard() {
        val boardName = binding?.etBoardNameDetails?.text.toString()
        val labelColor = mSelectedColor
        val dueDate = mSelectedDueDate

        val boardHashMap = HashMap<String, Any>()
        boardHashMap[Constants.NAME] = boardName
        boardHashMap["labelColor"] = labelColor
        boardHashMap["dueDate"] = dueDate

        // make the callback for updating board on DB with the hashmap created above
        FirestoreClass().updateBoard(this, boardHashMap, mBoardDetails)
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarBoardDetailsActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_arrow_24dp)
            actionBar.title = mBoardDetails.name

            binding?.toolbarBoardDetailsActivity?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
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

    override fun getUserDataSuccess(user: User) {
        // assign lateinit with the callback result User object
        mCreatedByUser = user

        // populate the UI with the data of user and board
        Glide
            .with(this)
            .load(mCreatedByUser.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding!!.ivCreatedByUserImage)

        binding?.etBoardNameDetails?.setText(mBoardDetails.name)
        binding?.tvMemberName?.text = mCreatedByUser.name
        binding?.tvMemberEmail?.text = mCreatedByUser.email

        if (mBoardDetails.labelColor != ""){
            mSelectedColor = mBoardDetails.labelColor
            setColor()
        }

        if (mBoardDetails.dueDate != ""){
            binding?.tvSelectDueDate?.text = mBoardDetails.dueDate
        }
    }

    override fun getUserDataFailed(error: String?) {
        Log.i("Error : ", error.toString())
    }

    override fun updateBoardSuccess() {
        Toast.makeText(this,"Board has been updated successfully!", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun updateBoardFailed(error: String?) {
        Log.i("Error : ", error.toString())
        Toast.makeText(this,"error!", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding != null){
            binding = null
        }
    }
}