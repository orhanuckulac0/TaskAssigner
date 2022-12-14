package com.example.taskassigner.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityCreateBoardBinding
import com.example.taskassigner.dialogs.LabelColorListDialog
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.Board
import com.example.taskassigner.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CreateBoardActivity : BaseActivity(), FirestoreClass.CreateBoardCallback {
    private var binding: ActivityCreateBoardBinding? = null
    private var mSelectedBoardImageFileUri: Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageURL: String = ""

    private var mSelectedColor = ""
    private var mSelectedDueDate = ""
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            // check the result, if It's okay and if the result data is not empty,
            // then get the location of the data, URI, and assign it as background image
            if (result.resultCode == RESULT_OK && result.data != null){
                val contentURI = result.data?.data
                mSelectedBoardImageFileUri = contentURI

                try {
                    Glide
                        .with(this)
                        .load(mSelectedBoardImageFileUri)  // load requires Uri
                        .centerCrop()
                        .placeholder(R.drawable.ic_board_place_holder)
                        .into(findViewById<CircleImageView>(R.id.ivBoardImage))

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
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()  // update view when user selects a date
        }

        binding?.ivBoardImage?.setOnClickListener {
            requestStoragePermission()
        }

        binding?.tvSelectBoardLabelColor?.setOnClickListener {
            showLabelColorsListDialog()
        }

        binding?.tvSelectBoardDueDate?.setOnClickListener {
            createDatePicker()
        }

        binding?.btnCreate?.setOnClickListener {
            if (mSelectedBoardImageFileUri != null){
                uploadBoardImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoardDocument()
            }
        }

        if (intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }
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

    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        // store image to firebase storage
        val sRef : StorageReference = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE"+System.currentTimeMillis() + "." + getFileExtension(mSelectedBoardImageFileUri)
        )
        sRef.putFile(mSelectedBoardImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
            Log.i("Firebase Board Img URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->  // actual link of the image
                Log.i("Download Board Img URI", uri.toString())
                mBoardImageURL = uri.toString()

                createBoardDocument()
            }
        }.addOnFailureListener{
                exception->
            Toast.makeText(this@CreateBoardActivity, exception.message, Toast.LENGTH_LONG).show()

            cancelProgressDialog()
        }
    }

    private fun createBoardDocument() {
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
        val currentDate = sdf.format(Date())

        val boardDescription = binding?.etBoardDescription?.text.toString()


//         check if inputs are not empty
        if (validateBoardCreation()) {

            assignedUsersArrayList.add(FirestoreClass().getCurrentUserId())

            val board = Board(
                name = binding?.etBoardName?.text.toString(),
                image = mBoardImageURL,
                createdBy = mUserName,
                createdByID = FirestoreClass().getCurrentUserId(),
                assignedTo = assignedUsersArrayList,
                labelColor= mSelectedColor,
                dueDate = mSelectedDueDate,
                date = currentDate,
                description = boardDescription
            )

            FirestoreClass().createBoard(this, board)
        }
    }

    private fun getFileExtension(uri: Uri?): String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    // make board image and title required
    private fun validateBoardCreation(): Boolean{
        return when {
//            mSelectedBoardImageFileUri == null -> {
//                cancelProgressDialog()
//                Toast.makeText(this, "Please upload an image.", Toast.LENGTH_LONG).show()
//                false
//            }
            binding?.etBoardName?.text.isNullOrEmpty() ->{
                cancelProgressDialog()
                Toast.makeText(this, "Please enter a name.", Toast.LENGTH_LONG).show()
                false
            }
            else -> {
                true
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

        binding?.tvSelectBoardDueDate?.text = sdf.format(cal.time).toString()

        mSelectedDueDate = binding?.tvSelectBoardDueDate?.text.toString()
    }

    // return list of color strings
    private fun colorsList(): ArrayList<String>{
        return ArrayList(resources.getStringArray(R.array.label_colors).asList())
    }

    // set color of the background on TaskListActivity for cards
    private fun setColor(){
        binding?.tvSelectBoardLabelColor?.text = ""
        binding?.tvSelectBoardLabelColor?.setBackgroundColor(Color.parseColor(mSelectedColor))
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

    override fun createBoardSuccess() {
        Toast.makeText(this@CreateBoardActivity, "Board created successfully.", Toast.LENGTH_LONG).show()
        cancelProgressDialog()
        // set result for resultLauncherForCreateBoard
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun createBoardFailed(error: String?) {
        cancelProgressDialog()
        Log.i("Error has occurred", error.toString())
        // set result for resultLauncherForCreateBoard
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding != null){
            binding = null
        }
    }
}