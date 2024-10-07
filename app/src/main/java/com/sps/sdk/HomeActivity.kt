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
                intent.putExtra("EkycToken", "VTJGc2RHVmtYMS9Ua0JrOExsREdpUmczK2RoYVkzaExZZXM3dW9Yam9ac3IrL1lCTzhlK3c2d1BUTmszVXJSTEd5Vm9kNC8reVBSUUNzZ2lKRUhqbTJ2aDVoZVhnU1JOWmw3TE5iUWZpYm1DazFLRGRMM0tLTmU0ZktwdThaaEQ=")
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