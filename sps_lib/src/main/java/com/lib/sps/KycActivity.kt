package com.lib.sps

import android.app.Dialog
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.example.example.ConsentLanguage
import com.example.example.EKycMasterDataResult
import com.example.example.EkycDeviceList
import com.google.gson.Gson
import com.lib.sps.network.ApiClient
import com.lib.sps.network.WebInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.internal.notify
import org.json.JSONObject
import payworld.com.aeps_lib.data.network.ApiResponse

class KycActivity : AppCompatActivity(), OnClickListener {

    val commonMethods = CommonMethods()

    private lateinit var etMobileNo: EditText
    private lateinit var etAadhaarNo: EditText
    private lateinit var etPanNo: EditText
    private lateinit var etOtp: EditText
    private lateinit var tvResendOtp: TextView
    private lateinit var llConsent: LinearLayout
    private lateinit var cbConsent: CheckBox
    private lateinit var tvConsentContent: TextView
    private lateinit var spnConsentLanguage: Spinner
    private lateinit var spnDevices: Spinner
    private lateinit var tvSubmit: TextView
    private lateinit var progressDialog: Dialog
    private var strMobileNumber = ""
    private var strAadhaarNumber = ""
    private var strMaskedAadhaarNumber = ""
    private var strPanNumber = ""
    private var strOTP = ""
    var job: Job? = null
    var kycDeviceList = ArrayList<EkycDeviceList>()
    var kycConsent= ArrayList<ConsentLanguage>()
    var isConsentExpanded = false
    var consent:String?=""
    var less_consent:String?=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kyc)
        progressDialog = CommonMethods().progressDialog(this)
        etMobileNo = findViewById(R.id.etMobileNo)
        etAadhaarNo = findViewById(R.id.etAadhaarNo)
        etPanNo = findViewById(R.id.etPanNo)
        etOtp = findViewById(R.id.etOtp)
        tvResendOtp = findViewById(R.id.tvResendOtp)
        llConsent = findViewById(R.id.llConsent)
        cbConsent = findViewById(R.id.cbConsent)
        tvConsentContent = findViewById(R.id.tvConsentContent)
        spnConsentLanguage = findViewById(R.id.spnConsentLanguage)
        spnDevices = findViewById(R.id.spnDevices)
        tvSubmit = findViewById(R.id.tvSubmit)
        tvSubmit.setOnClickListener(this)
        tvConsentContent.setOnClickListener(this)
        etMobileNo.setText("9253022366")
        etAadhaarNo.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                strAadhaarNumber = etAadhaarNo.text.toString()
                strMaskedAadhaarNumber = etAadhaarNo.text.toString()
                strMaskedAadhaarNumber = if (strMaskedAadhaarNumber.length == 12)
                    strMaskedAadhaarNumber.replaceRange(0, 8, "********")
                else {
                    strMaskedAadhaarNumber.replaceRange(0, 12, "************")
                }
                etAadhaarNo.setText(strMaskedAadhaarNumber)
            } else {
                // The EditText has gained focus
            }
        }


        var devicesAdapter = CustomDropDownAdapter(this@KycActivity, kycDeviceList)
        spnDevices.adapter = devicesAdapter
        spnDevices.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selection = parent.selectedItemPosition
                spnDevices.setSelection(selection)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                print("onNothingSelected")
            }
        }

        val languageAdapter = LanguageDropDownAdapter(this@KycActivity, kycConsent)
        spnConsentLanguage.adapter = languageAdapter
        spnConsentLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val seleccion = parent.selectedItemPosition
                spnConsentLanguage.setSelection(seleccion)
                consent = kycConsent[seleccion].content
                less_consent = kycConsent[seleccion].less_content
                if(isConsentExpanded){
                    tvConsentContent.text = SpannableStringBuilder(consent+" Show Less").apply {
                        setSpan(ForegroundColorSpan(Color.parseColor("#2595EE")), consent!!.length, consent!!.length+10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }else{
                    tvConsentContent.text = SpannableStringBuilder(less_consent+" Show More").apply {
                        setSpan(ForegroundColorSpan(Color.parseColor("#2595EE")), less_consent!!.length, less_consent!!.length+10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                print("onNothingSelected")
            }
        }

        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if(CommonMethods().isNetworkConnected(this)){
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                service.fetchMasterData().let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            val gson = Gson()
                            val jsonString: String = gson.toJson(response.body())
                            val it = Gson().fromJson(jsonString, EKycMasterDataResult::class.java)
                            if (it.message == "SUCCESS") {
                                kycDeviceList = it.ekycMasterData?.ekycDeviceList!!
                                kycConsent = it.ekycMasterData?.ekycConsent!!
                                withContext(Dispatchers.Main) {
                                    devicesAdapter.updateData(kycDeviceList)
                                    languageAdapter.updateData(kycConsent)
                                }
                            } else
                                //onError(it.message!!, true)
                            print(it.message);
                        } else {
                            val errorMsg = try {
                                val jsonObj =
                                    JSONObject(
                                        response.errorBody()!!.charStream().readText().trim()
                                    )
                                if (jsonObj.has("data")) {
                                    JSONObject(jsonObj.getString("data").trim()).getString("error_message")
                                } else
                                    jsonObj.getString("error_message")
                            } catch (e: Exception) {
                                e.message.toString()
                            }
                            //onError(errorMsg, true)
                            print(errorMsg);
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e);
                        progressDialog.dismiss()
                    }
                }
            }else{
                progressDialog.dismiss()
                CommonMethods().showMessageDialog(this,"No Internet....Please be connected to a working internet","Alert!")
            }
        }
    }

    override fun onClick(v: View?) {
        if (v != null && v.id == R.id.tvSubmit) {
            val validationUtil = Validation()

            if (validationUtil.validateForm(
                    etMobile = etMobileNo,
                    etAadhaar = etAadhaarNo,
                    etPan = etPanNo,
                    etOtp = etOtp,
                    spnConsent = spnConsentLanguage,
                    cbConsent = cbConsent,
                    spnDevice = spnDevices,
                    tvConsentContent = tvConsentContent
                )
            ) {
                Toast.makeText(this@KycActivity, "Validated", Toast.LENGTH_LONG).show()
               /* var biometricActionData = BiometricUtils().callCapture("Biometric",);
                if(!biometricActionData.isError){
                    val intent = Intent(biometricActionData.action)
                    intent.setPackage(biometricActionData.packageName)
                    intent.putExtra("PID_OPTIONS", biometricActionData.pidOptXML)
                    bioMetricInfoActivityResultLauncher.launch(intent)
                }else{
                    CommonMethods().showMessageDialog(this, biometricActionData.errorMessage, "Message")
                }*/
            }
        }else if(v != null && v.id == R.id.tvConsentContent){
            isConsentExpanded = !isConsentExpanded
            if (isConsentExpanded) {
                tvConsentContent.text = SpannableStringBuilder(consent+" Show Less").apply {
                    setSpan(ForegroundColorSpan(Color.parseColor("#2595EE")), consent!!.length, consent!!.length+10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                tvConsentContent.text = SpannableStringBuilder(less_consent+" Show More").apply {
                    setSpan(ForegroundColorSpan(Color.parseColor("#2595EE")), less_consent!!.length, less_consent!!.length+10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }

}