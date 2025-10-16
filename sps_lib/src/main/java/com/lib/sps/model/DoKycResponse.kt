package com.lib.sps.model

import com.google.gson.annotations.SerializedName

data class DoKycResponse (
    @SerializedName("message") var message: String? = "",
    @SerializedName("kyc_response") var eKycResponse: KycResponse? = KycResponse(),
    @SerializedName("error_message") var errorMessage: String? = "",
    )

data class KycResponse(
    @SerializedName("message") var eKycMessage: String? = "",
    @SerializedName("status") var status: String? = "",
    @SerializedName("redirectionUrl") var redirectionUrl: String? = "",
    @SerializedName("FORM60PdfGenResponse") var fORM60PdfGenResponse : FORM60PdfGenResponse? = FORM60PdfGenResponse()
)

data class FORM60PdfGenResponse (
    @SerializedName("PDF_URL") var pdfUrl   : String? = null,
    @SerializedName("REDIRECT_URL") var eSignUrl : String? = null,
    @SerializedName("ESIGN_SUCCESS_URL") var eSignSuccessUrl : String? = null,
    @SerializedName("ESIGN_FAILED_URL") var eSignFailedUrl : String? = null,
    @SerializedName("content") var content: ArrayList<String> = arrayListOf(),
    @SerializedName("verificationType") var verificationType: String? = "",
    @SerializedName("kycCharges") var kycCharges : String? = "",
    @SerializedName("signerId") var signerId : String? = "",
    @SerializedName("isWebView") var isWebView : Boolean? = true,

)