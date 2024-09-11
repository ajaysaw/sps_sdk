package com.lib.sps.network

import payworld.com.aeps_lib.data.network.WebConstants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WebInterface {
    @POST(WebConstants.masterDataUrl)
    suspend fun fetchValidateRequestData(
        @Body mapData: HashMap<String, String>
    ): Response<HashMap<String, String>>

    @GET(WebConstants.masterDataUrl)
    suspend fun fetchMasterData(): Response<HashMap<String, Any>>

}