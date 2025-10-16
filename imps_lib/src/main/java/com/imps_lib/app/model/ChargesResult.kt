package com.lib.ppi_imps.model

import com.google.gson.annotations.SerializedName

data class ChargesResult(

    @SerializedName("status") var status: Boolean? = false,
    @SerializedName("message") var message: String? = "",
    @SerializedName("data") var data: ChargesResultData? = ChargesResultData()

)

data class ChargesResultData(

    @SerializedName("amount") var amount: Int? = 0,
    @SerializedName("charges") var charges: Double? = 0.0,
    @SerializedName("gstAmount") var gstAmount: Double? = 0.0,
    @SerializedName("finalAmount") var finalAmount: Double? = 0.0,

    )
