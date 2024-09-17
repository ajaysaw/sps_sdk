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
    suspend fun resendOtp(@Body mapData: HashMap<String, String>): Response<HashMap<String, String>>

    @POST(WebConstants.doKycUrl)
    suspend fun doKyc(@Body mapData: HashMap<String, String>): Response<HashMap<String, String>>

}