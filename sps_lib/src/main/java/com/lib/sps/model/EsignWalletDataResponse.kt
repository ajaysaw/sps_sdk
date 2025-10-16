package com.lib.sps.model

import com.google.gson.annotations.SerializedName


data class EsignWalletDataResponse(
    @SerializedName("message") var message: String? = "",
    @SerializedName("esign_data") var eKycWalletData: EkycWalletData? = EkycWalletData(),
    @SerializedName("error_message") var errorMessage: String? = "",
)

data class EkycWalletData(
    @SerializedName("message") var message: String? = "",
    @SerializedName("status") var status: String?="",
    @SerializedName("KycStatus") var KycStatus: String? = "",
    @SerializedName("VerificationType") var VerificationType: String? = "",
    @SerializedName("Form60EsignStatus") var Form60EsignStatus: String? = "",
    @SerializedName("Form60EsignUrl") var Form60EsignUrl: String? = "",
    @SerializedName("ESIGN_SUCCESS_URL") var eSignSuccessUrl : String? = "",
    @SerializedName("ESIGN_FAILED_URL") var eSignFailedUrl : String? = "",
    @SerializedName("Form60EsignSuccessMsg") var form60EsignSuccessMsg : String? = "",
    @SerializedName("kycCharges") var kycCharges : KycCharges = KycCharges(),
    @SerializedName("signerId") var signerId : String? = "",
    @SerializedName("isWebView") var isWebView : Boolean? = true,
)

data class KycCharges(
    @SerializedName("PAN") var pan: ChargesAmount = ChargesAmount(),
    @SerializedName("FORM-60") var form60: ChargesAmount = ChargesAmount(),
)

data class ChargesAmount(
    @SerializedName("amount") var kycChargesAmount: String? = "0",
)