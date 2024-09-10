package com.lib.sps

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView

class KycActivity : AppCompatActivity(), OnClickListener {

    val commonMethods = CommonMethods()

    private lateinit var etMobileNo: EditText
    private lateinit var etAadhaarNo: EditText
    private lateinit var etPanNo: EditText
    private lateinit var etOtp: EditText
    private lateinit var tvResendOtp: TextView
    private lateinit var llConsent: LinearLayout
    private lateinit var cbConsent: CheckBox
    private lateinit var tvConsentContent: TextView
    private lateinit var spnConsentLanguage: Spinner
    private lateinit var spnDevices: Spinner
    private lateinit var tvSubmit: TextView

    private var strMobileNumber = ""
    private var strAadhaarNumber = ""
    private var strMaskedAadhaarNumber = ""
    private var strPanNumber = ""
    private var strOTP = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kyc)


        etMobileNo = findViewById(R.id.etMobileNo)
        etAadhaarNo = findViewById(R.id.etAadhaarNo)
        etPanNo = findViewById(R.id.etPanNo)
        etOtp = findViewById(R.id.etOtp)
        tvResendOtp = findViewById(R.id.tvResendOtp)
        llConsent = findViewById(R.id.llConsent)
        cbConsent = findViewById(R.id.cbConsent)
        tvConsentContent = findViewById(R.id.tvConsentContent)
        spnConsentLanguage = findViewById(R.id.spnConsentLanguage)
        spnDevices = findViewById(R.id.spnDevices)
        tvSubmit = findViewById(R.id.tvSubmit)

        tvSubmit.setOnClickListener(this)

        etMobileNo.setText("9253022366")

        etAadhaarNo.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                strAadhaarNumber = etAadhaarNo.text.toString()
                strMaskedAadhaarNumber = etAadhaarNo.text.toString()
                strMaskedAadhaarNumber = if (strMaskedAadhaarNumber.length == 12)

                    strMaskedAadhaarNumber.replaceRange(0, 8, "********")
                else {
                    strMaskedAadhaarNumber.replaceRange(0, 12, "************")
                }
                etAadhaarNo.setText(strMaskedAadhaarNumber)
            } else {
                // The EditText has gained focus
            }
        }


        val listLanguage = arrayOf("Select Language", "English", "Hindi")
        val languageAdapter = CustomDropDownAdapter(this@KycActivity, listLanguage)
        spnConsentLanguage.adapter = languageAdapter


        val listDevices = arrayOf(
            "Select Device",
            "Morpho",
            "Mantra",
            "Startek",
            "Aratek"
        )
        val devicesAdapter = CustomDropDownAdapter(this@KycActivity, listDevices)
        spnDevices.adapter = devicesAdapter


    }

    override fun onClick(v: View?) {
        if (v != null && v.id == R.id.tvSubmit) {
            val validationUtil = Validation()

            if (validationUtil.validateForm(
                    etMobile = etMobileNo,
                    etAadhaar = etAadhaarNo,
                    etPan = etPanNo,
                    etOtp = etOtp,
                    spnConsent = spnConsentLanguage,
                    cbConsent = cbConsent,
                    spnDevice = spnDevices,
                    tvConsentContent = tvConsentContent
                )
            ) {
                Toast.makeText(this@KycActivity, "Validated", Toast.LENGTH_LONG).show()
            }
        }
    }

}