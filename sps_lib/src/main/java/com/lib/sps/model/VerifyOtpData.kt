package com.lib.sps.model

import com.google.gson.annotations.SerializedName

data class VerifyOtpData (
    @SerializedName("message") var message: String? = "",
    @SerializedName("kyc_response") var eKycOtpData: VerifyOtpResponse? = VerifyOtpResponse(),
    @SerializedName("error_message") var errorMessage: String? = "",
)

data class VerifyOtpResponse(
    @SerializedName("mssg") var mssg: String? = "",
)


