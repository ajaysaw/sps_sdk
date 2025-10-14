package com.imps_lib.app.ui

import android.app.Dialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.imps_lib.app.CommonMethods
import com.imps_lib.app.Coroutines
import com.imps_lib.app.PrefManager
import com.imps_lib.app.R
import com.imps_lib.app.model.UserDetails
import com.imps_lib.app.model.WalletBalance
import com.imps_lib.app.network.ApiClient
import com.imps_lib.app.network.WebInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MrHomeActivity : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private val tabTitles = listOf("Add Beneficiary", "Deposit Money", "Transfer To Bank Account")
    private var job: Job? = null
    private lateinit var progressDialog: Dialog
    private lateinit var userDetails: UserDetails
    private lateinit var walletBalance: WalletBalance
    private val commonMethods = CommonMethods()
    private lateinit var tvImage: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvLimit: TextView
    private lateinit var tvBalance: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mr_home)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        tvImage = findViewById(R.id.tvImage)
        tvName = findViewById(R.id.tvName)
        tvLimit = findViewById(R.id.tvLimit)
        tvBalance = findViewById(R.id.tvBalance)
        progressDialog = commonMethods.progressDialog(this)
        viewPager.adapter = TabAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
        getUserDetails()
        getBalance()
    }

    private fun getUserDetails(){
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if(commonMethods.isNetworkConnected(this)){
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                requestData["mobileNo"] = "8800985790"
                service.getUserDetails(requestData).let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("get-user-details API", response.toString())
                            Log.d("get-user-details API response", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
                            val it = Gson().fromJson(JSONObject(jsonString).toString(), UserDetails::class.java)
                            if (it.status) {
                                withContext(Dispatchers.Main) {
                                    userDetails = it
                                    tvName.text = it.data?.name
                                    profileImage(it.data?.userImage.toString(),tvImage);
                                }
                            } else {
                                runOnUiThread{commonMethods.showMessageDialog(this,it.message,"Error","",false)}
                            }
                        } else {
                            runOnUiThread {
                                try {
                                    commonMethods.showMessageDialog(this, JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim(), "Error","",false)
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        progressDialog.dismiss()
                        runOnUiThread{commonMethods.showMessageDialog(this,e.toString(),"Error","",false)}
                    }
                }
            }else{
                progressDialog.dismiss()
                runOnUiThread{commonMethods.showMessageDialog(this,"No Internet....Please be connected to a working internet","Alert!","",false) }
            }
        }
    }
    private fun getBalance(){
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if(commonMethods.isNetworkConnected(this)){
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val requestData = HashMap<String, String>()
                requestData["mobileNo"] = PrefManager.getInstance(this@MrHomeActivity).getString("MOBILE")
                service.getBalance(requestData).let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("get-balance API", response.toString())
                            Log.d("get-balance API response", response.body().toString())
                            val jsonString: String = Gson().toJson(response.body())
                            val it = Gson().fromJson(JSONObject(jsonString).toString(), WalletBalance::class.java)
                            if (it.status) {
                                withContext(Dispatchers.Main) {
                                    walletBalance = it
                                    tvLimit.text = "Limit: ₹${it.data?.remainingCashDepositLimit?:0.0}"
                                    tvBalance.text = "Balance: ₹${it.data?.balance?:0.0}"
                                }
                            } else {
                                runOnUiThread{commonMethods.showMessageDialog(this,it.message,"Error","",false)}
                            }
                        } else {
                            runOnUiThread {
                                try {
                                    commonMethods.showMessageDialog(this, JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim(), "Error","",false)
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        print(e)
                        progressDialog.dismiss()
                        runOnUiThread{commonMethods.showMessageDialog(this,e.toString(),"Error","",false)}
                    }
                }
            }else{
                progressDialog.dismiss()
                runOnUiThread{commonMethods.showMessageDialog(this,"No Internet....Please be connected to a working internet","Alert!","",false) }
            }
        }
    }

    private fun profileImage(base64String: String?, imageView: ImageView) {
        try {
            if (!base64String.isNullOrEmpty()) {
                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(R.drawable.profile_icon)
                }
            } else {
                imageView.setImageResource(R.drawable.profile_icon)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            imageView.setImageResource(R.drawable.profile_icon)
        }
    }
}