package com.example.taskassigner.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.taskassigner.databinding.ActivitySplashBinding
import com.example.taskassigner.firebase.FirestoreClass

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private var binding: ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // set splashscreen to fullscreen
        setScreenToFullSize()

        // assign the text font
        val typeFace: Typeface = Typeface.createFromAsset(assets, "Quartist.ttf")
        typeFace.isBold
        binding?.tvWelcome?.typeface = typeFace
        binding?.tvAppName?.typeface = typeFace

        // after 3 seconds, redirect to main activity and finish the splashscreen
        Handler(Looper.getMainLooper()).postDelayed({
            // check if current user is still logged in, or recently logged in and we have the data
            val currentUserID = FirestoreClass().getCurrentUserId()
            if (currentUserID.isNotEmpty()){
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(this@SplashActivity, IntroActivity::class.java)
                startActivity(intent)
            }
            finish()

        }, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (binding != null){
            binding = null
        }
    }
}