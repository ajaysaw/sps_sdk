package com.imps_lib.app.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.google.gson.Gson
import com.imps_lib.app.ApiErrorHandler
import com.imps_lib.app.CommonMethods
import com.imps_lib.app.Coroutines
import com.imps_lib.app.GlobalData
import com.imps_lib.app.R
import com.imps_lib.app.WalletManager
import com.imps_lib.app.model.AddBeneficiaryOtp
import com.imps_lib.app.model.AddBeneficiaryOtpData
import com.imps_lib.app.model.BeneficiaryData
import com.imps_lib.app.model.FundTransferResult
import com.imps_lib.app.model.VerifyOtpResult
import com.imps_lib.app.network.ApiClient
import com.imps_lib.app.network.WebInterface
import com.mukeshsolanki.OtpView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class FundTransferActivity : AppCompatActivity() {
    private val commonMethods = CommonMethods()
    private lateinit var progressDialog: Dialog
    private var job: Job? = null
    private lateinit var etName: EditText
    private lateinit var etBank: EditText
    private lateinit var etAccount: EditText
    private lateinit var etIfsc: EditText
    private lateinit var etWallet: EditText
    private lateinit var etAmount: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageView

    private lateinit var tvResendOtpVerifyBeneficiary: TextView
    private lateinit var beneficiaryData: BeneficiaryData
    private lateinit var walletTransNo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund_transfer)
        progressDialog = commonMethods.progressDialog(this)
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
        walletTransNo = intent.getStringExtra("WalletTransNo").toString()

        beneficiaryData = intent.getParcelableExtra<BeneficiaryData>("beneficiaryData")!!

        beneficiaryData.let {
            // Accessing individual properties of BeneficiaryData
            //            val beneficiaryCode = it.beneficiaryCode ?: "N/A"
            val beneficiaryName = it.beneficiaryName ?: "N/A"
            val bankName = it.bankName ?: "N/A"
            val accountNumber = it.accountNumber ?: "N/A"
            val ifsc = it.ifsc ?: "N/A"


            etName.setText(beneficiaryName)
            etBank.setText(bankName)
            etAccount.setText(accountNumber)
            etWallet.setText(walletBalance)
            etAmount.setText(topUpAmount)
            etIfsc.setText(ifsc)

        }

        // Back button
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Submit button
        btnSubmit.setOnClickListener {
            confirmationDialog()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun confirmationDialog() {
        val dialog = Dialog(this, R.style.CustomDialogStyle)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.verify_dialog)
        val tvTitle: TextView = dialog.findViewById(R.id.tvTitle)
        val tvDes: TextView = dialog.findViewById(R.id.tvDes)

        tvTitle.text =
            "Are you sure you want to transfer ₹" + etAmount.text.toString() + " to " + etName.text.toString() + " ?"


        val tvCancel: TextView = dialog.findViewById(R.id.tvCancel)
        val tvProceed: TextView = dialog.findViewById(R.id.tvProceed)
        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        tvProceed.setOnClickListener {
            dialog.dismiss()
            generateOtp()
        }
        dialog.show()
    }

    private fun generateOtp() {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(this)) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()

                requestData["mobileNo"] = GlobalData.mobileNumber
                requestData["otpType"] = "MONEY_TRANSFER_OTP"
                requestData["accountNumber"] = etAccount.text.toString()
                requestData["amount"] = etAmount.text.toString()

                service.generateOtp(requestData).let { response ->

                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("resend OTP response API : ", response.toString())
                            Log.d("resend OTP API response :", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())

                            val it = Gson().fromJson(jsonString, AddBeneficiaryOtp::class.java)
                            if (it.status) {
                                withContext(Dispatchers.Main) {
                                    otpVerify(it.data)
                                }
                            } else {
                                print(it.message)
                                runOnUiThread {
                                    commonMethods.showMessageDialog(
                                        this,
                                        it.message,
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        } else {
                            runOnUiThread {
                                // ❌ Centralized error handling
                                val errorMessage = ApiErrorHandler.getErrorMessage(response)
                                Log.e("API_ERROR", errorMessage)
                                commonMethods.showMessageDialog(
                                    this,
                                    errorMessage,
                                    "Error",
                                    "",
                                    false
                                )
                            }
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e)
                        progressDialog.dismiss()
                        runOnUiThread {
                            commonMethods.showMessageDialog(
                                this,
                                e.localizedMessage ?: "Something went wrong.",
                                "Error",
                                "",
                                false
                            )
                        }
                    }
                }
            } else {
                progressDialog.dismiss()
                runOnUiThread {
                    commonMethods.showMessageDialog(
                        this,
                        "No Internet....Please be connected to a working internet",
                        "Alert!",
                        "",
                        false
                    )
                }
            }
        }
    }

    private fun startOtpTimer() {
        // Start countdown timer 30 seconds
        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvResendOtpVerifyBeneficiary.text =
                    "${getString(R.string.resend_otp_message)} 00:${
                        String.format(
                            "%02d",
                            seconds
                        )
                    }"
                tvResendOtpVerifyBeneficiary.setTextColor(
                    ContextCompat.getColor(
                        this@FundTransferActivity,
                        R.color.gray
                    )
                )
            }

            override fun onFinish() {
                tvResendOtpVerifyBeneficiary.text =
                    "${getString(R.string.resend_otp_text)}"
                tvResendOtpVerifyBeneficiary.isEnabled = true
                val spannable = SpannableString(tvResendOtpVerifyBeneficiary.text)
                val boldSpan = StyleSpan(Typeface.BOLD)
                spannable.setSpan(
                    boldSpan,
                    tvResendOtpVerifyBeneficiary.text.indexOf("Resend"),
                    tvResendOtpVerifyBeneficiary.text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                tvResendOtpVerifyBeneficiary.text = spannable
                tvResendOtpVerifyBeneficiary.setTextColor(
                    ContextCompat.getColor(
                        this@FundTransferActivity,
                        R.color.colorPrimary
                    )
                )
            }
        }.start()
    }

    private fun otpVerify(data: AddBeneficiaryOtpData?) {
        val dialog = Dialog(this, R.style.CustomDialogStyle)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.otp_verify_beneficiary_pop_up)
        val tvDes: TextView = dialog.findViewById(R.id.tvOtpSubtitleVerifyBeneficiary)
        val tvCancel: TextView = dialog.findViewById(R.id.tvCancel)
        val tvProceed: TextView = dialog.findViewById(R.id.tvVerifyProceed)
        tvResendOtpVerifyBeneficiary = dialog.findViewById(R.id.tvResendOtpVerifyBeneficiary)
        val otpView = dialog.findViewById<OtpView>(R.id.otp_view_VerifyBeneficiary)
        tvDes.text = data?.message
        tvResendOtpVerifyBeneficiary.isEnabled = false
        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        tvProceed.setOnClickListener {
            val otp = otpView.text.toString()
            if (otp.length == 6) {
                dialog.dismiss()
                verifyOtp(otp, data?.requestNo!!, data.otp!!)
            } else {
                Toast.makeText(
                    this@FundTransferActivity,
                    "Please enter 6-digit OTP",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        otpView.setOtpCompletionListener { otp ->
            dialog.dismiss()
            verifyOtp(otp, data?.requestNo!!, data.otp!!)
        }

        // Resend OTP logic
        tvResendOtpVerifyBeneficiary.setOnClickListener {
            reSendOtp(data?.requestNo!!)
        }
        startOtpTimer()
        dialog.show()
    }

    private fun reSendOtp(requestNo: String) {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(this)) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                requestData["mobileNo"] = GlobalData.mobileNumber
                requestData["requestNo"] = requestNo

                service.resendOtp(requestData).let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("resend OTP response API : ", response.toString())
                            Log.d("resend OTP API response :", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
                            val it = Gson().fromJson(jsonString, VerifyOtpResult::class.java)
                            if (it.status == true) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@FundTransferActivity,
                                        it.message,
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    startOtpTimer()
//                                    getBeneData()
                                }

                            } else {
                                runOnUiThread {
                                    commonMethods.showMessageDialog(
                                        this@FundTransferActivity,
                                        it.message,
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        } else {
                            runOnUiThread {
                                // ❌ Centralized error handling
                                val errorMessage = ApiErrorHandler.getErrorMessage(response)
                                Log.e("API_ERROR", errorMessage)
                                commonMethods.showMessageDialog(
                                    this,
                                    errorMessage,
                                    "Error",
                                    "",
                                    false
                                )
                            }
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e)
                        progressDialog.dismiss()
                        runOnUiThread {
                            commonMethods.showMessageDialog(
                                this,
                                e.localizedMessage ?: "Something went wrong.",
                                "Error",
                                "",
                                false
                            )
                        }
                    }
                }
            } else {
                progressDialog.dismiss()
                runOnUiThread {
                    commonMethods.showMessageDialog(
                        this@FundTransferActivity,
                        "No Internet....Please be connected to a working internet",
                        "Alert!",
                        "",
                        false
                    )
                }
            }
        }
    }

    private fun verifyOtp(otp: String, requestNo: String, otpHash: String) {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(this@FundTransferActivity)) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()

                requestData["otpHash"] = otpHash
                requestData["action"] = "MONEY_TRANSFER_OTP"
                requestData["mobileNo"] = GlobalData.mobileNumber
                requestData["requestNo"] = requestNo
                requestData["otp"] = otp

                service.verifyOtp(requestData).let { response ->

                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("resend OTP response API : ", response.toString())
                            Log.d("resend OTP API response :", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
                            val it = Gson().fromJson(jsonString, VerifyOtpResult::class.java)
                            if (it.status == true) {
                                withContext(Dispatchers.Main) {
                                    fundTransfer(it.data?.verificationReferenceNo!!)
                                }
                            } else {
                                print(it.message)
                                runOnUiThread {
                                    commonMethods.showMessageDialog(
                                        this@FundTransferActivity,
                                        it.message,
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        } else {
                            runOnUiThread {
                                // ❌ Centralized error handling
                                val errorMessage = ApiErrorHandler.getErrorMessage(response)
                                Log.e("API_ERROR", errorMessage)
                                commonMethods.showMessageDialog(
                                    this,
                                    errorMessage,
                                    "Error",
                                    "",
                                    false
                                )
                            }
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e)
                        progressDialog.dismiss()
                        runOnUiThread {
                            commonMethods.showMessageDialog(
                                this@FundTransferActivity,
                                e.toString(),
                                "Error",
                                "",
                                false
                            )
                        }
                    }
                }
            } else {
                progressDialog.dismiss()
                runOnUiThread {
                    commonMethods.showMessageDialog(
                        this@FundTransferActivity,
                        "No Internet....Please be connected to a working internet",
                        "Alert!",
                        "",
                        false
                    )
                }
            }
        }
    }

    private fun fundTransfer(otpVerificationReferenceNo: String) {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(this)) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()


                requestData["mobileNo"] = GlobalData.mobileNumber
                requestData["amount"] = etAmount.text.toString()
                requestData["beneficiaryName"] = beneficiaryData.beneficiaryName.toString()
                requestData["beneficiaryCode"] = beneficiaryData.beneficiaryCode.toString()
                requestData["accountType"] = beneficiaryData.accountType.toString()
                requestData["ifsc"] = beneficiaryData.ifsc.toString()
                requestData["accountNo"] = beneficiaryData.accountNumber.toString()
                requestData["otpVerificationReferenceNo"] = otpVerificationReferenceNo
                requestData["topUpTransactionNo"] = walletTransNo


                service.fundTransfer(requestData).let { response ->

                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("resend OTP response API : ", response.toString())
                            Log.d("resend OTP API response :", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())

                            val it = Gson().fromJson(jsonString, FundTransferResult::class.java)
                            if (it.status) {
                                withContext(Dispatchers.Main) {
                                    fundSuccessDialog(
                                        it.data?.amount.toString(),
                                        it.data?.requestTransNo.toString()
                                    )
                                }
                            } else {
                                print(it.message)
                                runOnUiThread {
                                    commonMethods.showMessageDialog(
                                        this,
                                        it.message,
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        } else {
                            runOnUiThread {
                                // ❌ Centralized error handling
                                val errorMessage = ApiErrorHandler.getErrorMessage(response)
                                Log.e("API_ERROR", errorMessage)
                                commonMethods.showMessageDialog(
                                    this,
                                    errorMessage,
                                    "Error",
                                    "",
                                    false
                                )
                            }
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e)
                        progressDialog.dismiss()
                        runOnUiThread {
                            commonMethods.showMessageDialog(
                                this,
                                e.localizedMessage ?: "Something went wrong.",
                                "Error",
                                "",
                                false
                            )
                        }
                    }
                }
            } else {
                progressDialog.dismiss()
                runOnUiThread {
                    commonMethods.showMessageDialog(
                        this,
                        "No Internet....Please be connected to a working internet",
                        "Alert!",
                        "",
                        false
                    )
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun fundSuccessDialog(amount: String, txnId: String) {
        WalletManager.fetchBalance(this@FundTransferActivity, CommonMethods())
        val dialog = Dialog(this@FundTransferActivity, R.style.CustomDialogStyle)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_fund_transfer_success)

        val tvDescription: TextView = dialog.findViewById(R.id.tvDescription)
        val tvOkay: TextView = dialog.findViewById(R.id.tvOkay)

        val description = buildSpannedString {
            append("Your fund transfer of ")
            color(Color.parseColor("#000000")) { bold { append("Rs. $amount") } }
            append(" to ")
            color(Color.parseColor("#000000")) { bold { append(beneficiaryData.beneficiaryName.toString()) } }
            append(" has been initiated successfully with transaction ID ")
            color(Color.parseColor("#000000")) { bold { append(txnId) } }
        }


        tvDescription.text = description

        tvOkay.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}
