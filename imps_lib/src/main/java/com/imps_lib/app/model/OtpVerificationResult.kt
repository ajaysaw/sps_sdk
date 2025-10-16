package com.imps_lib.app.model

import com.google.gson.annotations.SerializedName

data class OtpVerificationResult(

    @SerializedName("status") var status: Boolean? = false,
    @SerializedName("message") var message: String? = "",
    @SerializedName("data") var data: OtpVerificationData? = OtpVerificationData()

)

data class OtpVerificationData(

    @SerializedName("otpStatus") var otpStatus: String? = "",
    @SerializedName("mpinStatus") var mpinStatus: String? = ""

)
