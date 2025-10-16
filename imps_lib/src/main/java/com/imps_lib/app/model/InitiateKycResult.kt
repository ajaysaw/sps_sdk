package com.imps_lib.app.model

import com.google.gson.annotations.SerializedName

data class InitiateKycResult(

    @SerializedName("status") var status: Boolean? = false,
    @SerializedName("message") var message: String? = "",
    @SerializedName("data") var data: InitiateKycData? = InitiateKycData()

)

data class InitiateKycData(
    @SerializedName("requestTransNo") var TopUpAmount: String? = "",
    @SerializedName("merchantTransNo") var WalletTransNo: String? = "",
    @SerializedName("proceedEkyc") var IsLastTopUpUnused: Boolean? = false
)
