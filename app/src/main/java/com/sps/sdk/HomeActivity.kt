package com.sps.sdk

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lib.sps.KycActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        val btnDoKyc = findViewById<Button>(R.id.btnDoKyc)

        startActivity(Intent(this@HomeActivity, KycActivity::class.java))

        btnDoKyc.setOnClickListener {
            startActivity(Intent(this@HomeActivity, KycActivity::class.java))
        }
    }
}