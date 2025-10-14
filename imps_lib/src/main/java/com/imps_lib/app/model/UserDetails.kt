package com.imps_lib.app.model
import com.google.gson.annotations.SerializedName

data class UserDetails(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UserData?
)

data class UserData(
    @SerializedName("Mobileno") val mobileNo: String?,
    @SerializedName("Name") val name: String?,
    @SerializedName("Gender") val gender: String?,
    @SerializedName("KycStatus") val kycStatus: String?,
    @SerializedName("Balance") val balance: String?,
    @SerializedName("BankName") val bankName: String?,
    @SerializedName("AccNo") val accountNo: String?,
    @SerializedName("IFSC") val ifsc: String?,
    @SerializedName("PanStatus") val panStatus: String?,
    @SerializedName("UserImage") val userImage: String?,
    @SerializedName("PanNo") val panNo: String?,
    @SerializedName("DateOfBirth") val dateOfBirth: String?,
    @SerializedName("ManageLimitsUrl") val manageLimitsUrl: String?
)
