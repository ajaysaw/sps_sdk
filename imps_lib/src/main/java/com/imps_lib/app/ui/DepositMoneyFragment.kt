package com.imps_lib.app.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.motion.widget.Key.VISIBILITY
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.widget.TextViewCompat
import androidx.transition.Visibility
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.imps_lib.app.ApiErrorHandler
import com.imps_lib.app.CommonMethods
import com.imps_lib.app.Coroutines
import com.imps_lib.app.GlobalData
import com.imps_lib.app.R
import com.imps_lib.app.WalletManager
import com.imps_lib.app.model.DepositMasterResponse
import com.imps_lib.app.model.DepositRateSlab
import com.imps_lib.app.model.LastTopUpDetails
import com.imps_lib.app.model.WalletStatusResult
import com.imps_lib.app.network.ApiClient
import com.imps_lib.app.network.WebInterface
import com.lib.ppi_imps.model.ChargesResult
import com.lib.ppi_imps.model.DepositSuccessData
import com.lib.ppi_imps.model.DepositSuccessResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class DepositMoneyFragment : Fragment() {

    private var job: Job? = null
    private lateinit var progressDialog: Dialog
    private val commonMethods = CommonMethods()

    private lateinit var etAmount: EditText
    private lateinit var tvProceedToPay: TextView
    private lateinit var cardUnusedTopUP: CardView
    private lateinit var tvUnusedTopUpAmount: TextView
    private lateinit var tvTransactionId: TextView
    private lateinit var tvTransferNow: TextView
    private lateinit var tableLayout: TableLayout

    private var depositRateSlab: ArrayList<DepositRateSlab>? = null
    private var lastTopUpDetails: LastTopUpDetails? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_deposit_money, container, false)
        etAmount = rootView.findViewById(R.id.etAmount)
        tvProceedToPay = rootView.findViewById(R.id.tvProceedToPay)
        cardUnusedTopUP = rootView.findViewById(R.id.cardUnusedTopUP)
        tvUnusedTopUpAmount = rootView.findViewById(R.id.tvUnusedTopUpAmount)
        tvTransactionId = rootView.findViewById(R.id.tvTransactionId)
        tvTransferNow = rootView.findViewById(R.id.tvTransferNow)
        tableLayout = rootView.findViewById(R.id.table_layout)

        cardUnusedTopUP.visibility = View.GONE

        progressDialog = commonMethods.progressDialog(requireActivity())

        tvProceedToPay.setOnClickListener {

            if (etAmount.text.trim().isNotEmpty()) {
                if (depositRateSlab.isNullOrEmpty()) {
                    fetchMasterData(calculateRates = true)
                } else {
                    calculateRates()
                }

            } else {
                etAmount.error = "Please enter amount"
            }
        }

        tvTransferNow.setOnClickListener {
            (activity as? MrHomeActivity)?.navigateToTab(index = 2)
        }



        return rootView
    }

    override fun onResume() {
        super.onResume()
        fetchMasterData(calculateRates = false)
    }

    private fun fetchMasterData(calculateRates: Boolean) {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(requireActivity())) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)

                val requestData = HashMap<String, String>()
