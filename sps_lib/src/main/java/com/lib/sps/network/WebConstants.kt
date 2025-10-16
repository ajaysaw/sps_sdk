package payworld.com.aeps_lib.data.network

object WebConstants {
    const val masterDataUrl: String = "/get/new/ekyc/master-data"
    const val resendOtpUrl: String = "/get/new/ekyc/otp-data"
    const val doKycUrl: String = "/do/new/ekyc/kyc-data-request"
    const val verifyOtp: String = "/do/new/ekyc/verify-otp"
    const val verifyPan: String = "/do/new/ekyc/verify-pan"
    const val eSignStatus: String = "/get/new/form60/esign/status"
    const val getWalletData: String = "/get/new/ekyc/wallet-data"
}