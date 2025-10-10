package com.imps_lib.app.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.imps_lib.app.CommonMethods
import com.imps_lib.app.Coroutines
import com.imps_lib.app.R
import com.imps_lib.app.network.ApiClient
import com.imps_lib.app.network.WebInterface
import com.lib.ppi_imps.model.WalletStatusData
import com.lib.ppi_imps.model.WalletStatusResult
import com.lib.sps.KycActivity
//import KycActivity
//import java_json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class OtpActivity : AppCompatActivity(), OnClickListener {

    private val commonMethods = CommonMethods()
    private lateinit var progressDialog: Dialog
    private var job: Job? = null
    private lateinit var etMobile: EditText
    private lateinit var btnSubmit: Button
    private lateinit var tilMobile: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_login)

        progressDialog = commonMethods.progressDialog(this)
        etMobile = findViewById(R.id.etMobile)
        btnSubmit = findViewById(R.id.btnSubmit)
        tilMobile = findViewById(R.id.tilMobile)
        btnSubmit.setOnClickListener(this)
//        try{
//            EkycToken = intent.getStringExtra("EkycToken")!!
//        }catch (e:Exception){
//            print(e.message)
//        }
//        getMasterData() //call master API
    }

    override fun onClick(v: View?) {
        if (v != null && v.id == R.id.btnSubmit) {
            if (etMobile.text.toString().trim().length == 10) {
                fetchData()
            } else {
                tilMobile.error = "Please enter a valid 10-digit number"
            }

        } else {

        }

    }


    private fun fetchData() {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(this)) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
//                val jsonObject = JSONObject()
//                jsonObject.put("mobile", etMobile.text.toString().trim())
                val requestData = HashMap<String, String>()
//                requestData["data"] = commonMethods.aesEncrypt(jsonObject.toString())
                requestData["mobileNo"] = etMobile.text.toString().trim()

                service.fetchStatusData(requestData).let { response ->

                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("resend OTP response API : ", response.toString())
                            Log.d("resend OTP API response :", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
//                            val decryptData = commonMethods.aesDecrypt(JSONObject(jsonString).getString("data"))
//                            val decryptData = JSONObject(jsonString).getString("data")
                            val it = Gson().fromJson(jsonString, WalletStatusResult::class.java)
                            if (it.status == true) {
                                withContext(Dispatchers.Main) {
                                    if (it.data?.kycStatus.toString().trim() == "N") {
                                        kycPendingDialog(this@OtpActivity, it.data)
                                    }else{

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
                           /* runOnUiThread {
                                try {
                                    val decryptData = commonMethods.aesDecrypt(
                                        JSONObject(
                                            response.errorBody()!!.charStream().readText().trim()
                                        ).getString("data").trim()
                                    )
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
                            }*/
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


    private fun kycPendingDialog(ctx: Context?, data: WalletStatusData?) {
        val dialog = Dialog(ctx!!, R.style.CustomDialogStyle)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.kyc_pending_dialog)
        val tvTitle: TextView = dialog.findViewById(R.id.tvTitle)
        val tvDes: TextView = dialog.findViewById(R.id.tvDes)
        val tvCancel: TextView = dialog.findViewById(R.id.tvCancel)
        val tvProceed: TextView = dialog.findViewById(R.id.tvProceed)

        tvTitle.text = data?.ekycPopUpData?.heading ?: ""
        tvDes.text = data?.ekycPopUpData?.subheading ?: ""
        tvProceed.text = data?.ekycPopUpData?.proceedBtnText
        tvCancel.text = data?.ekycPopUpData?.cancelBtnText

        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        tvProceed.setOnClickListener {
            dialog.dismiss()

            val uri = Uri.parse(data?.ekycUrl)
            val token = uri.getQueryParameter("token")

            val intent = Intent(this@OtpActivity, KycActivity::class.java)
            intent.putExtra("EkycToken", token)
            startActivity(intent)
        }
        dialog.show()
    }
}