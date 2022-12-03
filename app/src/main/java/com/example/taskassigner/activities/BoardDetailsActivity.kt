package com.example.taskassigner.activities

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityBoardDetailsBinding
import com.example.taskassigner.dialogs.LabelColorListDialog
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.Board
import com.example.taskassigner.models.User
import com.example.taskassigner.utils.Constants
import com.google.common.io.Files.getFileExtension
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
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

    private var mSelectedImageFileUri: Uri? = null
    private var mBoardImageURL: String = ""

    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            // check the result, if It's okay and if the result data is not empty,
            // then get the location of the data, URI, and assign it as background image
            if (result.resultCode == RESULT_OK && result.data != null){
                val contentURI = result.data?.data!!
                mSelectedImageFileUri = contentURI

                try {
                    Glide
                        .with(this)
                        .load(mSelectedImageFileUri)  // load requires Uri
                        .centerCrop()
                        .placeholder(R.drawable.ic_board_place_holder)
                        .into(binding!!.ivUpdateBoardImage)

                }catch (e: IOException){
                    e.printStackTrace()
                }
            }
        }

    private val requestGalleryPermission: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                permission ->
            if (permission) {
                // start an intent to access Media in the phone
                val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGalleryLauncher.launch(pickIntent)
            }else{
                showRationaleDialogForGallery()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // get the intent and make a callback to get the creator user of this board
        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            if (Build.VERSION.SDK_INT >= 33) {
                mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL, Board::class.java)!!
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getSingleUserData(this, mBoardDetails.createdByID)

            }else {
                mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getSingleUserData(this, mBoardDetails.createdByID)
            }
        }

        if (mBoardDetails.labelColor != ""){
            mSelectedColor = mBoardDetails.labelColor
            setColor()
        }

        if (mBoardDetails.dueDate != "" || mBoardDetails.dueDate.isNotEmpty()){
            mSelectedDueDate = mBoardDetails.dueDate
        }else{
            mSelectedDueDate = resources.getString(R.string.select_due_date)
        }

        setupActionBar()

        binding?.ivUpdateBoardImage?.setOnClickListener {
            requestStoragePermission()
        }

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
            showProgressDialog(resources.getString(R.string.please_wait))
            if (mSelectedImageFileUri != null) {
                uploadBoardImage()
            }else{
                updateBoard()
            }
        }
    }

    private fun requestStoragePermission(){
        // check if gallery permission is not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Pass any permission you want while launching
            requestGalleryPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        }else{
            // if it is granted
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            openGalleryLauncher.launch(pickIntent)
        }
    }

    // upload the board image uploaded by user to firebase db and call updateBoard func
    private fun uploadBoardImage(){
        if (mSelectedImageFileUri != null){

            // store image to firebase storage
            val sRef : StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE"+System.currentTimeMillis() + "." + getFileExtension(
                    mSelectedImageFileUri.toString())
            )
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                    taskSnapshot ->
                Log.i("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri->  // actual link of the image
                    Log.i("Downloadable Image URI", uri.toString())
                    mBoardImageURL = uri.toString()

                    updateBoard()
                }
            }.addOnFailureListener{
                    exception->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()

                cancelProgressDialog()
            }
        }
    }

    private fun updateBoard() {
        // create hashmap to use for update DB
        val boardHashMap = HashMap<String, Any>()

        val boardName = binding?.etBoardNameDetails?.text.toString()
        val labelColor = mSelectedColor
        val dueDate = mSelectedDueDate


        if (boardName.isNotEmpty() && boardName != mBoardDetails.name){
            boardHashMap[Constants.NAME] = boardName
        }

        if (mBoardImageURL.isNotEmpty() && mBoardImageURL != mBoardDetails.image){
            boardHashMap[Constants.IMAGE] = mBoardImageURL
        }

        boardHashMap[Constants.LABEL_COLOR] = labelColor
        boardHashMap[Constants.DUE_DATE] = dueDate

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

    private fun showRationaleDialogForGallery(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Task Assigner")
            .setMessage("TaskAssigner needs Files & Media permission to upload an image from your storage." +
                    " Would you like to go to settings and give permission?")
            .setNegativeButton("Cancel"){
                    dialog, _-> dialog.dismiss()
            }
            .setPositiveButton("Yes"){
                // redirect user to app settings to allow permission for gallery
                    _, _-> startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            })
            }
        builder.create().show()
    }

    override fun getUserDataSuccess(user: User) {
        // assign lateinit with the callback result User object
        mCreatedByUser = user

        // populate the UI with the data of user and board
        // this below for creator user image
        Glide
            .with(this)
            .load(mCreatedByUser.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding!!.ivCreatedByUserImage)

        // this for board image
        Glide
            .with(this)
            .load(mBoardDetails.image)
            .centerCrop()
            .placeholder(R.drawable.ic_board_place_holder)
            .into(binding!!.ivUpdateBoardImage)

        binding?.etBoardNameDetails?.setText(mBoardDetails.name)
        binding?.tvSelectDueDate?.text = mSelectedDueDate
        binding?.tvMemberName?.text = mCreatedByUser.name
        binding?.tvMemberEmail?.text = mCreatedByUser.email

        cancelProgressDialog()
    }

    override fun getUserDataFailed(error: String?) {
        Log.i("Error getUserDataFailed", error.toString())
        cancelProgressDialog()
    }

    override fun updateBoardSuccess() {
        Toast.makeText(this,"Board has been updated successfully!", Toast.LENGTH_LONG).show()
        cancelProgressDialog()
        finish()
    }

    override fun updateBoardFailed(error: String?) {
        Log.i("Error updateBoardFailed", error.toString())
        cancelProgressDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding != null){
            binding = null
        }
    }
}