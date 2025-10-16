package com.lib.sps.network

import payworld.com.aeps_lib.data.network.WebConstants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WebInterface {
    @POST(WebConstants.masterDataUrl)
    suspend fun fetchMasterData(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.resendOtpUrl)
    suspend fun resendOtp(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.doKycUrl)
    suspend fun doKyc(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.verifyOtp)
    suspend fun verifyOtp(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.verifyPan)
    suspend fun verifyPan(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.eSignStatus)
    suspend fun getEsignStatus(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.getWalletData)
    suspend fun getKycWalletData(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

}