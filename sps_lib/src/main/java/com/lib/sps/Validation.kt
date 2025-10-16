package com.lib.sps

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.snackbar.Snackbar
import com.lib.sps.model.ValidationData


class Validation {
    private val regexMobileNumber = "^[6-9]\\d*$"
    private val regexPanNumber = "^[A-Z]{5}[0-9]{4}[A-Z]{1}\$"

    fun validatePanForm(
        etMobile: EditText,
        etAadhaar: EditText,
        etPan: EditText,
        etOtp: EditText,
        spnConsent: Spinner,
        cbConsent: CheckBox,
        spnDevice: Spinner,
        tvConsentContent: TextView,
        strAadhaarNumber:String,
        strMobileNumber:String,
    ): Boolean {
        val isValidate = if (validateMobileNo(etMobile,strMobileNumber)) {
            if (validateOTP(etOtp)) {
                if (validatePan(etPan)) {
                    if (validateAadhaar(etAadhaar,strAadhaarNumber)) {
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

    fun validateForm60(
        etMobile: EditText,
        etAadhaar: EditText,
        etFatherName: EditText,
        etAgriculturalIncome: EditText,
        etOtherAgriculturalIncome: EditText,
        etOtp: EditText,
        spnConsent: Spinner,
        cbConsent: CheckBox,
        spnDevice: Spinner,
        tvConsentContent: TextView,
        strAadhaarNumber:String,
        strMobileNumber:String,
        validationData: ValidationData,
        etAcknowledgementNumber: EditText,
        etAcknowledgementNumberDate: EditText,
    ): Boolean {
        val isValidate = if (validateMobileNo(etMobile,strMobileNumber)) {
            if (validateOTP(etOtp)) {
                if(acknowledgementNumber(etAcknowledgementNumber,validationData)){
                    if(acknowledgementDate(etAcknowledgementNumberDate,validationData)){
                        if (validateFatherName(etFatherName)) {
                            if(validateAgriculturalIncome(etAgriculturalIncome,validationData)){
                                if(validateOtherAgriculturalIncome(etOtherAgriculturalIncome,etAgriculturalIncome,validationData)){
                                    if (validateAadhaar(etAadhaar, strAadhaarNumber)) {
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
                                }else{
                                    false
                                }
                            }else{
                                false
                            }
                        } else {
                            false
                        }
                    }else{
                        false
                    }
                }else{
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

    private fun validateFatherName(editText: EditText):Boolean{
        var isValid = true
        val mobileNumber: String = editText.text.toString().trim()
        if (mobileNumber.isEmpty()) {
            isValid = false;
            editText.error = "Please enter father's name"
        }else{
            editText.error = null
        }
        if (!isValid) editText.requestFocus()

        return isValid

    }
    private fun validateAgriculturalIncome(editText: EditText,validationData: ValidationData):Boolean{
        var isValid = true
        val inputText: String = editText.text.toString().trim()
        if (inputText.isEmpty()) {
            isValid = false;
            editText.error = "Please enter agricultural income"
        }else if(validationData.maxIncomeLimit!! < inputText.toLong()){
            isValid = false;
            editText.error = validationData.incomeValidation
        }else{
            editText.error = null
        }
        if (!isValid) editText.requestFocus()
        return isValid

    }
    private fun validateOtherAgriculturalIncome(editText: EditText,etAgriculturalIncome:EditText,validationData: ValidationData):Boolean{
        var isValid = true
        val inputText: String = editText.text.toString().trim()
        val etAgriculturalIncome: String = etAgriculturalIncome.text.toString().trim()
        if (inputText.isEmpty()) {
            isValid = false;
            editText.error = "Please enter other agricultural income"
        }else if(validationData.maxIncomeLimit!!<inputText.toLong()){
            isValid = false;
            editText.error = validationData.incomeValidation
        }else if(validationData.maxIncomeLimit!!<inputText.toLong()+etAgriculturalIncome.toLong()){
            isValid = false;
            editText.error = validationData.incomeValidation
        }else{
            editText.error = null
        }
        if (!isValid) editText.requestFocus()

        return isValid

    }
    private fun validateMobileNo(editText: EditText,strMobileNumber:String): Boolean {
        var isValid = true
        val mobileNumber: String = editText.text.toString().trim()

        if (mobileNumber.isEmpty()) {
            isValid = false;
            editText.error = "Enter mobile number"
        } else if (mobileNumber.length != 10) {
            isValid = false;
            editText.error = "Mobile number must be of 10 digits"
        } else if (!strMobileNumber.matches(Regex(regexMobileNumber))) {
            isValid = false;
            editText.error = "Enter valid mobile number"
        } else {
            editText.error = null
        }

        if (!isValid) editText.requestFocus()

        return isValid
    }


    fun validateAadhaar(editText: EditText,strAadhaarNumber:String): Boolean {
        var isValid = true
        val aadhaarNo = editText.text.toString().trim()

        if (aadhaarNo.isEmpty()) {
            isValid = false;
            editText.error = "Aadhaar Number/VID must be in valid format"
        } else if (aadhaarNo.length < 12) {
            isValid = false;
            editText.error = "Aadhaar number must be of 12 digits"
        } else if (aadhaarNo.length == 12 && !VerhoeffAlgorithm.validateVerhoeff(strAadhaarNumber)) {
            isValid = false;
            editText.error = "Invalid Aadhaar number"
        }else if(aadhaarNo.length == 16 && !VerhoeffAlgorithm.validateVerhoeff(strAadhaarNumber)){
            isValid = false;
            editText.error = "Invalid VID number"
        } else {
            editText.error = null
        }

        if (!isValid) editText.requestFocus()

        return isValid
    }


    fun validatePan(editText: EditText): Boolean {
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

    private fun acknowledgementNumber(editText: EditText,validationData: ValidationData):Boolean{
        var isValid = true
        val acknowledgementNo = editText.text.toString().trim()
        if (editText.visibility==View.VISIBLE && acknowledgementNo.isEmpty()) {
            isValid = false
            editText.error = "Please enter pan acknowledgement no"
        }else if(editText.visibility==View.VISIBLE && acknowledgementNo.length<validationData.panAckNoLengthMin!!.toInt() || acknowledgementNo.length>validationData.panAckNoLengthMax!!.toInt() ){
            isValid = false
            editText.error = "Please enter valid pan acknowledgement no"
        } else {
            editText.error = null
        }

        if (!isValid) editText.requestFocus()

        return isValid
    }

    private fun acknowledgementDate(editText: EditText,validationData: ValidationData):Boolean{
        var isValid = true
        val acknowledgementDate = editText.text.toString().trim()
        if (editText.visibility==View.VISIBLE && acknowledgementDate.isEmpty()) {
            isValid = false
            editText.error = "Please enter pan acknowledgement date"
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
            if(spnConsent.selectedView!=null)
            (spnConsent.selectedView as TextView).error = "Select consent language"
        }
        return isValid
    }

    private fun validateDevice(spnDevice: Spinner): Boolean {
        var isValid = true

        if (spnDevice.selectedItemPosition == -1) {
            isValid = false
            if(spnDevice.selectedView!=null)
            (spnDevice.selectedView as TextView).error = "Select device"
        }
        return isValid
    }

    private fun validateConsent(cbConsent: CheckBox, tvConsentContent: TextView): Boolean {
        val isValid = if (!cbConsent.isChecked) {
            tvConsentContent.error = "Mark consent"
            Snackbar.make(tvConsentContent, "Please mark consent", Snackbar.LENGTH_SHORT).show()
            false
        } else {
            true
        }
        return isValid
    }

}