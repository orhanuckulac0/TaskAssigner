package com.example.taskassigner.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.Board
import com.example.taskassigner.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class CreateBoardActivity : BaseActivity(), FirestoreClass.CreateBoardCallback {
    private var binding: ActivityCreateBoardBinding? = null
    private var mSelectedBoardImageFileUri: Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageURL: String = ""

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

        binding?.ivBoardImage?.setOnClickListener {
            requestStoragePermission()
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

    private fun createBoardDocument(){
        val assignedUsersArrayList: ArrayList<String> = ArrayList()

        // check if inputs are not empty
        if (validateBoardCreation()){

            assignedUsersArrayList.add(FirestoreClass().getCurrentUserId())

            val board = Board(
                name = binding?.etBoardName?.text.toString(),
                image = mBoardImageURL,
                createdBy = mUserName,
                assignedTo = assignedUsersArrayList
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
            }else -> {
                true
            }
        }
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