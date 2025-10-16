package com.imps_lib.app.model
import com.google.gson.annotations.SerializedName

data class FundTransferResult(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: FundTransferResultData?
)


data class FundTransferResultData(
    @SerializedName("RequestTransNo") val requestTransNo: String?,
    @SerializedName("MerchantTransNo") val merchantTransNo: String?,
    @SerializedName("PaycTransId") val paycTransId: String?,
    @SerializedName("BankRrn") val bankRrn: String?,
    @SerializedName("Amount") val amount: String?,
    @SerializedName("Status") val status: String?,
    @SerializedName("Message") val message: String?,
    @SerializedName("Balance") val balance: String?,
)
