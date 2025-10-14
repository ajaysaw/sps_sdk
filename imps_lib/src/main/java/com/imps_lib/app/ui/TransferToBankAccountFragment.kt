package com.imps_lib.app.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.imps_lib.app.CommonMethods
import com.imps_lib.app.Coroutines
import com.imps_lib.app.PrefManager
import com.imps_lib.app.R
import com.imps_lib.app.model.BeneficiaryListResult
import com.imps_lib.app.network.ApiClient
import com.imps_lib.app.network.WebInterface
import com.lib.sps.java_json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class TransferToBankAccountFragment : Fragment() {

    private lateinit var rvBeneficiaries: RecyclerView
    private lateinit var adapter: BeneficiaryAdapter
    private val commonMethods = CommonMethods()
    private lateinit var progressDialog: Dialog
    private var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_transfer_to_bank_account, container, false)
        rvBeneficiaries = root.findViewById(R.id.rvBeneficiaries)
        rvBeneficiaries.layoutManager = LinearLayoutManager(requireContext())

        adapter = BeneficiaryAdapter(emptyList()) { beneficiary, action ->
            when (action) {
                "verify" -> {
                    // TODO: Handle Verify click
                }

                "transfer" -> {
                    val intent = Intent(
                        requireContext(),
                        FundTransferActivity::class.java
                    )
                    intent.putExtra("beneficiaryData", beneficiary)
                    startActivity(intent)
                }
            }
        }
        rvBeneficiaries.adapter = adapter

        // Fetch beneficiary list


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ✅ Safe place to initialize dialog in Fragment
        progressDialog = commonMethods.progressDialog(requireContext())
        getBeneData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    private fun getBeneData() {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }

            if (!commonMethods.isNetworkConnected(requireContext())) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    commonMethods.showMessageDialog(
                        requireContext(),
                        "No Internet... Please connect to a working network",
                        "Alert!",
                        "",
                        false
                    )
                }
                return@io
            }

            val service: WebInterface = ApiClient().createService(WebInterface::class.java)
            val requestData = HashMap<String, String>().apply {
                put("mobileNo", PrefManager.getInstance(requireContext()).getString("MOBILE"))
            }

            try {
                val response = service.getAllBeneList(requestData)
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    if (response.isSuccessful && response.body() != null) {
                        Log.d("Beneficiary API:", response.body().toString())

                        val jsonString = Gson().toJson(response.body())
                        val parsed = Gson().fromJson(jsonString, BeneficiaryListResult::class.java)

                        if (parsed.status == true && parsed.data.isNotEmpty()) {
                            adapter.updateData(parsed.data)
                        } else {
                            commonMethods.showMessageDialog(
                                requireContext(),
                                parsed.message ?: "No beneficiaries found",
                                "Info",
                                "",
                                false
                            )
                        }

                    } else {
                        handleApiError(response.errorBody()?.charStream()?.readText()?.trim())
                    }
                }

            } catch (e: Exception) {
                Log.e("Beneficiary API Error", e.toString())
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    commonMethods.showMessageDialog(
                        requireContext(),
                        e.localizedMessage ?: "Something went wrong",
                        "Error",
                        "",
                        false
                    )
                }
            }
        }
    }

    private suspend fun handleApiError(errorResponse: String?) {
        withContext(Dispatchers.Main) {
            try {
                val decryptData = JSONObject(errorResponse ?: "").getString("data").trim()
                val jsonObj = JSONObject(decryptData)
                val message = jsonObj.optString("error_message", "Something went wrong")

                commonMethods.showMessageDialog(
                    requireContext(),
                    message,
                    "Error",
                    "",
                    false
                )
            } catch (e: Exception) {
                commonMethods.showMessageDialog(
                    requireContext(),
                    e.localizedMessage ?: "Unknown error occurred",
                    "Error",
                    "",
                    false
                )
            }
        }
    }

}
