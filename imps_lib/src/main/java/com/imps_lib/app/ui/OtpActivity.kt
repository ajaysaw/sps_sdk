package com.imps_lib.app.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.imps_lib.app.CommonMethods
import com.imps_lib.app.Coroutines
import com.imps_lib.app.PrefManager
import com.imps_lib.app.R
import com.imps_lib.app.model.VerifyOtpResult
import com.imps_lib.app.network.ApiClient
import com.imps_lib.app.network.WebInterface
import com.lib.sps.java_json.JSONObject
import com.mukeshsolanki.OtpView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.util.Locale

class OtpActivity : AppCompatActivity() {

    private val commonMethods = CommonMethods()
    private lateinit var progressDialog: Dialog
    private var job: Job? = null
    private lateinit var tvResend: TextView
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        progressDialog = commonMethods.progressDialog(this)
        val otpView = findViewById<OtpView>(R.id.otp_view)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitOtp)

        val ivEdit = findViewById<ImageView>(R.id.ivEdit)
        val tvNumber = findViewById<TextView>(R.id.tvNumber)

        tvResend = findViewById(R.id.tvResendOtp)

        val mobile = intent.getStringExtra("mobile") ?: ""
        val requestNo = intent.getStringExtra("requestNo") ?: ""


        tvNumber.text = mobile

        ivEdit.setOnClickListener {
            this@OtpActivity.finish()
        }

        startResendTimer() // start timer on launch

        otpView.setOtpCompletionListener { otp ->
            verifyOtp(otp, mobile, requestNo)
        }

        btnSubmit.setOnClickListener {
            val otp = otpView.text.toString()
            if (otp.length == 6) {
                verifyOtp(otp, mobile, requestNo)
            } else {
                Toast.makeText(this, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        tvResend.setOnClickListener {
            if (tvResend.isEnabled) {
                reSendOtp(mobile, requestNo)
                startResendTimer()
            }
        }
    }

    private fun startResendTimer() {
        tvResend.isEnabled = false
        tvResend.setTextColor(ContextCompat.getColor(this, R.color.greyColor))

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(30_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvResend.text =
                    "Didn't get the OTP? Resend OTP in 00:${String.format("%02d", seconds)}"
            }

            override fun onFinish() {
                val fullText = "Didn't get the OTP? Resend OTP"
                val spannable = SpannableString(fullText)
                val boldSpan = StyleSpan(Typeface.BOLD)
                spannable.setSpan(
                    boldSpan,
                    fullText.indexOf("Resend"),
                    fullText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                tvResend.text = spannable
                tvResend.isEnabled = true
                tvResend.setTextColor(
                    ContextCompat.getColor(
                        this@OtpActivity,
                        R.color.colorPrimary
                    )
                )
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }


    private fun verifyOtp(otp: String, mobile: String, requestNo: String) {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(this)) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                requestData["mobileNo"] = mobile
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
                                    if (it.data?.otpStatus.toString().trim()
                                            .uppercase(Locale.ROOT) == "VERIFIED"
                                    ) {
                                        PrefManager.getInstance(this@OtpActivity).putString("MOBILE",mobile)

                                        val intent = Intent(
                                            this@OtpActivity,
                                            MrHomeActivity::class.java
                                        )
                                        startActivity(intent)
                                    } else {

                                        commonMethods.showMessageDialog(
                                            this@OtpActivity,
                                            it.message,
                                            "Error",
                                            "",
                                            false
                                        )
                                    }
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
                                try {
                                    val decryptData =
                                        JSONObject(
                                            response.errorBody()!!.charStream().readText().trim()
                                        ).getString("data").trim()
                                    val jsonObj = JSONObject(decryptData)
                                    if (jsonObj.has("message") && jsonObj.getString("message")
                                            .uppercase() == "FAILURE"
                                    ) {
                                        commonMethods.showMessageDialog(
                                            this,
                                            jsonObj.getString("error_message"),
                                            "Error",
                                            "",
                                            false
                                        )
                                    } else
                                        commonMethods.showMessageDialog(
                                            this,
                                            jsonObj.getString("error_message"),
                                            "Error",
                                            "",
                                            false
                                        )
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(
                                        this,
                                        e.message.toString(),
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e)
                        progressDialog.dismiss()
                        runOnUiThread {
                            commonMethods.showMessageDialog(
                                this,
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

    private fun reSendOtp(mobile: String, requestNo: String) {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(this)) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                requestData["mobileNo"] = mobile
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
                                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
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
                                try {
                                    val decryptData =
                                        JSONObject(
                                            response.errorBody()!!.charStream().readText().trim()
                                        ).getString("data").trim()

                                    val jsonObj = JSONObject(decryptData)
                                    if (jsonObj.has("message") && jsonObj.getString("message")
                                            .uppercase() == "FAILURE"
                                    ) {
                                        commonMethods.showMessageDialog(
                                            this,
                                            jsonObj.getString("error_message"),
                                            "Error",
                                            "",
                                            false
                                        )
                                    } else
                                        commonMethods.showMessageDialog(
                                            this,
                                            jsonObj.getString("error_message"),
                                            "Error",
                                            "",
                                            false
                                        )
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(
                                        this,
                                        e.message.toString(),
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e)
                        progressDialog.dismiss()
                        runOnUiThread {
                            commonMethods.showMessageDialog(
                                this,
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
}




