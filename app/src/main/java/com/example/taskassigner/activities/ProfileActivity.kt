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
import com.example.taskassigner.databinding.ActivityProfileBinding
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.User
import com.example.taskassigner.utils.Constants
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class ProfileActivity : BaseActivity(),
    FirestoreClass.UserDataLoadCallback,
    FirestoreClass.UserDataUpdateCallback {

    private var binding: ActivityProfileBinding? = null
    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserDetails: User
    private var mProfileImageURL: String = ""

    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            // check the result, if It's okay and if the result data is not empty,
            // then get the location of the data, URI, and assign it as background image
            if (result.resultCode == RESULT_OK && result.data != null){
                val contentURI = result.data?.data
                mSelectedImageFileUri = contentURI

                try {
                    Glide
                        .with(this)
                        .load(mSelectedImageFileUri)  // load requires Uri
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(findViewById<CircleImageView>(R.id.ivUserImage))

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
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        FirestoreClass().loadUserData(this)

        binding?.ivUserImage?.setOnClickListener {
            requestStoragePermission()
        }

        binding?.btnUpdate?.setOnClickListener{
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
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

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarProfileActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_arrow_24dp)
            actionBar.title = resources.getString(R.string.profile_title)
            binding?.toolbarProfileActivity?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
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

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null){

            // store image to firebase storage
            val sRef : StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE"+System.currentTimeMillis() + "." + getFileExtension(mSelectedImageFileUri)
            )
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.i("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->  // actual link of the image
                    Log.i("Downloadable Image URI", uri.toString())
                    mProfileImageURL = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener{
                exception->
                Toast.makeText(this@ProfileActivity, exception.message, Toast.LENGTH_LONG).show()

                cancelProgressDialog()
            }
        }
    }

    private fun getFileExtension(uri: Uri?): String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()
        var anyChangesMade = false

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
            anyChangesMade = true
        }

        if (binding?.etName?.text.toString() != mUserDetails.name && binding?.etName?.text.toString() != ""){
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
            anyChangesMade = true
        }

        if (binding?.etMobile?.text.toString() != mUserDetails.mobile.toString() && binding?.etMobile?.text.toString() != ""){
            userHashMap[Constants.MOBILE] = binding?.etMobile?.text.toString().toLong()
            anyChangesMade = true
        }

        if (anyChangesMade){
            FirestoreClass().updateUserProfileData(this, userHashMap)
        }else{
            cancelProgressDialog()
        }
    }

    // to populate view with current user data
    override fun userDataLoadSuccess(user: User) {
        // initiate the lateinit mUserDetails with the UserModel this fun receives
        // so that we can use it later on another func for val userHashMap
        mUserDetails = user

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById<ShapeableImageView>(R.id.ivUserImage))

        binding?.etName?.setText(user.name)
        binding?.etEmail?.setText(user.email)
        if (user.mobile != 0L){
            binding?.etMobile?.setText(user.mobile.toString())
        }
    }

    override fun userDataLoadFailed(error: String?) {
        Toast.makeText(this,"Something's wrong, please try again.", Toast.LENGTH_LONG).show()
        cancelProgressDialog()
        finish()
    }

    // to update view when the update result is success
    override fun updateDataLoadSuccess() {
        Toast.makeText(this,"Profile details are updated.", Toast.LENGTH_LONG).show()
        cancelProgressDialog()
        // set result for resultLauncherForProfileUpdate
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun updateDataLoadFailed(error: String?) {
        cancelProgressDialog()
        // set result for resultLauncherForProfileUpdate
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