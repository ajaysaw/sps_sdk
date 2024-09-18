package com.lib.sps.model

import com.google.gson.annotations.SerializedName

data class DoKycResponse (
    @SerializedName("message") var message: String? = "",
    @SerializedName("kyc_response") var eKycResponse: KycResponse? = KycResponse(),
    @SerializedName("error_message") var errorMessage: String? = "",
    )

data class KycResponse(
    @SerializedName("message") var eKycMessage: String? = "",
    @SerializedName("status") var status: String? = "",
    @SerializedName("redirectionUrl") var redirectionUrl: String? = ""
)