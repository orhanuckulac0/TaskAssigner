package com.example.taskassigner.activities

import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityProfileBinding
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.UserModel
import com.google.android.material.imageview.ShapeableImageView

class ProfileActivity : BaseActivity(), FirestoreClass.UserDataLoadCallback {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        FirestoreClass().loadUserData(this)
    }

    private fun setupActionBar(){
        setSupportActionBar(binding.toolbarProfileActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_arrow_24dp)
            actionBar.title = resources.getString(R.string.profile_title)
            binding.toolbarProfileActivity.setNavigationOnClickListener {
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

        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        if (user.mobile != 0L){
            binding.etMobile.setText(user.mobile.toString())
        }
    }

    override fun userDataLoadFailed(error: String?) {
        TODO("Not yet implemented")
    }
}