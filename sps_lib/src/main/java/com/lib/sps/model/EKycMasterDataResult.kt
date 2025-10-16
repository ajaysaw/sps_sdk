package com.lib.sps.model

import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap


data class EKycMasterDataResult(

    @SerializedName("message") var message: String? = "",
    @SerializedName("ekyc_master_data") var ekycMasterData: EkycMasterData? = EkycMasterData(),
    @SerializedName("error_message") var errorMessage: String? = "",
    @SerializedName("kyc_status") var status: String? = "",
)

data class EkycMasterData(

    @SerializedName("authentication_options") var authenticationOption: ArrayList<String> = arrayListOf(),
    @SerializedName("ekyc_device_list") var ekycDeviceList: ArrayList<EkycDeviceList> = arrayListOf(),
    @SerializedName("ekyc_consent") var ekycConsent: ArrayList<ConsentLanguage> = arrayListOf(),
    @SerializedName("implement_type") var implementType: String? = null,
    @SerializedName("url_list") var urlList: UrlList? = UrlList(),
    @SerializedName("ekyc_otp_msg") var ekycOtpMsg: String? = "",
    @SerializedName("ekyc_token") var ekycToken: String? = "",
    @SerializedName("mobile_no") var mobileNo: String? = "",
    @SerializedName("doc_proof_type") var docProofType: ArrayList<DocProofType> = arrayListOf(),
    @SerializedName("pan_applied") var panApplied: ArrayList<PanApplied> = arrayListOf(),
    @SerializedName("validation_list") var validationData: ValidationData? = ValidationData(),
    @SerializedName("isScreenCaptureRestricted") var isScreenCaptureRestricted: Boolean? = false,

)

data class EkycDeviceList(

    @SerializedName("deviceName") var deviceName: String? = null,
    @SerializedName("deviceCode") var deviceCode: String? = null,
    @SerializedName("packageName") var packageName: String? = null,
    @SerializedName("errorMessage") var errorMessage: String? = null,
    @SerializedName("pidBlockType") var pidBlockType: Int? = 0,
    @SerializedName("isBiometric") var isBiometric: Boolean? = true,
    @SerializedName("deviceType") var deviceType: String? = "",
    @SerializedName("pidBlockNodes") var pidBlockNodes: LinkedTreeMap<String, Any>

)
data class UrlList(

    @SerializedName("BASE_URL") var BASEURL: String? = null,
    @SerializedName("SEND_OTP") var SENDOTP: String? = null,
    @SerializedName("RESEND_OTP") var RESENDOTP: String? = null,
    @SerializedName("DO_KYC") var DOKYC: String? = null

)

data class ConsentLanguage(
    @SerializedName("Language") var language: String? = null,
    @SerializedName("content") var content: String? = null,
    @SerializedName("content1") var less_content: String? = null,
    @SerializedName("Audio") var audio: String? = null,
)

data class DocProofType(
    @SerializedName("key") var key: String? = null,
    @SerializedName("value") var value: String? = null,
)

data class PanApplied(
    @SerializedName("key") var key: String? = null,
    @SerializedName("value") var value: String? = null,
    @SerializedName("default_selection") var defaultSelection: String? = null,
)

data class ValidationData(
    @SerializedName("max_income_limit") var maxIncomeLimit: Long? = 0,
    @SerializedName("income_validation") var incomeValidation: String? = "",
    @SerializedName("pan_ack_date_format") var panAckDateFormat: String? = "dd-MM-yyyy",
    @SerializedName("pan_ack_date_duration") var panAckDateDuration: String? = "30",
    @SerializedName("pan_ack_no_length_min") var panAckNoLengthMin: String? = "10",
    @SerializedName("pan_ack_no_length_max") var panAckNoLengthMax: String? = "20",
    @SerializedName("ekyc_charges_msg") var eKycChargesMsg: EkycChargesMsg? = EkycChargesMsg(),
)

data class EkycChargesMsg(
    @SerializedName("PAN") var panMsg: String? = "",
    @SerializedName("FORM-60") var form60Msg: String? = "",
)