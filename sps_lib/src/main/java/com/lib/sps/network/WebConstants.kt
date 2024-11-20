package payworld.com.aeps_lib.data.network

object WebConstants {
    const val masterDataUrl: String = "/get/new/ekyc/master-data"
    const val resendOtpUrl: String = "/get/new/ekyc/otp-data"
    const val doKycUrl: String = "/do/new/ekyc/kyc-data-request"
    const val verifyOtp: String = "/do/new/ekyc/verify-otp"
    const val verifyPan: String = "/do/new/ekyc/verify-pan"

    const val REQUEST_CAMERA_PERMISSION = 201
    const val MY_PERMISSIONS_REQUEST_LOCATION = 123
    const val MY_PERMISSIONS_WRITE_STORAGE = 200
}