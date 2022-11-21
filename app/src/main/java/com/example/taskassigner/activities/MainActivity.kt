package com.example.taskassigner.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.example.taskassigner.R
import com.example.taskassigner.databinding.ActivityMainBinding
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.UserModel
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity :
    BaseActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    FirestoreClass.UserDataLoadCallback
{

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        binding.navView.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this)
    }

    private fun setupActionBar(){
        val toolbar: Toolbar = findViewById(R.id.toolbarMainActivity)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar.setNavigationOnClickListener {
            //Toggle drawer
            toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.navMyProfile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.navSignOut -> {
                Firebase.auth.signOut()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.openDrawer(GravityCompat.START)
        return true
    }

    override fun userDataLoadSuccess(user: UserModel) {
        // set user image and user name on UI
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById<ShapeableImageView>(R.id.profileImage))

        findViewById<TextView>(R.id.tvUsername).text = user.name
    }

    override fun userDataLoadFailed(error: String?) {
        TODO("Not yet implemented")
    }
}