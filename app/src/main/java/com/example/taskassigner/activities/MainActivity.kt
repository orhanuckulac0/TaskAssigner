package com.example.taskassigner.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.taskassigner.R
import com.example.taskassigner.adapters.BoardItemsAdapter
import com.example.taskassigner.databinding.ActivityMainBinding
import com.example.taskassigner.firebase.FirestoreClass
import com.example.taskassigner.models.Board
import com.example.taskassigner.models.User
import com.example.taskassigner.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity :
    BaseActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    FirestoreClass.UserDataLoadCallback,
    FirestoreClass.GetBoardsListCallback,
        FirestoreClass.UserDataUpdateCallback
{

    private var binding: ActivityMainBinding? = null
    private lateinit var mUserName: String
    private var dividerCreated: Boolean = false

    private lateinit var mSharedPreferences: SharedPreferences
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var resultLauncherForProfileUpdate = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            // refresh the user data
            FirestoreClass().loadUserData(this)
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            Log.e("Error", "Cancelled")
        }
    }

    private var resultLauncherForCreateBoard = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            // refresh the UI with new boards
            FirestoreClass().getBoardsList(this)
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            Log.e("Error", "Cancelled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()
        binding?.navView?.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(Constants.TASKASSIGNER_PREFERENCES, Context.MODE_PRIVATE)
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (!tokenUpdated){
            FirebaseMessaging.getInstance().token.addOnSuccessListener(this@MainActivity){
                updateFCMToken(it)
            }
        }

        // get user data and board data onResume
        // so that if new data or board is created user will see the updated UI

        // setup intent for fab
        findViewById<FloatingActionButton>(R.id.fabAddBoard).setOnClickListener {
            val intent = Intent(this@MainActivity, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            resultLauncherForCreateBoard.launch(intent)
        }
        refreshApp()
    }

    private fun refreshApp(){
        swipeRefreshLayout = findViewById(R.id.swipeToRefresh)
        swipeRefreshLayout.setOnRefreshListener{
            swipeRefreshLayout.isRefreshing = false
            onResume()
        }
    }

    // on callback success result, populate the UI with boardsList fetched from Firestore
    private fun populateBoardsListToUI(boardsList: ArrayList<Board>){

        val rvBoardsList = findViewById<RecyclerView>(R.id.rvBoardsList)
        val tvNoBoardsAvailable = findViewById<TextView>(R.id.tvNoBoardsAvailable)

        if (boardsList.size > 0){

            rvBoardsList.visibility = View.VISIBLE
            tvNoBoardsAvailable.visibility = View.GONE

            // setup layout
            val layoutManager = LinearLayoutManager(this)
            rvBoardsList.layoutManager = LinearLayoutManager(this)
            rvBoardsList.setHasFixedSize(true)

            // setup adapter
            val adapter = BoardItemsAdapter(this, boardsList)
            rvBoardsList.adapter = adapter

            // setup onClickListener to each adapter item
            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    // pass the current clicked documentID to get its details on TaskListActivity
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })

            // divider between recycler view items
            // keep track if divider is created before or not
            // because each time screen re-uploads with onResume, divider gets created again
            // this causes divider color to get thicken
            if (!dividerCreated){
                val dividerItemDecoration =
                    DividerItemDecoration(rvBoardsList.context, layoutManager.orientation)
                rvBoardsList.addItemDecoration(dividerItemDecoration)
                dividerCreated = true
            }

        }else{
            rvBoardsList.visibility = View.GONE
            tvNoBoardsAvailable.visibility = View.VISIBLE
        }
        cancelProgressDialog()
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
        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    private fun updateFCMToken(token: String){
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        FirestoreClass().updateUserProfileData(this, userHashMap)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.navMyProfile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                // start the activity for result
                resultLauncherForProfileUpdate.launch(intent)
            }
            R.id.navSignOut -> {
                Firebase.auth.signOut()

                // reset the shared preferences to empty
                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout!!.openDrawer(GravityCompat.START)
        return true
    }

    override fun userDataLoadSuccess(user: User) {
        mUserName = user.name

        // set user image and user name on UI
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById<ShapeableImageView>(R.id.profileImage))

        findViewById<TextView>(R.id.tvUsername).text = user.name

        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        cancelProgressDialog()
    }

    override fun userDataLoadFailed(error: String?) {
        cancelProgressDialog()
        Log.i("Error occurred", error.toString())
    }

    // get boards from Firestore callback
    override fun getBoardsSuccess(boardsList: ArrayList<Board>) {
        cancelProgressDialog()
        populateBoardsListToUI(boardsList)
    }

    override fun getBoardsFailed(error: String?) {
        cancelProgressDialog()
        Log.i("Fetch BoardsList Error", "$error")
    }

    override fun updateDataLoadSuccess() {
        Log.i("Token Added:", "SUCCESSFUL")
    }

    override fun updateDataLoadFailed(error: String?) {
        Log.i("update data failed", "$error")
    }

    override fun onResume() {
        super.onResume()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this)
        FirestoreClass().getBoardsList(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding != null){
            binding = null
        }
    }
}