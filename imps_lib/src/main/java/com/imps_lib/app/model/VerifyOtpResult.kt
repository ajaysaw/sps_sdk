package com.imps_lib.app.model

import com.google.gson.annotations.SerializedName

data class VerifyOtpResult(

    @SerializedName("status") var status: Boolean? = false,
    @SerializedName("message") var message: String? = "",
    @SerializedName("data") var data: VerifyOtpData? = VerifyOtpData()

)

data class VerifyOtpData(

    @SerializedName("otpStatus") var otpStatus: String? = "",
    @SerializedName("mpinStatus") var mpinStatus: String? = "",
    @SerializedName("mobileNo") var mobileNo: String? = "",
    @SerializedName("OtpVerify") var OtpVerify: String? = "",
    @SerializedName("Response") var Response: String? = "",
    @SerializedName("Message") var Message: String? = "",

)
