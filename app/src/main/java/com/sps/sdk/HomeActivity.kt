package com.sps.sdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.imps_lib.app.ui.WalletLoginActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        val etMobileNo = findViewById<EditText>(R.id.etMobileNo)
        val etAgentId = findViewById<EditText>(R.id.etAgentId)
        val etBcAgentId = findViewById<EditText>(R.id.etBcAgentId)
        val btnDoKyc = findViewById<Button>(R.id.btnDoKyc)

        btnDoKyc.setOnClickListener {
            if(etMobileNo.text.isNotEmpty()){
                //val intent = Intent(this@HomeActivity, KycActivity::class.java)
                val intent = Intent(this@HomeActivity, WalletLoginActivity::class.java)
                intent.putExtra("authorizationToken", etMobileNo.text.toString())
                intent.putExtra("agentId", etAgentId.text.toString())
                intent.putExtra("bcAgentId", etBcAgentId.text.toString())
                resultLauncher.launch(intent)
            }
        }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val message = data?.getStringExtra("message")
            val status = data?.getStringExtra("status")
            print(message)
            print(status)
        }
    }
}