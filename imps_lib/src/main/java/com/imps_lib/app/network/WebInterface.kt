package com.imps_lib.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WebInterface {
    @POST(WebConstants.checkStatus)
    suspend fun fetchStatusData(@Body mapData: HashMap<String, String>): Response<HashMap<String, Any>>

}