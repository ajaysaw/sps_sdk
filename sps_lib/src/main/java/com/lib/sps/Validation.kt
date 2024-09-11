package com.lib.sps

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService


class Validation {
    private val regexMobileNumber = "^[6-9]\\d*$"
    private val regexPanNumber = "^[A-Z]{5}[0-9]{4}[A-Z]{1}\$"

    fun validateForm(
        etMobile: EditText,
        etAadhaar: EditText,
        etPan: EditText,
        etOtp: EditText,
        spnConsent: Spinner,
        cbConsent: CheckBox,
        spnDevice: Spinner,
        tvConsentContent: TextView,
        strAadhaarNumber:String,

    ): Boolean {
        val isValidate = if (validateMobileNo(etMobile)) {
            if (validateAadhaar(etAadhaar,strAadhaarNumber)) {
                if (validatePan(etPan)) {
                    if (validateOTP(etOtp)) {
                        if (validateConsentLanguage(spnConsent)) {
                            if (validateDevice(spnDevice)) {
                                validateConsent(
                                    cbConsent = cbConsent,
                                    tvConsentContent = tvConsentContent
                                )
                            } else {
                                false
                            }
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                } else {
                    false
                }
            } else {
                false
            }
        } else {
            false
        }

        return isValidate
    }

    private fun validateMobileNo(editText: EditText): Boolean {
        var isValid = true
        val mobileNumber: String = editText.text.toString().trim()

        if (mobileNumber.isEmpty()) {
            isValid = false;
            editText.error = "Enter mobile number"
        } else if (mobileNumber.length != 10) {
            isValid = false;
            editText.error = "Mobile number must be of 10 digits"
        } else if (!mobileNumber.matches(Regex(regexMobileNumber))) {
            isValid = false;
            editText.error = "Enter valid mobile number"
        } else {
            editText.error = null
        }

        if (!isValid) editText.requestFocus()

        return isValid
    }


    private fun validateAadhaar(editText: EditText,strAadhaarNumber:String): Boolean {
        var isValid = true
        val aadhaarNo = editText.text.toString().trim()

        if (aadhaarNo.isEmpty()) {
            isValid = false;
            editText.error = "Enter Aadhaar number"
        } else if (aadhaarNo.length < 12) {
            isValid = false;
            editText.error = "Aadhaar number must be of 12 digits"
        } else if (aadhaarNo.length == 12 && !VerhoeffAlgorithm.validateVerhoeff(strAadhaarNumber)) {
            isValid = false;
            editText.error = "Invalid Aadhaar number"
        } else {
            editText.error = null
        }

        if (!isValid) editText.requestFocus()

        return isValid
    }


    private fun validatePan(editText: EditText): Boolean {
        var isValid = true
        val panNo = editText.text.toString().trim()

        if (panNo.isEmpty()) {
            isValid = false
            editText.error = "Enter PAN number"
        } else if (!panNo.matches(Regex(regexPanNumber))) {
            isValid = false;
            editText.error = "Invalid PAN number"
        } else {
            editText.error = null
        }

        if (!isValid) editText.requestFocus()

        return isValid
    }

    private fun validateOTP(editText: EditText): Boolean {
        var isValid = true
        val otp = editText.text.toString().trim()

        if (otp.isEmpty()) {
            isValid = false
            editText.error = "Enter OTP"
        } else {
            editText.error = null
        }

        if (!isValid) editText.requestFocus()

        return isValid
    }

    private fun validateConsentLanguage(spnConsent: Spinner): Boolean {
        var isValid = true

        if (spnConsent.selectedItemPosition == -1) {
            isValid = false
            (spnConsent.selectedView as TextView).error = "Select consent language"

        }

        return isValid
    }

    private fun validateDevice(spnDevice: Spinner): Boolean {
        var isValid = true

        if (spnDevice.selectedItemPosition == -1) {
            isValid = false
            (spnDevice.selectedView as TextView).error = "Select device"

        }

        return isValid
    }

    private fun validateConsent(cbConsent: CheckBox, tvConsentContent: TextView): Boolean {
        val isValid = if (!cbConsent.isChecked) {
            tvConsentContent.error = "Mark consent"
            false
        } else {
            true
        }

        return isValid
    }

}