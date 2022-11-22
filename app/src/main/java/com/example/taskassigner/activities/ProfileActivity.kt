package com.example.taskassigner.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityProfileBinding
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.UserModel
import com.google.android.material.imageview.ShapeableImageView
import java.io.IOException

class ProfileActivity : BaseActivity(), FirestoreClass.UserDataLoadCallback {

    private var binding: ActivityProfileBinding? = null
    private var mSelectedImageFileUri: Uri? = null

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
                        .into(findViewById<ShapeableImageView>(R.id.ivUserImage))
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

    override fun userDataLoadSuccess(user: UserModel) {
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
        TODO("Not yet implemented")
    }

    private fun showRationaleDialogForGallery(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Happy Places App")
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

    override fun onDestroy() {
        super.onDestroy()
        if (binding != null){
            binding = null
        }
    }
}