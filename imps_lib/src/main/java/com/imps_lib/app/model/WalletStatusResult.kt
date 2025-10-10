package com.imps_lib.app.model

import com.google.gson.annotations.SerializedName

data class WalletStatusResult(

    @SerializedName("status") var status: Boolean? = false,
    @SerializedName("message") var message: String? = "",
    @SerializedName("data") var data: WalletStatusData? = WalletStatusData()

)

data class WalletStatusData(
    @SerializedName("CustomerName") var customerName: String? = "",
    @SerializedName("MobileNo") var mobileNo: String? = "",
    @SerializedName("KycStatus") var kycStatus: String? = "",
    @SerializedName("CardExists") var cardExists: String? = "",
    @SerializedName("CardNo") var cardNo: String? = "",
    @SerializedName("BcAgentId") var bcAgentId: String? = "",
    @SerializedName("onboardingDistributorId") var onboardingDistributorId: String? = "",
    @SerializedName("otp") var otp: String? = "",
    @SerializedName("requestNo") var requestNo: String? = "",
    @SerializedName("EkycUrl") var ekycUrl: String? = "",
    @SerializedName("EkycPopUpData") var ekycPopUpData: EkycPopUpData? = EkycPopUpData()
)

data class EkycPopUpData(
    @SerializedName("heading") var heading: String? = "",
    @SerializedName("subheading") var subheading: String? = "",
    @SerializedName("proceedBtnText") var proceedBtnText: String? = "",
    @SerializedName("cancelBtnText") var cancelBtnText: String? = ""
)
