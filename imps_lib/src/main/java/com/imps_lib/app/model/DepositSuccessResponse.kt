package com.lib.ppi_imps.model

import com.google.gson.annotations.SerializedName

data class DepositSuccessResponse(

    @SerializedName("status") var status: Boolean? = false,
    @SerializedName("message") var message: String? = "",
    @SerializedName("data") var data: DepositSuccessData? = DepositSuccessData()

)

data class DepositSuccessData(

    @SerializedName("RequestTransNo") var requestTransNo: String? = "",
    @SerializedName("MerchantTransNo") var merchantTransNo: String? = "",
    @SerializedName("PaycTransId") var paycTransId: String? = "",
    @SerializedName("Amount") var amount: Double? = 0.0,
    @SerializedName("Status") var status: String? = "",
    @SerializedName("Message") var message: String? = "",
    @SerializedName("Balance") var balance: String? = "",

    )
