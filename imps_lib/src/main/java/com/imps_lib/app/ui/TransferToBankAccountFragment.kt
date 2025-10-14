package com.imps_lib.app.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.imps_lib.app.CommonMethods
import com.imps_lib.app.Coroutines
import com.imps_lib.app.PrefManager
import com.imps_lib.app.R
import com.imps_lib.app.model.AddBeneficiaryOtp
import com.imps_lib.app.model.AddBeneficiaryOtpData
import com.imps_lib.app.model.BeneficiaryData
import com.imps_lib.app.model.BeneficiaryListResult
import com.imps_lib.app.model.VerifyBeneficiary
import com.imps_lib.app.model.VerifyOtpResult
import com.imps_lib.app.model.WalletBalance
import com.imps_lib.app.network.ApiClient
import com.imps_lib.app.network.WebInterface
import com.lib.ppi_imps.model.LastTopUpResult
import com.lib.sps.java_json.JSONObject
import com.mukeshsolanki.OtpView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class TransferToBankAccountFragment : Fragment() {

    private lateinit var rvBeneficiaries: RecyclerView
    private lateinit var adapter: BeneficiaryAdapter
    private val commonMethods = CommonMethods()
    private lateinit var progressDialog: Dialog
    private var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_transfer_to_bank_account, container, false)
        rvBeneficiaries = root.findViewById(R.id.rvBeneficiaries)
        rvBeneficiaries.layoutManager = LinearLayoutManager(requireContext())

        adapter = BeneficiaryAdapter(emptyList()) { beneficiary, action ->
            when (action) {
                "verify" -> {
                    verifyBeneDialog(beneficiary)
                }

                "delete" -> {
                    deleteBeneDialog(beneficiary)
                }

                "transfer" -> {
                    getBalance(beneficiary)

                }
            }
        }
        rvBeneficiaries.adapter = adapter

        // Fetch beneficiary list


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ✅ Safe place to initialize dialog in Fragment
        progressDialog = commonMethods.progressDialog(requireContext())
