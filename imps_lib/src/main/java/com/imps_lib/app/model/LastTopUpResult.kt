package com.lib.ppi_imps.model

import com.google.gson.annotations.SerializedName

data class LastTopUpResult(

    @SerializedName("status") var status: Boolean? = false,
    @SerializedName("message") var message: String? = "",
    @SerializedName("data") var data: LastTopUpData? = LastTopUpData()

)

data class LastTopUpData(
    @SerializedName("TopUpAmount") var TopUpAmount: String? = "",
    @SerializedName("WalletTransNo") var WalletTransNo: String? = "",
    @SerializedName("IsLastTopUpUnused") var IsLastTopUpUnused: Boolean? = false
)
