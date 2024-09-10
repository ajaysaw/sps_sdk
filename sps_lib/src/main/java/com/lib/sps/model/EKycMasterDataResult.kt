package com.example.example

import com.google.gson.annotations.SerializedName


data class EKycMasterDataResult(

    @SerializedName("message") var message: String? = null,
    @SerializedName("ekyc_master_data") var ekycMasterData: EkycMasterData? = EkycMasterData()

)

data class EkycMasterData(

    @SerializedName("ekyc_device_list") var ekycDeviceList: ArrayList<EkycDeviceList> = arrayListOf(),
    @SerializedName("ekyc_consent") var ekycConsent: String? = null,
    @SerializedName("implement_type") var implementType: String? = null,
    @SerializedName("url_list") var urlList: UrlList? = UrlList()

)

data class EkycDeviceList(

    @SerializedName("name") var name: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("pidBlockNodes") var pidBlockNodes: PidBlockNodes? = PidBlockNodes()

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