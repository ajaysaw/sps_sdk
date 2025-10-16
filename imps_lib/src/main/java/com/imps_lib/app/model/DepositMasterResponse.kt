package com.imps_lib.app.model

import com.google.gson.annotations.SerializedName

data class DepositMasterResponse(

    @SerializedName("status")
    var status: Boolean? = false,

    @SerializedName("message")
    var message: String? = "",

    @SerializedName("data")
    var data: DepositData? = DepositData()
)

data class DepositData(

    @SerializedName("DepositRateSlab")
    var depositRateSlab: ArrayList<DepositRateSlab>? = arrayListOf(),

    @SerializedName("LastTopUpDetails")
    var lastTopUpDetails: LastTopUpDetails? = LastTopUpDetails()
)

data class DepositRateSlab(

    @SerializedName("min_amount")
    var minAmount: Double? = 0.0,

    @SerializedName("max_amount")
    var maxAmount: Double? = 0.0,

    @SerializedName("rate_mode")
    var rateMode: String? = "",

    @SerializedName("rate_value")
    var rateValue: Double? = 0.0,

    @SerializedName("indirect_tax_mode")
    var indirectTaxMode: String? = "",

    @SerializedName("indirect_tax_type")
    var indirectTaxType: String? = "",

    @SerializedName("indirect_tax_rate")
    var indirectTaxRate: String? = ""
)

data class LastTopUpDetails(

    @SerializedName("TopUpAmount")
    var topUpAmount: String? = "",

    @SerializedName("WalletTransNo")
    var walletTransNo: String? = "",

    @SerializedName("IsLastTopUpUnused")
    var isLastTopUpUnused: Boolean? = false,
)
