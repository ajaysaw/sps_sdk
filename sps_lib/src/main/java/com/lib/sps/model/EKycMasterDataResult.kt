package com.example.example

import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap


data class EKycMasterDataResult(

    @SerializedName("message") var message: String? = null,
    @SerializedName("ekyc_master_data") var ekycMasterData: EkycMasterData? = EkycMasterData(),
    @SerializedName("error_message") var errorMessage: String? = null,
)

data class EkycMasterData(

    @SerializedName("ekyc_device_list") var ekycDeviceList: ArrayList<EkycDeviceList> = arrayListOf(),
    @SerializedName("ekyc_consent") var ekycConsent: ArrayList<ConsentLanguage> = arrayListOf(),
    @SerializedName("implement_type") var implementType: String? = null,
    @SerializedName("url_list") var urlList: UrlList? = UrlList(),
    @SerializedName("ekyc_otp_msg") var ekycOtpMsg: String? = "",
    @SerializedName("ekyc_token") var ekycToken: String? = ""

)

data class EkycDeviceList(

    @SerializedName("deviceName") var deviceName: String? = null,
    @SerializedName("deviceCode") var deviceCode: String? = null,
    @SerializedName("packageName") var packageName: String? = null,
    @SerializedName("errorMessage") var errorMessage: String? = null,
    @SerializedName("pidBlockType") var pidBlockType: Int? = 0,
    @SerializedName("isBiometric") var isBiometric: Boolean? = true,
    @SerializedName("pidBlockNodes") var pidBlockNodes: LinkedTreeMap<String, Any>

)

data class PidBlockNodes(

    @SerializedName("ver") var ver: String? = null,
    @SerializedName("fCount") var fCount: String? = null,
    @SerializedName("iCount") var iCount: String? = null,
    @SerializedName("iType") var iType: String? = null,
    @SerializedName("pCount") var pCount: String? = null,
    @SerializedName("pType") var pType: String? = null,
    @SerializedName("pidVer") var pidVer: String? = null,
    @SerializedName("timeout") var timeout: String? = null,
    @SerializedName("otp") var otp: String? = null,
    @SerializedName("wadh") var wadh: String? = null,
    @SerializedName("posh") var posh: String? = null,
    @SerializedName("env") var env: String? = null,
    @SerializedName("fType") var fType: String? = null,
    @SerializedName("format") var format: String? = null

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
)