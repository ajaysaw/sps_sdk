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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lib.sps.KycActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        val etMobileNo = findViewById<EditText>(R.id.etMobileNo)
        val btnDoKyc = findViewById<Button>(R.id.btnDoKyc)

        btnDoKyc.setOnClickListener {
            if(etMobileNo.text.isNotEmpty()){
                val intent = Intent(this@HomeActivity, KycActivity::class.java)
                //intent.putExtra("AgentId", "56")
                intent.putExtra("EkycToken", etMobileNo.text.toString()) //Test
               // intent.putExtra("EkycToken", "VTJGc2RHVmtYMS85RURuZ3ZzeEVFR2NGNjJ2ZzJScEcvNDZrRmxpK1lhRldlMU5rcW5neVNiNmc2TmNKekVWVGpEbGFteVJVRitJVmdkSVFFeUdxQ3pGYis4S3lXTHM2cU9oc1dXb2ZsQU5LVHArMm8vRk8reE1ZbVFNd2IrSVZaQUp5aDJ2TjBKS1dDZ3kxYk1xaEV4WENvaW5jZ3FkWDhUQ1BnNG5ZZ3VVPQ==") //Live
                //intent.putExtra("MobileNo", etMobileNo.text.toString())
                //startActivity(intent)
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