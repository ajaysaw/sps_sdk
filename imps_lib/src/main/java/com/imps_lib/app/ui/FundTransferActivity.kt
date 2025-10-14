package com.imps_lib.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.imps_lib.app.R
import com.imps_lib.app.model.BeneficiaryData

class FundTransferActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etBank: EditText
    private lateinit var etAccount: EditText
    private lateinit var etIfsc: EditText
    private lateinit var etWallet: EditText
    private lateinit var etAmount: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund_transfer)

        // Initialize views
        etName = findViewById(R.id.etName)
        etBank = findViewById(R.id.etBank)
        etAccount = findViewById(R.id.etAccount)
        etIfsc = findViewById(R.id.etIfsc)
        etWallet = findViewById(R.id.etWallet)
        etAmount = findViewById(R.id.etAmount)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)


        val walletBalance = intent.getStringExtra("walletBalance")
        val topUpAmount = intent.getStringExtra("TopUpAmount")
        val walletTransNo = intent.getStringExtra("WalletTransNo")

        val beneficiaryData = intent.getParcelableExtra<BeneficiaryData>("beneficiaryData")

        beneficiaryData?.let {
            // Accessing individual properties of BeneficiaryData
//            val beneficiaryCode = it.beneficiaryCode ?: "N/A"
            val beneficiaryName = it.beneficiaryName ?: "N/A"
            val bankName = it.bankName ?: "N/A"
            val accountNumber = it.accountNumber ?: "N/A"


            etName.setText(beneficiaryName)
            etBank.setText(bankName)
            etAccount.setText(accountNumber)
            etWallet.setText(walletBalance)
            etAmount.setText(topUpAmount)

        } ?: run {
            // Handle the case when beneficiaryData is null
            Toast.makeText(this, "No beneficiary data available", Toast.LENGTH_SHORT).show()
        }

        // Back button
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Submit button
        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val bank = etBank.text.toString().trim()
            val account = etAccount.text.toString().trim()
            val ifsc = etIfsc.text.toString().trim()
            val wallet = etWallet.text.toString().trim()
            val amount = etAmount.text.toString().trim()

            if (name.isEmpty() || bank.isEmpty() || account.isEmpty() ||
                ifsc.isEmpty() || wallet.isEmpty() || amount.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                """
                ✅ Fund Transfer Details:
                Name: $name
                Bank: $bank
                A/C: $account
                IFSC: $ifsc
                Wallet: ₹$wallet
                Amount: ₹$amount
                """.trimIndent(),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
