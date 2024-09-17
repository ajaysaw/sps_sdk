package com.lib.sps.model

import com.google.gson.annotations.SerializedName

data class ResendOtpData (
    @SerializedName("message") var message: String? = "",
    @SerializedName("ekyc_otp_data") var eKycOtpData: EkycOtpData? = EkycOtpData(),
    @SerializedName("error_message") var errorMessage: String? = "",
)

data class EkycOtpData(
    @SerializedName("ekyc_otp_msg") var eKycOtpMsg: String? = "",
    @SerializedName("ekyc_token") var eKycToken: String? = ""
)