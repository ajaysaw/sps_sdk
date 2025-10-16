package com.imps_lib.app

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.imps_lib.app.model.WalletBalance
import com.imps_lib.app.network.ApiClient
import com.imps_lib.app.network.WebInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

object WalletManager {

    private val _walletBalance = MutableLiveData<WalletBalance?>()
    val walletBalance: LiveData<WalletBalance?> get() = _walletBalance

    fun fetchBalance(context: Activity, commonMethods: CommonMethods, progressDialog: Dialog? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    progressDialog?.show()
                }

                if (!commonMethods.isNetworkConnected(context)) {
                    withContext(Dispatchers.Main) {
                        progressDialog?.dismiss()
                        commonMethods.showMessageDialog(
                            context,
                            "No Internet... Please connect to working internet.",
                            "Alert!",
                            "",
                            false
                        )
                    }
                    return@launch
                }

                val service = ApiClient().createService(WebInterface::class.java)
                val requestData = hashMapOf("mobileNo" to GlobalData.mobileNumber)
                val response = service.getBalance(requestData)

                withContext(Dispatchers.Main) {
                    progressDialog?.dismiss()

                    if (response.isSuccessful) {
                        val jsonString = Gson().toJson(response.body())
                        val wallet = Gson().fromJson(JSONObject(jsonString).toString(), WalletBalance::class.java)

                        if (wallet.status) {
                            _walletBalance.value = wallet
                        } else {
                            commonMethods.showMessageDialog(context, wallet.message, "Error", "", false)
                        }
                    } else {
                        context.runOnUiThread {
                            // ❌ Centralized error handling
                            val errorMessage = ApiErrorHandler.getErrorMessage(response)
                            Log.e("API_ERROR", errorMessage)
                            commonMethods.showMessageDialog(
                                context,
                                errorMessage,
                                "Error",
                                "",
                                false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WalletManager", "fetchBalance error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    progressDialog?.dismiss()
                    commonMethods.showMessageDialog(context, e.message ?: "Something went wrong", "Error", "", false)
                }
            }
        }
    }
}