//        getBeneData()
    }

    override fun onResume() {
        super.onResume()
        getBeneData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    private fun getBeneData() {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }

            if (!commonMethods.isNetworkConnected(requireContext())) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    commonMethods.showMessageDialog(
                        requireContext(),
                        "No Internet... Please connect to a working network",
                        "Alert!",
                        "",
                        false
                    )
                }
                return@io
            }

            val service: WebInterface = ApiClient().createService(WebInterface::class.java)
            val requestData = HashMap<String, String>().apply {
                put("mobileNo", PrefManager.getInstance(requireContext()).getString("MOBILE"))
            }

            try {
                val response = service.getAllBeneList(requestData)
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    if (response.isSuccessful && response.body() != null) {
                        Log.d("Beneficiary API:", response.body().toString())

                        val jsonString = Gson().toJson(response.body())
                        val parsed = Gson().fromJson(jsonString, BeneficiaryListResult::class.java)

                        if (parsed.status == true && parsed.data.isNotEmpty()) {
                            adapter.updateData(parsed.data)
                        } else {
                            commonMethods.showMessageDialog(
                                requireContext(),
                                parsed.message ?: "No beneficiaries found",
                                "Info",
                                "",
                                false
                            )
                        }

                    } else {
                        handleApiError(response.errorBody()?.charStream()?.readText()?.trim())
                    }
                }

            } catch (e: Exception) {
                Log.e("Beneficiary API Error", e.toString())
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    commonMethods.showMessageDialog(
                        requireContext(),
                        e.localizedMessage ?: "Something went wrong",
                        "Error",
                        "",
                        false
                    )
                }
            }
        }
    }

    private suspend fun handleApiError(errorResponse: String?) {
        withContext(Dispatchers.Main) {
            try {
                val decryptData = JSONObject(errorResponse ?: "").getString("data").trim()
                val jsonObj = JSONObject(decryptData)
                val message = jsonObj.optString("error_message", "Something went wrong")

                commonMethods.showMessageDialog(
                    requireContext(),
                    message,
                    "Error",
                    "",
                    false
                )
            } catch (e: Exception) {
                commonMethods.showMessageDialog(
                    requireContext(),
                    e.localizedMessage ?: "Unknown error occurred",
                    "Error",
                    "",
                    false
                )
            }
        }
    }

    private fun verifyBeneficiary(beneficiaryData: BeneficiaryData) {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(requireContext())) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                requestData["mobileNo"] =
                    PrefManager.getInstance(requireContext()).getString("MOBILE")
                requestData["accountNo"] = beneficiaryData.accountNumber.toString()
                requestData["ifsc"] = beneficiaryData.ifsc.toString()
                requestData["beneficiaryCode"] = beneficiaryData.beneficiaryCode.toString()
                service.verifyBeneficiary(requestData).let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("verify beneficiary API", response.toString())
                            Log.d("verify beneficiary  response", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
                            val it = Gson().fromJson(
                                org.json.JSONObject(jsonString).toString(),
                                VerifyBeneficiary::class.java
                            )
                            if (it.status) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        requireContext(),
                                        it.data?.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    getBeneData()
                                }

                            } else {
                                requireActivity().runOnUiThread {
                                    commonMethods.showMessageDialog(
                                        requireContext(),
                                        it.message,
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                try {
                                    commonMethods.showMessageDialog(
                                        requireContext(), org.json.JSONObject(
                                            response.errorBody()!!.charStream().readText().trim()
                                        ).getString("message").trim(), "Error", "", false
                                    )
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(
                                        requireContext(),
                                        e.message.toString(),
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        progressDialog.dismiss()
                        requireActivity().runOnUiThread {
                            commonMethods.showMessageDialog(
                                requireContext(),
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
                requireActivity().runOnUiThread {
                    commonMethods.showMessageDialog(
                        requireContext(),
                        "No Internet....Please be connected to a working internet",
                        "Alert!",
                        "",
                        false
                    )
                }
            }
        }
    }

    private fun deleteBeneficiary(beneficiaryData: BeneficiaryData) {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(requireContext())) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                requestData["mobileNo"] =
                    PrefManager.getInstance(requireContext()).getString("MOBILE")
                requestData["beneficiaryCode"] = beneficiaryData.beneficiaryCode.toString()
                requestData["beneficiaryName"] = beneficiaryData.beneficiaryName.toString()
                requestData["beneficiaryType"] = beneficiaryData.beneficiaryType.toString()
                requestData["accountNo"] = beneficiaryData.accountNumber.toString()
                requestData["ifsc"] = beneficiaryData.ifsc.toString()
                requestData["accountType"] = beneficiaryData.accountType.toString()
                service.deleteBeneficiary(requestData).let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("add beneficiary API", response.toString())
                            Log.d("add beneficiary  response", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
                            val it = Gson().fromJson(
                                org.json.JSONObject(jsonString).toString(),
                                AddBeneficiaryOtp::class.java
                            )
                            if (it.status) {
                                withContext(Dispatchers.Main) {
                                    otpVerifyAddBeneficiary(requireContext(), it.data)
                                }
                            } else {
                                requireActivity().runOnUiThread {
                                    commonMethods.showMessageDialog(
                                        requireContext(),
                                        it.message,
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                try {
                                    commonMethods.showMessageDialog(
                                        requireContext(), org.json.JSONObject(
                                            response.errorBody()!!.charStream().readText().trim()
                                        ).getString("message").trim(), "Error", "", false
                                    )
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(
                                        requireContext(),
                                        e.message.toString(),
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        progressDialog.dismiss()
                        requireActivity().runOnUiThread {
                            commonMethods.showMessageDialog(
                                requireContext(),
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
                requireActivity().runOnUiThread {
                    commonMethods.showMessageDialog(
                        requireContext(),
                        "No Internet....Please be connected to a working internet",
                        "Alert!",
                        "",
                        false
                    )
                }
            }
        }
    }

    private fun verifyOtp(otp: String, requestNo: String) {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(requireContext())) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                requestData["mobileNo"] = "8800985790"
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
                                    Toast.makeText(
                                        requireContext(),
                                        it.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    getBeneData()
                                }
                            } else {
                                print(it.message)
                                requireActivity().runOnUiThread {
                                    commonMethods.showMessageDialog(
                                        requireContext(),
                                        it.message,
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                try {
                                    // Read the raw error body once
                                    val errorBodyStr = response.errorBody()?.string()
                                    Log.e("API_ERROR_BODY", errorBodyStr ?: "null")
                                    var errorMessage = "Unknown error"
                                    if (!errorBodyStr.isNullOrEmpty()) {
                                        val jsonObj = JSONObject(errorBodyStr)
                                        if (jsonObj.has("message") && !jsonObj.isNull("message")) {
                                            errorMessage = jsonObj.getString("message")
                                        } else if (jsonObj.has("error_message") && !jsonObj.isNull("error_message")) {
                                            errorMessage = jsonObj.getString("error_message")
                                        }
                                    }

                                    commonMethods.showMessageDialog(
                                        requireContext(),          // use activity context explicitly
                                        errorMessage,
                                        "Error",
                                        "",
                                        false
                                    )
                                } catch (e: Exception) {
                                    // In case parsing fails, show exception text
                                    commonMethods.showMessageDialog(
                                        requireContext(),
                                        e.message ?: "Something went wrong",
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
                        requireActivity().runOnUiThread {
                            commonMethods.showMessageDialog(
                                requireContext(),
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
                requireActivity().runOnUiThread {
                    commonMethods.showMessageDialog(
                        requireContext(),
                        "No Internet....Please be connected to a working internet",
                        "Alert!",
                        "",
                        false
                    )
                }
            }
        }
    }

    private fun reSendOtp(requestNo: String) {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(requireContext())) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                requestData["mobileNo"] = "8800985790"
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
                                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT)
                                        .show()
                                    getBeneData()
                                }

                            } else {
                                requireActivity().runOnUiThread {
                                    commonMethods.showMessageDialog(
                                        requireContext(),
                                        it.message,
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        } else {
                            requireActivity().runOnUiThread {
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
                                            requireContext(),
                                            jsonObj.getString("error_message"),
                                            "Error",
                                            "",
                                            false
                                        )
                                    } else
                                        commonMethods.showMessageDialog(
                                            requireContext(),
                                            jsonObj.getString("error_message"),
                                            "Error",
                                            "",
                                            false
                                        )
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(
                                        requireContext(),
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
                        requireActivity().runOnUiThread {
                            commonMethods.showMessageDialog(
                                requireContext(),
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
                requireActivity().runOnUiThread {
                    commonMethods.showMessageDialog(
                        requireContext(),
                        "No Internet....Please be connected to a working internet",
                        "Alert!",
                        "",
                        false
                    )
                }
            }
        }
    }

    private fun getBalance(beneficiaryData: BeneficiaryData) {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(requireContext())) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                requestData["mobileNo"] =
                    PrefManager.getInstance(requireContext()).getString("MOBILE")

                service.getBalance(requestData).let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("get-balance API", response.toString())
                            Log.d("get-balance API response", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
                            val it = Gson().fromJson(
                                org.json.JSONObject(jsonString).toString(),
                                WalletBalance::class.java
                            )
                            if (it.status) {
                                withContext(Dispatchers.Main) {
                                    getLastTopUp(it.data?.balance.toString(),beneficiaryData)
                                    /*walletBalance = it
                                    tvLimit.text = "Limit: ₹${it.data?.remainingCashDepositLimit?:0.0}"
                                    tvBalance.text = "Balance: ₹${it.data?.balance?:0.0}"*/
                                }
                            } else {
                                requireActivity().runOnUiThread {
                                    commonMethods.showMessageDialog(
                                        requireContext(),
                                        it.message,
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                try {
                                    commonMethods.showMessageDialog(
                                        requireContext(), org.json.JSONObject(
                                            response.errorBody()!!.charStream().readText().trim()
                                        ).getString("data").trim(), "Error", "", false
                                    )
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(
                                        requireContext(),
                                        e.message.toString(),
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        print(e)
                        progressDialog.dismiss()
                        requireActivity().runOnUiThread {
                            commonMethods.showMessageDialog(
                                requireContext(),
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
                requireActivity().runOnUiThread {
                    commonMethods.showMessageDialog(
                        requireContext(),
                        "No Internet....Please be connected to a working internet",
                        "Alert!",
                        "",
                        false
                    )
                }
            }
        }
    }

    private fun getLastTopUp(walletBalance: String,beneficiaryData: BeneficiaryData) {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(requireContext())) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                requestData["mobileNo"] =
                    PrefManager.getInstance(requireContext()).getString("MOBILE")
                service.lastTopUp(requestData).let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("get-balance API", response.toString())
                            Log.d("get-balance API response", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
                            val it = Gson().fromJson(
                                org.json.JSONObject(jsonString).toString(),
                                LastTopUpResult::class.java
                            )
                            if (it.status == true) {
                                withContext(Dispatchers.Main) {
                                    if (it?.data?.IsLastTopUpUnused == true) {
                                        val intent = Intent(
                                            requireContext(),
                                            FundTransferActivity::class.java
                                        )

                                        intent.putExtra("walletBalance", walletBalance)
                                        intent.putExtra("TopUpAmount", it.data?.TopUpAmount.toString())
                                        intent.putExtra("WalletTransNo", it.data?.WalletTransNo.toString())
                                        intent.putExtra("beneficiaryData", beneficiaryData)
                                        startActivity(intent)
                                    } else {
                                        requireActivity().runOnUiThread {
                                            commonMethods.showMessageDialog(
                                                requireContext(),
                                                it.message,
                                                "Error",
                                                "",
                                                false
                                            )
                                        }
                                    }

                                    /*walletBalance = it
                                    tvLimit.text = "Limit: ₹${it.data?.remainingCashDepositLimit?:0.0}"
                                    tvBalance.text = "Balance: ₹${it.data?.balance?:0.0}"*/
                                }
                            } else {
                                requireActivity().runOnUiThread {
                                    commonMethods.showMessageDialog(
                                        requireContext(),
                                        it.message,
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                try {
                                    commonMethods.showMessageDialog(
                                        requireContext(), org.json.JSONObject(
                                            response.errorBody()!!.charStream().readText().trim()
                                        ).getString("data").trim(), "Error", "", false
                                    )
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(
                                        requireContext(),
                                        e.message.toString(),
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        print(e)
                        progressDialog.dismiss()
                        requireActivity().runOnUiThread {
                            commonMethods.showMessageDialog(
                                requireContext(),
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
                requireActivity().runOnUiThread {
                    commonMethods.showMessageDialog(
                        requireContext(),
                        "No Internet....Please be connected to a working internet",
                        "Alert!",
                        "",
                        false
                    )
                }
            }
        }
    }


    //////DIALOGUE////////////
    private fun verifyBeneDialog(beneficiaryData: BeneficiaryData) {
        val dialog = Dialog(requireContext(), R.style.CustomDialogStyle)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.verify_dialog)
        val tvTitle: TextView = dialog.findViewById(R.id.tvTitle)
        val tvDes: TextView = dialog.findViewById(R.id.tvDes)

        tvTitle.text =
            "Are you sure you want to verify this beneficiary? A charge of ₹4 will be applied."

        val tvCancel: TextView = dialog.findViewById(R.id.tvCancel)
        val tvProceed: TextView = dialog.findViewById(R.id.tvProceed)
        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        tvProceed.setOnClickListener {
            dialog.dismiss()
            verifyBeneficiary(beneficiaryData)
        }
        dialog.show()
    }

    private fun deleteBeneDialog(beneficiaryData: BeneficiaryData) {
        val dialog = Dialog(requireContext(), R.style.CustomDialogStyle)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.verify_dialog)
        val tvTitle: TextView = dialog.findViewById(R.id.tvTitle)
        val tvDes: TextView = dialog.findViewById(R.id.tvDes)

        tvTitle.text =
            "Are you sure you want to permanently delete beneficiary " + beneficiaryData.beneficiaryName + "? This action cannot be undone."

        val tvCancel: TextView = dialog.findViewById(R.id.tvCancel)
        val tvProceed: TextView = dialog.findViewById(R.id.tvProceed)
        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        tvProceed.setOnClickListener {
            dialog.dismiss()
            deleteBeneficiary(beneficiaryData)
        }
        dialog.show()
    }

    private fun otpVerifyAddBeneficiary(ctx: Context?, data: AddBeneficiaryOtpData?) {
        val dialog = Dialog(ctx!!, R.style.CustomDialogStyle)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.otp_verify_beneficiary_pop_up)
        val tvDes: TextView = dialog.findViewById(R.id.tvOtpSubtitleVerifyBeneficiary)
        val tvCancel: TextView = dialog.findViewById(R.id.tvCancel)
        val tvProceed: TextView = dialog.findViewById(R.id.tvVerifyProceed)
        val tvResendOtpVerifyBeneficiary: TextView =
            dialog.findViewById(R.id.tvResendOtpVerifyBeneficiary)
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
                verifyOtp(otp, data?.requestNo!!)
            } else {
                Toast.makeText(ctx, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }
        otpView.setOtpCompletionListener { otp ->
            dialog.dismiss()
            verifyOtp(otp, data?.requestNo!!)
        }

        // Resend OTP logic
        tvResendOtpVerifyBeneficiary.setOnClickListener {
            reSendOtp(data?.requestNo!!)
        }

        // Start countdown timer 30 seconds
        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvResendOtpVerifyBeneficiary.text =
                    "${ctx.getString(R.string.resend_otp_message)} 00:${
                        String.format(
                            "%02d",
                            seconds
                        )
                    }"
            }

            override fun onFinish() {
                tvResendOtpVerifyBeneficiary.text = "${ctx.getString(R.string.resend_otp_text)}"
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
                        ctx,
                        R.color.colorPrimary
                    )
                )
            }
        }.start()

        dialog.show()
    }
}