//                requestData["data"] = commonMethods.aesEncrypt(jsonObject.toString())
                requestData["mobileNo"] = GlobalData.mobileNumber

                service.fetchDepositMasterData(requestData).let { response ->

                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("deposit master response API : ", response.toString())
                            Log.d("deposit master API response :", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
//                            val decryptData = commonMethods.aesDecrypt(JSONObject(jsonString).getString("data"))
//                            val decryptData = JSONObject(jsonString).getString("data")
                            val it = Gson().fromJson(jsonString, DepositMasterResponse::class.java)
                            if (it.status == true) {
                                withContext(Dispatchers.Main) {
                                    if (it.data?.depositRateSlab != null && it.data?.depositRateSlab!!.size > 0) {

                                        depositRateSlab = it.data!!.depositRateSlab!!

                                        val df = DecimalFormat("#.##")
                                        val data = it.data?.depositRateSlab?.map { slab ->
                                            val min = df.format(slab.minAmount ?: 0)
                                            val max = df.format(slab.maxAmount ?: 0)
                                            val rate = df.format(slab.rateValue ?: 0)

                                            Pair("$min-$max", "₹ $rate")
                                        } ?: emptyList()
                                        tableLayout.removeAllViews()
                                        data.forEach { row ->
                                            val tableRow = TableRow(requireContext()).apply {
                                                layoutParams = TableLayout.LayoutParams(
                                                    TableLayout.LayoutParams.MATCH_PARENT,
                                                    TableLayout.LayoutParams.WRAP_CONTENT,
                                                )
                                            }
                                            val amountCell = TextView(requireContext()).apply {
                                                text = row.first
                                                setPadding(0, 16, 0, 16)
                                                gravity = Gravity.CENTER
                                                setTextColor(Color.parseColor("#194086"))
                                                TextViewCompat.setTextAppearance(
                                                    this,
                                                    R.style.TextViewStyleBold
                                                )
                                                layoutParams = TableRow.LayoutParams(
                                                    0,
                                                    TableRow.LayoutParams.WRAP_CONTENT,
                                                    1f
                                                )
                                            }
                                            val chargeCell = TextView(requireContext()).apply {
                                                text = row.second
                                                setPadding(0, 16, 0, 16)
                                                gravity = Gravity.CENTER
                                                setTextColor(Color.parseColor("#194086"))
                                                TextViewCompat.setTextAppearance(
                                                    this,
                                                    R.style.TextViewStyleBold
                                                )
                                                layoutParams = TableRow.LayoutParams(
                                                    0,
                                                    TableRow.LayoutParams.WRAP_CONTENT,
                                                    1f
                                                )
                                            }
                                            tableRow.addView(amountCell)
                                            tableRow.addView(chargeCell)
                                            val rowIndex = tableLayout.childCount
                                            tableRow.setBackgroundColor(
                                                if (rowIndex % 2 == 0) Color.parseColor("#F5F5F5") else Color.WHITE
                                            )
                                            tableLayout.addView(tableRow)
                                        }


                                        if (calculateRates) {
                                            calculateRates()
                                        }


                                    } else {
                                        commonMethods.showMessageDialog(
                                            requireActivity(),
                                            it.message,
                                            "Error",
                                            "",
                                            false
                                        )
                                    }

                                    if (it.data?.lastTopUpDetails != null && it.data?.lastTopUpDetails!!.isLastTopUpUnused == true) {

                                        lastTopUpDetails = it.data?.lastTopUpDetails!!

                                        cardUnusedTopUP.visibility = View.VISIBLE
                                        tvUnusedTopUpAmount.text =
                                            it.data?.lastTopUpDetails!!.topUpAmount.toString()
                                                .trim()
                                        tvTransactionId.text =
                                            it.data?.lastTopUpDetails!!.walletTransNo.toString()
                                                .trim()

                                        tvProceedToPay.isEnabled = false
                                        tvProceedToPay.alpha = 0.6f

                                    } else {
                                        cardUnusedTopUP.visibility = View.GONE
                                        tvProceedToPay.isEnabled = true
                                    }
                                }
                            }
                            else {
                                print("status false - ${ it.message }")
                                requireActivity().runOnUiThread {
                                    commonMethods.showMessageDialog(
                                        requireActivity(),
                                        it.message,
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                // ❌ Centralized error handling
                                val errorMessage = ApiErrorHandler.getErrorMessage(response)
                                Log.e("API_ERROR", errorMessage)
                                commonMethods.showMessageDialog(
                                    requireContext(),
                                    errorMessage,
                                    "Error",
                                    "",
                                    false
                                )
                            }
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e)
                        progressDialog.dismiss()
                        requireActivity().runOnUiThread {
                            commonMethods.showMessageDialog(
                                requireActivity(),
                                e.toString(),
                                "Error",
                                "",
                                false
                            )
                        }
                    }
                }
            } else {
                progressDialog.dismiss()
                requireActivity().runOnUiThread {
                    commonMethods.showMessageDialog(
                        requireActivity(),
                        "No Internet....Please be connected to a working internet",
                        "Alert!",
                        "",
                        false
                    )
                }
            }
        }
    }

    private fun calculateRates() {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(requireActivity())) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)

