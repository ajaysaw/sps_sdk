package com.lib.sps.model

import com.google.gson.annotations.SerializedName

data class GetStatusResponse(
    @SerializedName("message") var message: String? = "",
    @SerializedName("esign_data") var eSignData: EsignDataResponse? = EsignDataResponse(),
    @SerializedName("error_message") var errorMessage: String? = "",
)

data class EsignDataResponse(
    @SerializedName("message") var message: String? = "",
    @SerializedName("status") var status: String? = "",
    @SerializedName("Form60Status") var form60Status: String? = "",
    @SerializedName("Form60EsignStatus") var form60EsignStatus: String? = "",
)