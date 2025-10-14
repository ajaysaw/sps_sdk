package com.imps_lib.app.model

import com.google.gson.annotations.SerializedName

data class VerifyBeneficiary(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: VerifyBeneficiaryData?
)

data class VerifyBeneficiaryData(
    @SerializedName("RequestTransNo") val requestTransNo: String?,
    @SerializedName("MerchantTransNo") val merchantTransNo: String?,
    @SerializedName("accountHolderName") val accountHolderName: String?,
    @SerializedName("txnRefNo") val txnRefNo: String?,
    @SerializedName("Status") val status: String?,
    @SerializedName("Message") val message: String?
)