//                requestData["data"] = commonMethods.aesEncrypt(jsonObject.toString())

                val chargesRequest = ChargesRequest(
                    mobileNo = GlobalData.mobileNumber,
                    amount = etAmount.text.toString(),
                    rateSlab = depositRateSlab!!
                )

                service.calculateRates(chargesRequest).let { response ->

                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("deposit master response API : ", response.toString())
                            Log.d("deposit master API response :", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
//                            val decryptData = commonMethods.aesDecrypt(JSONObject(jsonString).getString("data"))
//                            val decryptData = JSONObject(jsonString).getString("data")
                            val it = Gson().fromJson(jsonString, ChargesResult::class.java)
                            if (it.status == true) {
                                withContext(Dispatchers.Main) {

                                    if (it.data != null) {
                                        val chargesResult = it.data!!

                                        showDepositSummaryBottomSheet(
                                            charges = chargesResult.charges.toString(),
                                            gstAmount = chargesResult.gstAmount.toString(),
                                            amount = chargesResult.amount.toString(),
                                            finalAmount = chargesResult.finalAmount.toString(),
                                            totalCharges = chargesResult.finalCharge.toString()
                                        )

                                    } else {
                                        commonMethods.showMessageDialog(
                                            requireActivity(),
                                            it.message,
                                            "Error",
                                            "",
                                            false
                                        )
                                    }

                                }
                            } else {
                                print(it.message)
                                requireActivity().runOnUiThread {
                                    commonMethods.showMessageDialog(
                                        requireActivity(),
                                        it.message,
                                        "Error",
                                        "",
                                        false
                                    )
                                }
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                // ❌ Centralized error handling
                                val errorMessage = ApiErrorHandler.getErrorMessage(response)
                                Log.e("API_ERROR", errorMessage)
                                commonMethods.showMessageDialog(
                                    requireContext(),
                                    errorMessage,
                                    "Error",
                                    "",
                                    false
                                )
                            }
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e)
                        progressDialog.dismiss()
                        requireActivity().runOnUiThread {
                            commonMethods.showMessageDialog(
                                requireActivity(),
                                e.toString(),
                                "Error",
                                "",
                                false
                            )
                        }
                    }
                }
            } else {
                progressDialog.dismiss()
                requireActivity().runOnUiThread {
                    commonMethods.showMessageDialog(
                        requireActivity(),
                        "No Internet....Please be connected to a working internet",
                        "Alert!",
                        "",
                        false
                    )
                }
            }
        }
    }

    private fun creditAmount(finalAmount: String, charges: String, amount: String,totalCharges: String) {
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if (commonMethods.isNetworkConnected(requireActivity())) {
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)

                val requestData = HashMap<String, String>()
//                requestData["data"] = commonMethods.aesEncrypt(jsonObject.toString())
                requestData["mobileNo"] = GlobalData.mobileNumber
                requestData["rateChannel"] = "CASH"
                requestData["amount"] = amount
                requestData["finalAmount"] = finalAmount
                requestData["charges"] = totalCharges

                service.creditAmount(requestData).let { response ->

                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("deposit master response API : ", response.toString())
                            Log.d("deposit master API response :", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
//                            val decryptData = commonMethods.aesDecrypt(JSONObject(jsonString).getString("data"))
//                            val decryptData = JSONObject(jsonString).getString("data")
                            val it = Gson().fromJson(jsonString, DepositSuccessResponse::class.java)
                            if (it.status == true) {
                                withContext(Dispatchers.Main) {
                                    if (it.data != null && it.data?.status == "SUCCESS") {
                                        depositSuccessDialog(
                                            amount = it.data?.amount.toString(),
                                            txnId = it.data?.paycTransId.toString(),
                                            balance = it.data?.balance.toString()
                                        )
                                    } else {
                                        commonMethods.showMessageDialog(
                                            requireActivity(),
                                            it.message,
                                            "Error",
                                            "",
                                            false
                                        )
                                    }
                                }
                            } else {
                                print(it.message)
                                val errorMessage = ApiErrorHandler.getErrorMessage(response)
                                Log.e("API_ERROR", errorMessage)
                                commonMethods.showMessageDialog(
                                    requireContext(),
                                    errorMessage,
                                    "Error",
                                    "",
                                    false
                                )
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                // ❌ Centralized error handling
                                val errorMessage = ApiErrorHandler.getErrorMessage(response)
                                Log.e("API_ERROR", errorMessage)
                                commonMethods.showMessageDialog(
                                    requireContext(),
                                    errorMessage,
                                    "Error",
                                    "",
                                    false
                                )
                            }
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e)
                        progressDialog.dismiss()
                        requireActivity().runOnUiThread {
                            commonMethods.showMessageDialog(
                                requireActivity(),
                                e.toString(),
                                "Error",
                                "",
                                false
                            )
                        }
                    }
                }
            } else {
                progressDialog.dismiss()
                requireActivity().runOnUiThread {
                    commonMethods.showMessageDialog(
                        requireActivity(),
                        "No Internet....Please be connected to a working internet",
                        "Alert!",
                        "",
                        false
                    )
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDepositSummaryBottomSheet(
        amount: String,
        charges: String,
        gstAmount: String,
        finalAmount: String,
        totalCharges: String
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_deposit_summary, null)
        bottomSheetDialog.setContentView(view)


        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.background = requireContext().getDrawable(R.drawable.bg_bottomsheet_rounded)


        val tvDepositAmount = view.findViewById<TextView>(R.id.tvDepositAmount)
        val tvConvenienceFee = view.findViewById<TextView>(R.id.tvConvenienceFee)
        val tvGST = view.findViewById<TextView>(R.id.tvGST)
        val tvTotalText = view.findViewById<TextView>(R.id.tvTotalText)
        val tvCancel = view.findViewById<TextView>(R.id.tvCancel)
        val tvProceed = view.findViewById<TextView>(R.id.tvProceed)


        tvDepositAmount.text = "₹${formatAmount(amount)}"
        tvConvenienceFee.text = "₹${formatAmount(charges)}"
        tvGST.text = "₹${formatAmount(gstAmount)}"
        tvTotalText.text = "₹${formatAmount(finalAmount)}"




        tvCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        tvProceed.setOnClickListener {
            bottomSheetDialog.dismiss()
            creditAmount(finalAmount = finalAmount, charges = charges, amount = amount, totalCharges = totalCharges)
        }

        bottomSheetDialog.show()
    }

    private fun formatAmount(value: String?): String {
        if (value.isNullOrBlank()) return "0"

        val number = value.toDoubleOrNull() ?: return value

        return if (number % 1.0 == 0.0)
            number.toInt().toString()
        else
            String.format("%.2f", number)
    }


    @SuppressLint("SetTextI18n")
    private fun depositSuccessDialog(amount: String, txnId: String, balance: String) {
        WalletManager.fetchBalance(requireActivity(), CommonMethods())
        val dialog = Dialog(requireActivity(), R.style.CustomDialogStyle)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_deposit_success)

        val tvDepositAmount: TextView = dialog.findViewById(R.id.tvDepositAmount)
        val tvTransactionId: TextView = dialog.findViewById(R.id.tvTransactionId)
        val tvWalletBalance: TextView = dialog.findViewById(R.id.tvWalletBalance)
        val tvOkay: TextView = dialog.findViewById(R.id.tvOkay)

        val textAmount = buildSpannedString {
            append("Your wallet has been credited with ")
            color(Color.parseColor("#000000")) { bold { append("₹$amount") } }
        }
        val textTxnId = buildSpannedString {
            append("Transaction ID: ")
            color(Color.parseColor("#000000")) { bold { append(txnId) } }
        }
        val textBalance = buildSpannedString {
            append("Available Wallet Balance: ")
            color(Color.parseColor("#000000")) { bold { append("₹$balance") } }
        }



        tvDepositAmount.text = textAmount
        tvTransactionId.text = textTxnId
        tvWalletBalance.text = textBalance



        tvOkay.setOnClickListener {
            dialog.dismiss()
            (activity as? MrHomeActivity)?.navigateToTab(index = 2)
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog.dismiss()
        job?.cancel()
    }


}

data class ChargesRequest(
    val mobileNo: String,
    val amount: String,
    val rateSlab: List<DepositRateSlab>
)
