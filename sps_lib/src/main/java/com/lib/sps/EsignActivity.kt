package com.lib.sps

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.lib.sps.model.GetStatusResponse
import com.lib.sps.network.ApiClient
import com.lib.sps.network.WebInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.json.JSONObject

class EsignActivity : AppCompatActivity() , OnClickListener {
    private val commonMethods = CommonMethods()
    private lateinit var pdfWebView: WebView
    private lateinit var backImageView: ImageView
    private lateinit var progressDialog: Dialog
    private var eKycToken:String?=""
    private var eSignUrl:String?=""
//    private var eSignSuccessUrl:String?=""
//    private var eSignFailedUrl:String?=""
    private var job: Job? = null
    private var status:Boolean=true;
    private var verificationType: String? = ""
    private var kycCharges: String? = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_esign)
        progressDialog = commonMethods.progressDialog(this)
        pdfWebView = findViewById(R.id.eSignWebView)
        backImageView = findViewById(R.id.backImageView)
        backImageView.setOnClickListener(this)
        try{
            eKycToken = intent.getStringExtra("EkycToken")!!
            eSignUrl = intent.getStringExtra("REDIRECT_URL")!!
            verificationType = intent.getStringExtra("verificationType")!!
            kycCharges = intent.getStringExtra("kycCharges")!!
        }catch (e:Exception){
            print(e.message)
        }

        pdfWebView.settings.javaScriptEnabled = true
        pdfWebView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        pdfWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        if (newProgress < 100) {
                            progressDialog.show()
                        } else {
                            progressDialog.dismiss()
                        }
                    }
                }
            }
        }
        pdfWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                val uri = Uri.parse(url)
                val kycStatus = uri.getQueryParameter("KycStatus")
                if (kycStatus?.uppercase()=="SUCCESS" && status) {
                    status = false
                    showMessageDialog("Your KYC verification for the sender wallet is completed.","Success","Success")
                    return true
                }else if(kycStatus?.uppercase()=="FAILED" && status){
                    status = false
                    showMessageDialog("Your sender wallet KYC verification is unsuccessful.","Error","Fail")
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                getStatusEsign()
            }
        })

        pdfWebView.loadUrl(eSignUrl!!);
    }

    override fun onClick(v: View?) {
        if(v != null && v.id == R.id.backImageView){
            getStatusEsign()
        }
    }

    private fun showMessageDialog(messageTxt: String?, argTitle: String?, kycStatus:String) {
        val dialog = Dialog(this, R.style.CustomDialogStyle)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.message_dialog_layout)
        val tvTitle: TextView = dialog.findViewById(R.id.tvTitle)
        val tvDes: TextView = dialog.findViewById(R.id.tvDes)
        val tvOk: TextView = dialog.findViewById(R.id.tvOk)
        tvTitle.text = argTitle
        tvDes.text = messageTxt
        tvOk.setOnClickListener {
            dialog.dismiss()
                val resultIntent = Intent()
                resultIntent.putExtra("message", messageTxt)
                resultIntent.putExtra("status", kycStatus)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
        }
        dialog.show()
    }


    private fun getStatusEsign(){
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if(commonMethods.isNetworkConnected(this)){
                val service: WebInterface = ApiClient().createService(WebInterface::class.java,this)
                val jsonObject = JSONObject()
                jsonObject.put("EkycToken", eKycToken)
                jsonObject.put("app_type", "MOBILE")
                val requestData = HashMap<String, String>()
                requestData["data"] = commonMethods.aesEncrypt(jsonObject.toString())
                service.getEsignStatus(requestData).let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("getStatus response API", response.toString());
                            Log.d("getStatus API response", response.body().toString());
                            val jsonString: String = Gson().toJson(response.body())
                            val decryptData = commonMethods.aesDecrypt(JSONObject(jsonString).getString("data"))
                            val it = Gson().fromJson(decryptData, GetStatusResponse::class.java)
                            val resultIntent = Intent().apply {
                                putExtra("message", it.eSignData?.message)
                                putExtra("status", it.eSignData?.form60EsignStatus)
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()

                        } else {
                            runOnUiThread {
                                try {
                                    val decryptData = commonMethods.aesDecrypt(JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim())
                                    val jsonObj = JSONObject(decryptData)
                                    commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false,verificationType!!,kycCharges!!)
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false,verificationType!!,kycCharges!!)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        print(e);
                        progressDialog.dismiss()
                        runOnUiThread{commonMethods.showMessageDialog(this,e.toString(),"Error","",false,verificationType!!,kycCharges!!)}
                    }
                }
            }else{
                progressDialog.dismiss()
                runOnUiThread{commonMethods.showMessageDialog(this,"No Internet....Please be connected to a working internet","Alert!","",false,verificationType!!,kycCharges!!) }
            }
        }
    }
}
