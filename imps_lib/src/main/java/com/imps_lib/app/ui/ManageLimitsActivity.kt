package com.imps_lib.app.ui

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.imps_lib.app.CommonMethods
import com.imps_lib.app.R

class ManageLimitsActivity : AppCompatActivity(){
    private var manageLimitUrl:String?=""
    private lateinit var webView: WebView
    private lateinit var backImageView: ImageView
    private val commonMethods = CommonMethods()
    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_limits)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorLightBlue)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
        progressDialog = commonMethods.progressDialog(this)
        webView = findViewById(R.id.eSignWebView)
        backImageView = findViewById(R.id.backImageView)
        try{
            manageLimitUrl = intent.getStringExtra("manageLimitUrl")!!
        }catch (e:Exception){
            print(e.message)
        }
        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.webChromeClient = object : WebChromeClient() {
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
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                val uri = Uri.parse(url)
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })

        backImageView.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }
        }
        webView.loadUrl(manageLimitUrl!!);
    }
}