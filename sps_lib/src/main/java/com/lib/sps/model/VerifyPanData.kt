package com.lib.sps.model

import com.google.gson.annotations.SerializedName

class VerifyPanData(
    @SerializedName("message") var message: String? = "",
    @SerializedName("kyc_response") var eKycOtpData: VerifyPanResponse? = VerifyPanResponse(),
    @SerializedName("error_message") var errorMessage: String? = "",
)

data class VerifyPanResponse(
    @SerializedName("mssg") var mssg: String? = "",
    @SerializedName("panName") var panName: String? = "",
)