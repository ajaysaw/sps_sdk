package com.lib.sps

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.lib.sps.java_json.XML
import com.lib.sps.model.ConsentLanguage
import com.lib.sps.model.DoKycResponse
import com.lib.sps.model.EKycMasterDataResult
import com.lib.sps.model.EkycDeviceList
import com.lib.sps.model.ResendOtpData
import com.lib.sps.model.VerifyOtpData
import com.lib.sps.model.VerifyPanData
import com.lib.sps.network.ApiClient
import com.lib.sps.network.WebInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject


class KycActivity : AppCompatActivity(), OnClickListener {

    private val commonMethods = CommonMethods()
    private lateinit var authenticationAdapter: AuthenticationDropDownAdapter;
    private lateinit var languageAdapter: LanguageDropDownAdapter;
    private lateinit var devicesAdapter: CustomDropDownAdapter;
    private lateinit var etMobileNo: EditText
    private lateinit var etAadhaarNo: EditText
    private lateinit var etPanNo: EditText
    private lateinit var tlPanNo: TextInputLayout
    private lateinit var etOtp: EditText
    private lateinit var tlOtp: TextInputLayout
    private lateinit var tvResendOtp: TextView
    private lateinit var llConsent: LinearLayout
    private lateinit var cbConsent: CheckBox
    private lateinit var tvConsentContent: TextView
    private lateinit var tvOtpSendMessage: TextView
    private lateinit var spnAuthentication: Spinner
    private lateinit var spnConsentLanguage: Spinner
    private lateinit var spnDevices: Spinner
    private lateinit var tvSubmit: TextView
    private lateinit var progressDialog: Dialog
    private var strMobileNumber = ""
    private var EkycToken = ""
    private var strAadhaarNumber = ""
    private var strMaskedAadhaarNumber = ""
    private var job: Job? = null
    private var kycAuthenticationList = ArrayList<String>()
    private var kycDeviceList = ArrayList<EkycDeviceList>()
    private var kycConsent= ArrayList<ConsentLanguage>()
    private var isConsentExpanded = false
    private var consent:String?=""
    private var lessConsent:String?=""
    private var eKycToken:String?=""
    var isUpdatingText = true
    private var panName:String?=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kyc)
        progressDialog = commonMethods.progressDialog(this)
        etMobileNo = findViewById(R.id.etMobileNo)
        etAadhaarNo = findViewById(R.id.etAadhaarNo)
        tlPanNo = findViewById(R.id.tlPanNo)
        etPanNo = findViewById(R.id.etPanNo)
        tlOtp = findViewById(R.id.tlOtp)
        etOtp = findViewById(R.id.etOtp)
        tvResendOtp = findViewById(R.id.tvResendOtp)
        llConsent = findViewById(R.id.llConsent)
        cbConsent = findViewById(R.id.cbConsent)
        tvConsentContent = findViewById(R.id.tvConsentContent)
        tvOtpSendMessage = findViewById(R.id.tvOtpSendMessage)
        spnAuthentication = findViewById(R.id.authentication)
        spnConsentLanguage = findViewById(R.id.spnConsentLanguage)
        spnDevices = findViewById(R.id.spnDevices)
        tvSubmit = findViewById(R.id.tvSubmit)
        tvSubmit.setOnClickListener(this)
        tvConsentContent.setOnClickListener(this)
        tvResendOtp.setOnClickListener(this)
        etAadhaarNo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                println("Text changed to: $s")
                if(etAadhaarNo.text.isNotEmpty() && etAadhaarNo.text.length>11 && isUpdatingText){
                    isUpdatingText = false
                    etAadhaarNo.clearFocus()
                    Validation().validateAadhaar(etAadhaarNo,etAadhaarNo.text.toString())
                    strAadhaarNumber = etAadhaarNo.text.toString()
                    strMaskedAadhaarNumber = etAadhaarNo.text.toString()
                    etAadhaarNo.setText(commonMethods.getMaskNumber(strMaskedAadhaarNumber))
                    etAadhaarNo.setSelection(etAadhaarNo.text.length)
                }else if(etAadhaarNo.text.startsWith("*") && etAadhaarNo.text.length==11){
                    etAadhaarNo.text.clear()
                }else{
                    isUpdatingText = true
                }
            }
            override fun afterTextChanged(s: Editable?) {
                println("Final text: ${s.toString()}")
            }
        })

        tlOtp.setEndIconOnClickListener {
            if(etOtp.text.isEmpty()){
                commonMethods.showMessageDialog(this,"Please Enter OTP", "Alert!","",false)
            }else if (etOtp.text.isNotEmpty() && etOtp.isEnabled){
                verifyOTP()
            }
        }

        tlPanNo.setEndIconOnClickListener {
            if(etOtp.text.isEmpty()){
                etOtp.requestFocus()
            }else if(etPanNo.text.isEmpty()){
                commonMethods.showMessageDialog(this,"Please Enter PAN No.", "Alert!","",false)
            }else if (etPanNo.text.isNotEmpty() && etPanNo.isEnabled){
                if(Validation().validatePan(etPanNo))
                verifyPan()
            }
        }

        authenticationAdapter = AuthenticationDropDownAdapter(this@KycActivity, kycAuthenticationList)
        spnAuthentication.adapter = authenticationAdapter
        spnAuthentication.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selection = parent.selectedItemPosition
                spnAuthentication.setSelection(selection)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                print("onNothingSelected")
            }
        }

        devicesAdapter = CustomDropDownAdapter(this@KycActivity, kycDeviceList)
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

        languageAdapter = LanguageDropDownAdapter(this@KycActivity, kycConsent)
        spnConsentLanguage.adapter = languageAdapter
        spnConsentLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selection = parent.selectedItemPosition
                spnConsentLanguage.setSelection(selection)
                consent = kycConsent[selection].content
                lessConsent = kycConsent[selection].less_content
                if(isConsentExpanded){
                    tvConsentContent.text = SpannableStringBuilder(consent+" Show Less").apply {
                        setSpan(ForegroundColorSpan(Color.parseColor("#2595EE")), consent!!.length, consent!!.length+10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }else{
                    tvConsentContent.text = SpannableStringBuilder(lessConsent+" Show More").apply {
                        setSpan(ForegroundColorSpan(Color.parseColor("#2595EE")), lessConsent!!.length, lessConsent!!.length+10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                print("onNothingSelected")
            }
        }
        try{
            EkycToken = intent.getStringExtra("EkycToken")!!
        }catch (e:Exception){
            print(e.message)
        }
        getMasterData() //call master API
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
                    tvConsentContent = tvConsentContent,
                    strAadhaarNumber = strAadhaarNumber,
                    strMobileNumber = strMobileNumber,
                )
            ) {
                var biometricActionData = BiometricUtils().callCapture(deviceDetails = kycDeviceList[spnDevices.selectedItemPosition], biometricFormat = "", wadh = "", pidBlockNodes = kycDeviceList[spnDevices.selectedItemPosition].pidBlockNodes,this);
                if(!biometricActionData.isError){
                    val intent = Intent(biometricActionData.action)
                    intent.setPackage(biometricActionData.packageName)
                    intent.putExtra("PID_OPTIONS", biometricActionData.pidOptXML)
                    bioMetricInfoActivityResultLauncher.launch(intent)
                }
            }
        }else if(v != null && v.id == R.id.tvConsentContent){
            isConsentExpanded = !isConsentExpanded
            if (isConsentExpanded) {
                tvConsentContent.text = SpannableStringBuilder(consent+" Show Less").apply {
                    setSpan(ForegroundColorSpan(Color.parseColor("#2595EE")), consent!!.length, consent!!.length+10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                tvConsentContent.text = SpannableStringBuilder(lessConsent+" Show More").apply {
                    setSpan(ForegroundColorSpan(Color.parseColor("#2595EE")), lessConsent!!.length, lessConsent!!.length+10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }else if(v != null && v.id == R.id.tvResendOtp){
            resendOTP()
        }
    }

    private val bioMetricInfoActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        runOnUiThread {
            val data: Intent? = result.data
            if (result.resultCode == Activity.RESULT_OK) {
                val pidData = data?.getStringExtra("PID_DATA")
                if (pidData != null) {
                    Log.e("Morpho Data :", pidData)
                    Log.e("pid Data :", pidData)
                    try {
                        val jsonObjPidData = XML.toJSONObject(pidData)
                        //val jsonPid: JSONObject? = jsonObjPidData.getJSONObject("PidData")
                        val jsonPid = JSONObject(jsonObjPidData.getJSONObject("PidData").toString())
                        val jsonResp = jsonPid.getJSONObject("Resp")
                        val errCode = jsonResp.getString("errCode")
                        if (errCode == "0") {
                            Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                            doKyc(pidData)
                        } else {
                            if (jsonResp.has("errInfo")) {
                                val errInfo =
                                    """${jsonResp.getString("errInfo")}Please reconnect your bio-metric device & try again."""
                                if (!TextUtils.isEmpty(errInfo)) {
                                    Toast.makeText(this, errInfo, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "NULL STRING RETURNED", Toast.LENGTH_LONG).show()
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Scan Failed/Aborted!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Please Connect Device", Toast.LENGTH_LONG).show()
            }
        }
    }


 private fun getMasterData() {
     job = Coroutines.io {
         withContext(Dispatchers.Main) {
             progressDialog.show()
         }
         if(commonMethods.isNetworkConnected(this)){
             val service: WebInterface = ApiClient().createService(WebInterface::class.java)
             val jsonObject = JSONObject()
             jsonObject.put("EkycToken", EkycToken)
             jsonObject.put("app_type", "MOBILE")
             val requestData = HashMap<String, String>()
             requestData["data"] = commonMethods.aesEncrypt(jsonObject.toString())
             service.fetchMasterData(requestData).let { response ->
                 try {
                     progressDialog.dismiss()
                     if (response.isSuccessful) {
                         Log.d("Master response API", response.toString());
                         Log.d("Master API response", response.body().toString());
                         val jsonString: String = Gson().toJson(response.body())
                         val decryptData = commonMethods.aesDecrypt(JSONObject(jsonString).getString("data"))
                         val it = Gson().fromJson(decryptData, EKycMasterDataResult::class.java)
                         if (it.message!!.uppercase() == "SUCCESS") {
                             kycAuthenticationList = it.ekycMasterData?.authenticationOption!!
                             kycDeviceList = it.ekycMasterData?.ekycDeviceList!!
                             kycConsent = it.ekycMasterData?.ekycConsent!!
                             withContext(Dispatchers.Main) {
                                 authenticationAdapter.updateData(kycAuthenticationList)
                                 devicesAdapter.updateData(kycDeviceList)
                                 languageAdapter.updateData(kycConsent)
                                 tvOtpSendMessage.text = it.ekycMasterData?.ekycOtpMsg
                                 eKycToken = it.ekycMasterData?.ekycToken
                                 strMobileNumber = it.ekycMasterData?.mobileNo.toString();
                                 if(strMobileNumber.length>9)
                                    etMobileNo.setText(strMobileNumber)
                                 etPanNo.isEnabled = false
                                 etAadhaarNo.isEnabled = false
                                 spnDevices.isEnabled = false
                                 spnConsentLanguage.isEnabled = false
                                 spnAuthentication.isEnabled = false
                             }
                         } else {
                             print(it.message);
                             runOnUiThread{commonMethods.showMessageDialog(this,it.errorMessage,"Error","",false)}
                         }
                     } else {
                         runOnUiThread {
                             try {
                                 val decryptData = commonMethods.aesDecrypt(JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim())
                                 val jsonObj = JSONObject(decryptData)
                                 if (jsonObj.has("message") && jsonObj.getString("message").uppercase() == "FAILURE"){
                                     if(jsonObj.has("kyc_status") && jsonObj.getString("kyc_status").uppercase() == "SUCCESS"){
                                         commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error",jsonObj.getString("kyc_status"),true)
                                     }else{
                                         commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false)
                                     }
                                 } else
                                     commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false)
                             } catch (e: Exception) {
                                 commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false)
                             }
                         }
                     }
                 } catch (e: Exception) {
                     //onError("$response", true)
                     print(e);
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

 private fun resendOTP(){
     job = Coroutines.io {
         withContext(Dispatchers.Main) {
             progressDialog.show()
         }
         if(commonMethods.isNetworkConnected(this)){
             val service: WebInterface = ApiClient().createService(WebInterface::class.java)
             val jsonObject = JSONObject()
             jsonObject.put("EkycToken", eKycToken!!)
             jsonObject.put("MobileNo", strMobileNumber)
             jsonObject.put("app_type", "MOBILE")
             val requestData = HashMap<String, String>()
             requestData["data"] = commonMethods.aesEncrypt(jsonObject.toString())
             service.resendOtp(requestData).let { response ->
                 try {
                     progressDialog.dismiss()
                     if (response.isSuccessful) {
                         Log.d("resend OTP response API : ", response.toString());
                         Log.d("resend OTP API response :", response.body().toString());
                         val jsonString: String = Gson().toJson(response.body())
                         val decryptData = commonMethods.aesDecrypt(JSONObject(jsonString).getString("data"))
                         val it = Gson().fromJson(decryptData, ResendOtpData::class.java)
                         if (it.message!!.uppercase() == "SUCCESS") {
                             withContext(Dispatchers.Main) {
                                 eKycToken = it.eKycOtpData?.eKycToken
                                 etOtp.text.clear()
                                 commonMethods.showMessageDialog(this@KycActivity,it.eKycOtpData?.eKycOtpMsg,"Success","",false)
                             }
                         } else {
                             print(it.message);
                             runOnUiThread{commonMethods.showMessageDialog(this,it.errorMessage,"Error","",false)}
                         }
                     } else {
                         runOnUiThread {
                             try {
                                 val decryptData = commonMethods.aesDecrypt(JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim())
                                 val jsonObj = JSONObject(decryptData)
                                 if (jsonObj.has("message") && jsonObj.getString("message").uppercase() == "FAILURE") {
                                     commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false)
                                 } else
                                     commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false)
                             } catch (e: Exception) {
                                 commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false)
                             }
                         }
                     }
                 } catch (e: Exception) {
                     //onError("$response", true)
                     print(e);
                     progressDialog.dismiss()
                     runOnUiThread {commonMethods.showMessageDialog(this,e.toString(),"Error","",false)}
                 }
             }
         }else{
             progressDialog.dismiss()
             runOnUiThread {
                 commonMethods.showMessageDialog(this, "No Internet....Please be connected to a working internet", "Alert!","",false)
             }
         }
     }
  }

 private fun doKyc(pidData: String) {
     job = Coroutines.io {
         withContext(Dispatchers.Main) {
             progressDialog.show()
         }
         if(commonMethods.isNetworkConnected(this)){
             val service: WebInterface = ApiClient().createService(WebInterface::class.java)
             val jsonObject = JSONObject()
             jsonObject.put("kyc_token", eKycToken!!)
             jsonObject.put("sender_mobile_no", strMobileNumber)
             jsonObject.put("aadhaar_no", strAadhaarNumber)
             jsonObject.put("capturedDeviceData", pidData)
             jsonObject.put("consent", consent!!)
             jsonObject.put("Device", kycDeviceList[spnDevices.selectedItemPosition].deviceName!!)
             jsonObject.put("pan_no", etPanNo.text.toString())
             jsonObject.put("panName", panName)
             jsonObject.put("sender_pan_proof", "PAN")
             jsonObject.put("otp", etOtp.text.toString())
             jsonObject.put("app_type", "MOBILE")
             val requestData = HashMap<String, String>()
             requestData["data"] = commonMethods.aesEncrypt(jsonObject.toString())
             service.doKyc(requestData).let { response ->
                 try {
                     progressDialog.dismiss()
                     if (response.isSuccessful) {
                         Log.d("kyc response API : ", response.toString());
                         Log.d("kyc API response :", response.body().toString());
                         val jsonString: String = Gson().toJson(response.body())
                         val decryptData = commonMethods.aesDecrypt(JSONObject(jsonString).getString("data"))
                         val it = Gson().fromJson(decryptData, DoKycResponse::class.java)
                         if (it.message!!.uppercase() == "SUCCESS") {
                             withContext(Dispatchers.Main) {
                                 if(it.eKycResponse?.status?.uppercase()=="SUCCESS"){
                                     commonMethods.showMessageDialog(this@KycActivity,it.eKycResponse?.eKycMessage,it.eKycResponse?.status,it.eKycResponse?.status!!,true)
                                 }else{
                                     commonMethods.showMessageDialog(this@KycActivity,it.eKycResponse?.eKycMessage,it.eKycResponse?.status,it.eKycResponse?.status!!,false)
                                 }
                             }
                         } else {
                             print(it.message);
                             runOnUiThread{commonMethods.showMessageDialog(this,it.errorMessage,"Error","",false)}
                         }
                     } else {
                         runOnUiThread {
                             try {
                                 val decryptData = commonMethods.aesDecrypt(JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim())
                                 val jsonObj = JSONObject(decryptData)
                                 if (jsonObj.has("message") && jsonObj.getString("message").uppercase() == "FAILURE") {
                                     commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false)
                                 } else {
                                     commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false)
                                 }
                             } catch (e: Exception) {
                                 commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false)
                             }
                         }
                     }
                 } catch (e: Exception) {
                     //onError("$response", true)
                     print(e);
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

 private fun verifyOTP(){
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if(commonMethods.isNetworkConnected(this)){
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val jsonObject = JSONObject()
                jsonObject.put("kyc_token", eKycToken!!)
                jsonObject.put("otp", etOtp.text.toString())
                jsonObject.put("app_type", "MOBILE")
                val requestData = HashMap<String, String>()
                requestData["data"] = commonMethods.aesEncrypt(jsonObject.toString())
                service.verifyOtp(requestData).let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("verify OTP response API : ", response.toString());
                            Log.d("verify OTP API response :", response.body().toString());
                            val jsonString: String = Gson().toJson(response.body())
                            val decryptData = commonMethods.aesDecrypt(JSONObject(jsonString).getString("data"))
                            val it = Gson().fromJson(decryptData, VerifyOtpData::class.java)
                            if (it.message!!.uppercase() == "SUCCESS") {
                                withContext(Dispatchers.Main) {
                                    etOtp.isEnabled = false
                                    etPanNo.isEnabled = true
                                    tlOtp.endIconDrawable = ContextCompat.getDrawable(this@KycActivity, R.drawable.check_24)
                                    tlOtp.endIconMode = TextInputLayout.END_ICON_CUSTOM
                                    tlOtp.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this@KycActivity, R.color.greenColor)))
                                    etPanNo.requestFocus()
                                    tvResendOtp.visibility = View.GONE
                                    tvOtpSendMessage.visibility = View.GONE
                                }
                            } else {
                                print(it.message);
                                runOnUiThread{commonMethods.showMessageDialog(this,it.errorMessage,"Error","",false)}
                            }
                        } else {
                            runOnUiThread {
                                try {
                                    val decryptData = commonMethods.aesDecrypt(JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim())
                                    val jsonObj = JSONObject(decryptData)
                                    if (jsonObj.has("message") && jsonObj.getString("message").uppercase() == "FAILURE") {
                                        commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false)
                                    } else
                                        commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false)
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e);
                        progressDialog.dismiss()
                        runOnUiThread {commonMethods.showMessageDialog(this,e.toString(),"Error","",false)}
                    }
                }
            }else{
                progressDialog.dismiss()
                runOnUiThread {
                    commonMethods.showMessageDialog(this, "No Internet....Please be connected to a working internet", "Alert!","",false)
                }
            }
        }
    }

 private fun verifyPan(){
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if(commonMethods.isNetworkConnected(this)){
                val service: WebInterface = ApiClient().createService(WebInterface::class.java)
                val jsonObject = JSONObject()
                jsonObject.put("kyc_token", eKycToken!!)
                jsonObject.put("otp", etOtp.text.toString())
                jsonObject.put("pan_no", etPanNo.text.toString())
                jsonObject.put("app_type", "MOBILE")
                val requestData = HashMap<String, String>()
                requestData["data"] = commonMethods.aesEncrypt(jsonObject.toString())
                service.verifyPan(requestData).let { response ->
                    try {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d("verify PAN response API : ", response.toString());
                            Log.d("verify PAN API response :", response.body().toString());
                            val jsonString: String = Gson().toJson(response.body())
                            val decryptData = commonMethods.aesDecrypt(JSONObject(jsonString).getString("data"))
                            val it = Gson().fromJson(decryptData, VerifyPanData::class.java)
                            if (it.message!!.uppercase() == "SUCCESS") {
                                withContext(Dispatchers.Main) {
                                    panName = it.eKycOtpData?.panName
                                    etPanNo.isEnabled = false
                                    etAadhaarNo.isEnabled = true
                                    spnDevices.isEnabled = true
                                    spnConsentLanguage.isEnabled = true
                                    spnAuthentication.isEnabled = true
                                    tlPanNo.endIconDrawable = ContextCompat.getDrawable(this@KycActivity, R.drawable.check_24)
                                    tlPanNo.endIconMode = TextInputLayout.END_ICON_CUSTOM
                                    tlPanNo.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this@KycActivity, R.color.greenColor)))
                                    etAadhaarNo.requestFocus()
                                }
                            } else {
                                print(it.message);
                                runOnUiThread{commonMethods.showMessageDialog(this,it.errorMessage,"Error","",false)}
                            }
                        } else {
                            runOnUiThread {
                                try {
                                    val decryptData = commonMethods.aesDecrypt(JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim())
                                    val jsonObj = JSONObject(decryptData)
                                    if (jsonObj.has("message") && jsonObj.getString("message").uppercase() == "FAILURE") {
                                        commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false)
                                    } else
                                        commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false)
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e);
                        progressDialog.dismiss()
                        runOnUiThread {commonMethods.showMessageDialog(this,e.toString(),"Error","",false)}
                    }
                }
            }else{
                progressDialog.dismiss()
                runOnUiThread {
                    commonMethods.showMessageDialog(this, "No Internet....Please be connected to a working internet", "Alert!","",false)
                }
            }
        }
    }
}