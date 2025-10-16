package com.imps_lib.app.network

object WebConstants {

    const val checkStatus: String = "/api/v1/wallet/check-status"
    const val verifyOtp: String = "/api/v1/wallet/verify-otp"
    const val resendOtp: String = "/api/v1/wallet/resend-otp"
    const val getUSerDetails: String = "/api/v1/wallet/get-user-details"
    const val getAllBeneList: String = "/api/v1/wallet/imps/get-beneficiary"
    const val verifyBeneficiary: String = "/api/v1/wallet/verify-beneficiary"
    const val deleteBeneficiary: String = "/api/v1/wallet/imps/delete-beneficiary"
    const val getUserDetails: String = "/api/v1/wallet/get-user-details"
    const val getBalance: String = "/api/v1/wallet/get-balance"
    const val getBankMaster: String = "/api/v1/wallet/imps/get-bank-master"
    const val addBeneficiary: String = "/api/v1/wallet/imps/add-beneficiary"
    const val lastTopUp: String = "/api/v1/wallet/last-topup-details"
    const val initiateKyc: String = "/api/v1/wallet/initiate-ekyc"
    const val generateOtp: String = "/api/v1/wallet/generate-otp"
    const val fundTransfer: String = "/api/v1/wallet/debit"
    const val depositMasterData: String = "/api/v1/wallet/get-deposit-master-data"
    const val calculateRates: String = "/api/v1/wallet/calculate-rates"
    const val credit: String = "/api/v1/wallet/credit"

}