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
        val intent = Intent(this@HomeActivity, KycActivity::class.java)
        intent.putExtra("AgentId", "56")
        intent.putExtra("SecretKey", "04c5dafa4b8e83fce86675f8a4ae99d772b5")
        intent.putExtra("MobileNo", "9958957206")
        startActivity(intent)

        btnDoKyc.setOnClickListener {
            startActivity(intent)
        }
    }
}