package com.imps_lib.app.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.imps_lib.app.ApiErrorHandler
import com.imps_lib.app.CommonMethods
import com.imps_lib.app.Coroutines
import com.imps_lib.app.GlobalData
import com.imps_lib.app.R
import com.imps_lib.app.model.AddBeneficiaryOtp
import com.imps_lib.app.model.AddBeneficiaryOtpData
import com.imps_lib.app.model.BankListMaster
import com.imps_lib.app.model.BankMaster
import com.imps_lib.app.model.MetaData
import com.imps_lib.app.model.VerifyBeneficiary
import com.imps_lib.app.model.VerifyOtpResult
import com.imps_lib.app.network.ApiClient
import com.imps_lib.app.network.WebInterface
import com.mukeshsolanki.OtpView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale

class AddBeneficiaryFragment : Fragment(), OnClickListener {
    private lateinit var linerLayoutBtnVerify: LinearLayout
    private lateinit var etBeneficiaryName: EditText
    private lateinit var autoCompleteBank: AutoCompleteTextView
    private lateinit var spnAccountType: AutoCompleteTextView
    private lateinit var etIfscCode: EditText
    private lateinit var etBeneficiaryAccountNumber: EditText
    private lateinit var etConfirmBeneficiaryAccountNumber: EditText
    private lateinit var tvVerify: TextView
    private lateinit var tvCharge: TextView
    private lateinit var tvSubmit: TextView
    private lateinit var tvResendOtpVerifyBeneficiary: TextView
    private val commonMethods = CommonMethods()
    private var job: Job? = null
    private lateinit var progressDialog: Dialog
    var bankList = ArrayList<BankMaster>()
    private lateinit var adapter: ArrayAdapter<BankMaster>
    private var selectedBank: BankMaster? = null
    private lateinit var accountTypeAdapter: ArrayAdapter<String>
    private var accountTypeList = ArrayList<String>()
    private var accountType: String? = null
    private var accountNumber: String? = ""
    private var metadata: MetaData? = null
    private var isEditable: Boolean = true


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_add_beneficiary, container, false)
        linerLayoutBtnVerify = rootView.findViewById(R.id.linerLayoutBtnVerify)
        etBeneficiaryName = rootView.findViewById(R.id.etBeneficiaryName)
        autoCompleteBank = rootView.findViewById(R.id.autoCompleteBank)
        spnAccountType = rootView.findViewById(R.id.autoCompleteAccountType)
        etIfscCode = rootView.findViewById(R.id.etIfscCode)
        etBeneficiaryAccountNumber = rootView.findViewById(R.id.etBeneficiaryAccountNumber)
        etConfirmBeneficiaryAccountNumber =
            rootView.findViewById(R.id.etConfirmBeneficiaryAccountNumber)
        progressDialog = commonMethods.progressDialog(requireContext())
        tvVerify = rootView.findViewById(R.id.tvVerify)
        tvCharge = rootView.findViewById(R.id.tvCharge)
        tvSubmit = rootView.findViewById(R.id.tvSubmit)
        tvSubmit.setOnClickListener(this)
        tvVerify.setOnClickListener(this)
        adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, bankList)
        autoCompleteBank.setAdapter(adapter)
        autoCompleteBank.setOnItemClickListener { parent, _, position, _ ->
            selectedBank = parent.getItemAtPosition(position) as BankMaster
            if(selectedBank?.bankIfscCode!=null){
                etIfscCode.setText(selectedBank?.bankIfscCode.toString())
            }
            if (!isEditable) {
                linerLayoutBtnVerify.visibility = VISIBLE
                isEditable = true
            }
            Log.d("SelectedBank", "ID: ${selectedBank!!.id}, Name: ${selectedBank!!.bankName}")
        }
        accountTypeAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, accountTypeList)
        spnAccountType.setAdapter(accountTypeAdapter)
        spnAccountType.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()
                spnAccountType.showDropDown()
            }
            false
        }
        spnAccountType.setOnItemClickListener { parent, _, position, _ ->
            accountType = parent.getItemAtPosition(position).toString()
            if (!isEditable) {
                linerLayoutBtnVerify.visibility = VISIBLE
                isEditable = true
            }
            Log.d("Account", "Type: ${selectedBank}")
        }

        etBeneficiaryAccountNumber.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            private var previousLength = 0
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousLength = s?.length ?: 0
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isEditable) {
                    linerLayoutBtnVerify.visibility = VISIBLE
                    isEditable = true
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val input = s.toString()
                if (input.length > previousLength) {
                    val newChar = input.last().toString()
                    accountNumber += newChar
                } else if (input.length < previousLength && accountNumber!!.isNotEmpty()) {
                    accountNumber = accountNumber!!.dropLast(1)
                }
                val masked = "*".repeat(accountNumber!!.length)
                etBeneficiaryAccountNumber.setText(masked)
                etBeneficiaryAccountNumber.setSelection(masked.length)

                isEditing = false
            }
        })
        etBeneficiaryName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isEditable) {
                    linerLayoutBtnVerify.visibility = VISIBLE
                    isEditable = true
                    etBeneficiaryName.isEnabled=true
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        etConfirmBeneficiaryAccountNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isEditable) {
                    linerLayoutBtnVerify.visibility = VISIBLE
                    isEditable = true
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        etIfscCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isEditable) {
                    linerLayoutBtnVerify.visibility = VISIBLE
                    isEditable = true
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        return rootView
    }

    override fun onResume() {
        super.onResume()
        getBankList()
        isEditable = true
        linerLayoutBtnVerify.visibility = VISIBLE
        etBeneficiaryName.setText("")
        accountNumber = ""
        etBeneficiaryAccountNumber.setText("")
        etConfirmBeneficiaryAccountNumber.setText("")
        etIfscCode.setText("")
        selectedBank = null

    }

    override fun onClick(v: View?) {
        if (v != null && v.id == R.id.tvSubmit) {
            tvSubmit.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.button_drawable)
            tvSubmit.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tvVerify.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.border_button_drawable)
            tvVerify.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            if (checkValidation()) addBeneficiary() //call to api


        } else if (v != null && v.id == R.id.tvVerify) {
            tvVerify.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.button_drawable)
            tvVerify.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tvSubmit.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.border_button_drawable)
            tvSubmit.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            if (checkValidation()) verifyBeneficiary(requireContext()) //show confirmation pop-up
        }
    }

    private fun checkValidation(): Boolean {
        val confirmAccountNumber = etConfirmBeneficiaryAccountNumber.text.toString().trim()
        if (selectedBank == null) {
            autoCompleteBank.error = "Please select bank"
            return false
        } else if (accountType == null) {
            spnAccountType.error = "Please select an account type"
            return false
        } else if (accountNumber!!.isEmpty()) {
            etBeneficiaryAccountNumber.error = "Please enter beneficiary account no."
            return false
        } else if (confirmAccountNumber.isEmpty()) {
            etConfirmBeneficiaryAccountNumber.error = "Please enter confirm beneficiary account no."
            return false
        } else if (etIfscCode.text.isEmpty()) {
            etIfscCode.error = "Please entre IFSC code"
            return false
        } else if (etBeneficiaryName.text.isEmpty()) {
            etBeneficiaryName.error = "Please entre beneficiary name"
            return false
        } else if (accountNumber != confirmAccountNumber) {
            Toast.makeText(requireContext(), "Account number does not match", Toast.LENGTH_SHORT)
                .show()
            return false
        } else if (!commonMethods.isValidIFSC(etIfscCode.text.toString())) {
            etIfscCode.error = "Invalid IFSC code"
            return false
        } else {
            return true
        }
    }

    private fun verifyBeneficiary(ctx: Context?) {
        val dialog = Dialog(ctx!!, R.style.CustomDialogStyle)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.verify_beneficiary_pop_up)
        val tvTitle: TextView = dialog.findViewById(R.id.tvTitle)
        val tvDes: TextView = dialog.findViewById(R.id.tvDes)
        val tvCancel: TextView = dialog.findViewById(R.id.tvCancel)
        val tvProceed: TextView = dialog.findViewById(R.id.tvProceed)
        tvDes.text = metadata?.confirmationText
        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        tvProceed.setOnClickListener {
            dialog.dismiss()
            checkBeneficiary() //call to api
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
        startOtpTimer()
        dialog.show()
    }

    private fun startOtpTimer() {
        // Start countdown timer 30 seconds
        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvResendOtpVerifyBeneficiary.text =
                    "${requireContext().getString(R.string.resend_otp_message)} 00:${
                        String.format(
                            "%02d",
                            seconds
                        )
                    }"
                tvResendOtpVerifyBeneficiary.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray
                    )
                )
            }

            override fun onFinish() {
                tvResendOtpVerifyBeneficiary.text =
                    "${requireContext().getString(R.string.resend_otp_text)}"
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
                tvResendOtpVerifyBeneficiary.setTextColor(Color.parseColor("#1DA1F2"))
            }
        }.start()
    }

    @SuppressLint("SetTextI18n")
    private fun getBankList() {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(requireContext())) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                service.getBankMast(requestData).let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("get-bank list API", response.toString())
                            Log.d("get-bank list API response", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
                            val it = Gson().fromJson(
                                JSONObject(jsonString).toString(),
                                BankListMaster::class.java
                            )
                            if (it.status) {
                                withContext(Dispatchers.Main) {
                                    metadata = it.data.metaData
                                    tvCharge.text = getString(R.string.charges) + " " + getString(R.string.rupee_symbol) + "${metadata!!.chargesAmount}"
                                    bankList.clear()
                                    bankList.addAll(it.data.bankMaster)
                                    adapter.notifyDataSetChanged()
                                    autoCompleteBank.setText("",false)
                                    accountTypeList.clear()
                                    accountTypeList.addAll(metadata!!.accountType)
                                    accountTypeAdapter.notifyDataSetChanged()
                                    autoCompleteBank.setOnTouchListener { _, event ->
                                        if (event.action == MotionEvent.ACTION_UP) {
                                            autoCompleteBank.performClick() // Accessibility-safe focus
                                            autoCompleteBank.requestFocus()
                                            autoCompleteBank.postDelayed({
                                                autoCompleteBank.showDropDown()
                                            }, 400)
                                        }
                                        false
                                    }
                                    autoCompleteBank.threshold = 0
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
                                // ❌ Centralized error handling
                                val errorMessage = ApiErrorHandler.getErrorMessage(response)
                                Log.e("API_ERROR", errorMessage)
                                commonMethods.showMessageDialog(
                                    requireContext(),
                                    errorMessage,
                                    "Error",
                                    "",
                                    false
                                )
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

    private fun addBeneficiary() {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(requireContext())) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                requestData["mobileNo"] = GlobalData.mobileNumber
                requestData["bankId"] = selectedBank?.id.toString()
                requestData["beneficiaryName"] = etBeneficiaryName.text.toString()
                requestData["accountNo"] = accountNumber!!
                requestData["accountType"] = accountType.toString()
                requestData["ifsc"] = etIfscCode.text.toString()
                service.addBeneficiary(requestData).let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("add beneficiary API", response.toString())
                            Log.d("add beneficiary  response", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
                            val it = Gson().fromJson(
                                JSONObject(jsonString).toString(),
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
                                // ❌ Centralized error handling
                                val errorMessage = ApiErrorHandler.getErrorMessage(response)
                                Log.e("API_ERROR", errorMessage)
                                commonMethods.showMessageDialog(
                                    requireContext(),
                                    errorMessage,
                                    "Error",
                                    "",
                                    false
                                )
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

    private fun checkBeneficiary() {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(requireContext())) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                requestData["mobileNo"] = GlobalData.mobileNumber
                requestData["bankId"] = selectedBank?.id.toString()
                requestData["beneficiaryName"] = etBeneficiaryName.text.toString()
                requestData["accountNo"] = accountNumber!!
                requestData["accountType"] = accountType.toString()
                requestData["ifsc"] = etIfscCode.text.toString()
                service.verifyBeneficiary(requestData).let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("verify beneficiary API", response.toString())
                            Log.d("verify beneficiary  response", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
                            val it = Gson().fromJson(
                                JSONObject(jsonString).toString(),
                                VerifyBeneficiary::class.java
                            )
                            if (it.status) {
                                withContext(Dispatchers.Main) {
                                    isEditable = false
                                    etBeneficiaryName.setText(it.data?.accountHolderName)
                                    etBeneficiaryName.isEnabled=false
                                    linerLayoutBtnVerify.visibility = GONE
                                    tvSubmit.background = ContextCompat.getDrawable(
                                        requireContext(),
                                        R.drawable.button_drawable
                                    )
                                    tvSubmit.setTextColor(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        )
                                    )
                                    tvVerify.background = ContextCompat.getDrawable(
                                        requireContext(),
                                        R.drawable.border_button_drawable
                                    )
                                    tvVerify.setTextColor(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.black
                                        )
                                    )
                                    requireActivity().runOnUiThread {
                                        commonMethods.showMessageDialog(
                                            requireContext(),
                                            it.data?.message,
                                            it.data?.status,
                                            "",
                                            false
                                        )
                                    }
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
                                // ❌ Centralized error handling
                                val errorMessage = ApiErrorHandler.getErrorMessage(response)
                                Log.e("API_ERROR", errorMessage)
                                commonMethods.showMessageDialog(
                                    requireContext(),
                                    errorMessage,
                                    "Error",
                                    "",
                                    false
                                )
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
                                    if (it.data?.Response.toString().trim()
                                            .uppercase(Locale.ROOT) == "SUCCESS"
                                    ) {
                                        etBeneficiaryName.setText("")
                                        accountNumber = ""
                                        etBeneficiaryAccountNumber.setText("")
                                        etConfirmBeneficiaryAccountNumber.setText("")
                                        etIfscCode.setText("")
                                        selectedBank = null
                                        autoCompleteBank.setText("")
                                        isEditable = true
                                        etBeneficiaryName.isEnabled=true
                                        linerLayoutBtnVerify.visibility = VISIBLE
                                        commonMethods.showMessageDialog(
                                            requireContext(),
                                            it.message,
                                            "Success",
                                            "",
                                            false
                                        )
                                    } else {
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
                                // ❌ Centralized error handling
                                val errorMessage = ApiErrorHandler.getErrorMessage(response)
                                Log.e("API_ERROR", errorMessage)
                                commonMethods.showMessageDialog(
                                    requireContext(),
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
                                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT)
                                        .show()
                                    startOtpTimer()
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
                                // ❌ Centralized error handling
                                val errorMessage = ApiErrorHandler.getErrorMessage(response)
                                Log.e("API_ERROR", errorMessage)
                                commonMethods.showMessageDialog(
                                    requireContext(),
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
}