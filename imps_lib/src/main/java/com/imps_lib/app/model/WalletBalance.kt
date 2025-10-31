package com.imps_lib.app.model

import com.google.gson.annotations.SerializedName

data class WalletBalance(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: WalletData?
)

data class WalletData(
    @SerializedName("MobileNo") val mobileNo: String?,
    @SerializedName("Balance") val balance: String?,
    @SerializedName("RemainingCashDepositLimit") val remainingCashDepositLimit: Int?,
    @SerializedName("RemainingRechargeLimit") val remainingRechargeLimit: Int?,
    @SerializedName("MarqueeMessage") val marqueeMessage: String?
)
