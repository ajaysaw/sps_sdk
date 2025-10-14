package com.imps_lib.app.model
import com.google.gson.annotations.SerializedName

data class AddBeneficiaryOtp(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AddBeneficiaryOtpData?
)

data class AddBeneficiaryOtpData(
    @SerializedName("Message") val message: String?,
    @SerializedName("otp") val otp: String?,
    @SerializedName("requestNo") val requestNo: String?
)
