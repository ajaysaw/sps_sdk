package com.lib.sps

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.google.gson.Gson
import com.lib.sps.model.EKycMasterDataResult
import com.lib.sps.model.GetStatusResponse
import com.lib.sps.network.ApiClient
import com.lib.sps.network.WebInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection


class Form60Activity : AppCompatActivity() ,OnClickListener{
    private val commonMethods = CommonMethods()
    private lateinit var pdfView: PDFView
    private lateinit var tvSignature: TextView
    private lateinit var tvDownload: TextView
    private lateinit var tvContent: TextView
    private lateinit var backImageView: ImageView
    private lateinit var progressDialog: Dialog
    private var eKycToken:String?=""
    private var pdfUrl:String?=""
    private var eSignUrl:String?=""
    private var eSignSuccessUrl:String?=""
    private var eSignFailedUrl:String?=""
    private var content:ArrayList<String> = arrayListOf()
    private var job: Job? = null
    private var verificationType: String? = ""
    private var kycCharges: String? = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_form60)
        progressDialog = commonMethods.progressDialog(this)
        pdfView = findViewById(R.id.pdfView)
        tvSignature = findViewById(R.id.tvSignature)
        tvDownload = findViewById(R.id.tvDownload)
        tvContent = findViewById(R.id.tvContent)
        backImageView = findViewById(R.id.backImageView)
        tvSignature.setOnClickListener(this)
        backImageView.setOnClickListener(this)
        tvDownload.setOnClickListener(this)

        try{
            eKycToken = intent.getStringExtra("EkycToken")!!
            pdfUrl = intent.getStringExtra("PDF_URL")!!
            eSignUrl = intent.getStringExtra("REDIRECT_URL")!!
            eSignSuccessUrl = intent.getStringExtra("ESIGN_SUCCESS_URL")!!
            eSignFailedUrl = intent.getStringExtra("ESIGN_FAILED_URL")!!
            content = intent.getStringArrayListExtra("content")!!
            verificationType = intent.getStringExtra("verificationType")!!
            kycCharges = intent.getStringExtra("kycCharges")!!
            if(content.size>0){
                val consent = content.joinToString(separator = "\n\n")
                tvContent.text = consent
            }

        viewPdf(pdfView,pdfUrl!!)

        }catch (e:Exception){
            print(e.message)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                getStatusEsign()
            }
        })
    }

    override fun onClick(v: View?) {
        if (v != null && v.id == R.id.tvSignature) {
            val intent = Intent(this@Form60Activity, EsignActivity::class.java)
            intent.putExtra("EkycToken",eKycToken)
            intent.putExtra("REDIRECT_URL",eSignUrl)
            intent.putExtra("verificationType", verificationType)
            intent.putExtra("kycCharges", kycCharges)
            eSignActivityResultLauncher.launch(intent)
        }else if(v != null && v.id == R.id.backImageView){
            getStatusEsign()
        }else if(v != null && v.id == R.id.tvDownload){
            val finaleName = Uri.parse(pdfUrl!!).lastPathSegment
            downloadPdf(pdfUrl!!,finaleName!!)
        }
    }


    private val eSignActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val message = data?.getStringExtra("message")
            val status = data?.getStringExtra("status")
            if(message!=null && status!=null){
                val resultIntent = Intent().apply {
                    putExtra("message", message)
                    putExtra("status", status)
                }
                setResult(Activity.RESULT_OK, resultIntent)
            }
            finish()
        }
    }

    private fun viewPdf(pdfView: PDFView, pdfUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var inputStream: InputStream? = null
            try {
                withContext(Dispatchers.Main) {
                    progressDialog.show()
                }
                val url = URL(pdfUrl)
                val urlConnection: HttpURLConnection = url.openConnection() as HttpsURLConnection
                if (urlConnection.responseCode == 200) {
                    inputStream = BufferedInputStream(urlConnection.inputStream)
                }
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                }
            }

            withContext(Dispatchers.Main) {
                inputStream?.let {
                    pdfView.fromStream(it).load()
                } ?: run {
                    Toast.makeText(this@Form60Activity, "Failed to load PDF", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
        }
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
    private fun downloadPdf(url: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription("Downloading PDF...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName
                )

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(this@Form60Activity, "Download started...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this@Form60Activity, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

}