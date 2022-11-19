package com.example.taskassigner.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.taskassigner.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set splashscreen to fullscreen
        setScreenToFullSize()

        // assign the text font
        val typeFace: Typeface = Typeface.createFromAsset(assets, "Quartist.ttf")
        typeFace.isBold
        binding.tvWelcome.typeface = typeFace
        binding.tvAppName.typeface = typeFace

        // after 3 seconds, redirect to main activity and finish the splashscreen
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, IntroActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}