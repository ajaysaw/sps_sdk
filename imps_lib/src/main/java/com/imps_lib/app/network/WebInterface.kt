package com.imps_lib.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WebInterface {
    @POST(WebConstants.checkStatus)
    suspend fun fetchStatusData(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.verifyOtp)
    suspend fun verifyOtp(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.resendOtp)
    suspend fun resendOtp(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.getUserDetails)
    suspend fun getUserDetails(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.getBalance)
    suspend fun getBalance(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.getBankMaster)
    suspend fun getBankMast(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.addBeneficiary)
    suspend fun addBeneficiary(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>
    @POST(WebConstants.getAllBeneList)
    suspend fun getAllBeneList(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.verifyBeneficiary)
    suspend fun verifyBeneficiary(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.deleteBeneficiary)
    suspend fun deleteBeneficiary(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

    @POST(WebConstants.lastTopUp)
    suspend fun lastTopUp(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

}