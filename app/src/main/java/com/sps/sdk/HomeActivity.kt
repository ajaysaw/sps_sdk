package com.sps.sdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
        intent.putExtra("MobileNo", "9988775544")
        //startActivity(intent)
        resultLauncher.launch(intent)

        btnDoKyc.setOnClickListener {
            //startActivity(intent)
            resultLauncher.launch(intent)
        }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            print("AJAY")
            val message = data?.getStringExtra("message")
            val status = data?.getStringExtra("status")
            print(message)
            print(status)
        }
    }
}