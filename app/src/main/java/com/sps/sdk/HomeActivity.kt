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
            //if(etMobileNo.text.length==10){
                val intent = Intent(this@HomeActivity, KycActivity::class.java)
                //intent.putExtra("AgentId", "56")
                //intent.putExtra("EkycToken", "VTJGc2RHVmtYMTl3bWpPdXpyQlRKWk1BajVXQTZncG9tMnVrT2RpcUhqZTdCSE9sSnU0M0Fxd1N3S1hsbjBEMTNHQlNTdndvVUVJYmo1SXFBU2FYWUFST2R0b0svV0JwL25XT3c0eVMrU1ZTa3NlSTBkanRhaU4vYjR6Zm5KNzl6SWpqaUJaci9VWG5udHBzU3hOeDF1eDg1MDUzNFpBTE5BeTM4OTlUSUdRPQ==") //Test
                intent.putExtra("EkycToken", "VTJGc2RHVmtYMSt2dk8xZSt4eHJWRjhRWjlyUWpCUHZwdXZOVXBseU03Y0xSY3l4dW90cHB6Tng3dTB5THNhdUJCWnZTS1VPWVJuZ2FMVVlxUTJXN00rSWxJdDY4Rm9OR2Z2dmE0RUNRa3g2Y0kwMWxKQjloOXllamNHQ3ZrTUY3THplV3pQajJJVDFncUdEeHFrbCtyRExHZ3hWZDA2RjZTSVM1MTdwMWFjPQ==") //Live
                //intent.putExtra("MobileNo", etMobileNo.text.toString())
                //startActivity(intent)
                resultLauncher.launch(intent)
            //}
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