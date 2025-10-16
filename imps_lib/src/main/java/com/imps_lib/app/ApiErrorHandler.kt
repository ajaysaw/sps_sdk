package com.imps_lib.app

import android.util.Log
import org.json.JSONObject
import retrofit2.Response

object ApiErrorHandler {

    fun getErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()

            if (errorBody.isNullOrEmpty()) {
                return "Unknown error occurred"
            }

            Log.e("API_ERROR_BODY", errorBody)

            // Try parsing it as a known structure
            val json = JSONObject(errorBody)

            when {
                json.has("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                json.has("error_message") -> json.getString("error_message")
                else -> "Something went wrong. Please try again."
            }
        } catch (e: Exception) {
            Log.e("API_ERROR_HANDLER", e.message.toString())
            "Unexpected error: ${e.localizedMessage}"
        }
    }
}
