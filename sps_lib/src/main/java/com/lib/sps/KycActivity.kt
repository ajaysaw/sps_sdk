package com.lib.sps

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
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
import com.example.example.ConsentLanguage
import com.example.example.EKycMasterDataResult
import com.example.example.EkycDeviceList
import com.google.gson.Gson
import com.lib.sps.java_json.XML
import com.lib.sps.model.DoKycResponse
import com.lib.sps.model.ResendOtpData
import com.lib.sps.network.ApiClient
import com.lib.sps.network.WebInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class KycActivity : AppCompatActivity(), OnClickListener {

    private val commonMethods = CommonMethods()
    private lateinit var languageAdapter: LanguageDropDownAdapter;
    private lateinit var devicesAdapter: CustomDropDownAdapter;
    private lateinit var etMobileNo: EditText
    private lateinit var etAadhaarNo: EditText
    private lateinit var etPanNo: EditText
    private lateinit var etOtp: EditText
    private lateinit var tvResendOtp: TextView
    private lateinit var llConsent: LinearLayout
    private lateinit var cbConsent: CheckBox
    private lateinit var tvConsentContent: TextView
    private lateinit var tvOtpSendMessage: TextView
    private lateinit var spnConsentLanguage: Spinner
    private lateinit var spnDevices: Spinner
    private lateinit var tvSubmit: TextView
    private lateinit var progressDialog: Dialog
    private var strMobileNumber = ""
    private var agentId = ""
    private var secretKey = ""
    private var strAadhaarNumber = ""
    private var strMaskedAadhaarNumber = ""
    private var job: Job? = null
    private var kycDeviceList = ArrayList<EkycDeviceList>()
    private var kycConsent= ArrayList<ConsentLanguage>()
    private var isConsentExpanded = false
    private var consent:String?=""
    private var lessConsent:String?=""
    private var eKycToken:String?=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kyc)
        progressDialog = commonMethods.progressDialog(this)
        etMobileNo = findViewById(R.id.etMobileNo)
        etAadhaarNo = findViewById(R.id.etAadhaarNo)
        etPanNo = findViewById(R.id.etPanNo)
        etOtp = findViewById(R.id.etOtp)
        tvResendOtp = findViewById(R.id.tvResendOtp)
        llConsent = findViewById(R.id.llConsent)
        cbConsent = findViewById(R.id.cbConsent)
        tvConsentContent = findViewById(R.id.tvConsentContent)
        tvOtpSendMessage = findViewById(R.id.tvOtpSendMessage)
        spnConsentLanguage = findViewById(R.id.spnConsentLanguage)
        spnDevices = findViewById(R.id.spnDevices)
        tvSubmit = findViewById(R.id.tvSubmit)
        tvSubmit.setOnClickListener(this)
        tvConsentContent.setOnClickListener(this)
        tvResendOtp.setOnClickListener(this)
        etAadhaarNo.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && etAadhaarNo.text.isNotEmpty() && etAadhaarNo.text.length>11) {
                strAadhaarNumber = etAadhaarNo.text.toString()
                strMaskedAadhaarNumber = etAadhaarNo.text.toString()
                etAadhaarNo.setText(commonMethods.getMaskNumber(strMaskedAadhaarNumber))
            } else {
                // The EditText has gained focus
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
            strMobileNumber = intent.getStringExtra("MobileNo")!!
            if(strMobileNumber.length>9)
            etMobileNo.setText(strMobileNumber)
            agentId = intent.getStringExtra("AgentId")!!
            secretKey = intent.getStringExtra("SecretKey")!!
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
                }/*else{
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
             val request = HashMap<String, String>()
             request["AgentId"] = agentId
             request["SecretKey"] = secretKey
             request["MobileNo"] = strMobileNumber
             service.fetchMasterData(request).let { response ->
                 try {
                     progressDialog.dismiss()
                     if (response.isSuccessful) {
                         Log.d("Master response API", response.toString());
                         Log.d("Master API response", response.body().toString());
                         val gson = Gson()
                         val jsonString: String = gson.toJson(response.body())
                         val it = Gson().fromJson(jsonString, EKycMasterDataResult::class.java)
                         if (it.message!!.uppercase() == "SUCCESS") {
                             kycDeviceList = it.ekycMasterData?.ekycDeviceList!!
                             kycConsent = it.ekycMasterData?.ekycConsent!!
                             withContext(Dispatchers.Main) {
                                 devicesAdapter.updateData(kycDeviceList)
                                 languageAdapter.updateData(kycConsent)
                                 tvOtpSendMessage.text = it.ekycMasterData?.ekycOtpMsg
                                 eKycToken = it.ekycMasterData?.ekycToken
                             }
                         } else {
                             print(it.message);
                             runOnUiThread{commonMethods.showMessageDialog(this,it.errorMessage,"Error","",false)}
                         }
                     } else {
                         runOnUiThread {
                             try {
                                 val jsonObj = JSONObject(response.errorBody()!!.charStream().readText().trim())
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
                     commonMethods.showMessageDialog(this,e.toString(),"Error","",false)
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
             val request = HashMap<String, String>()
             request["EkycToken"] = eKycToken!!
             request["MobileNo"] = strMobileNumber
             service.resendOtp(request).let { response ->
                 try {
                     progressDialog.dismiss()
                     if (response.isSuccessful) {
                         Log.d("resend OTP response API : ", response.toString());
                         Log.d("resend OTP API response :", response.body().toString());
                         val gson = Gson()
                         val jsonString: String = gson.toJson(response.body())
                         val it = Gson().fromJson(jsonString, ResendOtpData::class.java)
                         if (it.message!!.uppercase() == "SUCCESS") {
                             withContext(Dispatchers.Main) {
                                 eKycToken = it.eKycOtpData?.eKycToken
                                 commonMethods.showMessageDialog(this@KycActivity,it.eKycOtpData?.eKycOtpMsg,"Success","",false)
                             }
                         } else {
                             print(it.message);
                             runOnUiThread{commonMethods.showMessageDialog(this,it.errorMessage,"Error","",false)}
                         }
                     } else {
                         runOnUiThread {
                             try {
                                 val jsonObj = JSONObject(response.errorBody()!!.charStream().readText().trim())
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
                     commonMethods.showMessageDialog(this,e.toString(),"Error","",false)
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
             val request = HashMap<String, String>()
             request["kyc_token"] = eKycToken!!
             request["sender_mobile_no"] = strMobileNumber
             request["aadhaar_no"] = strAadhaarNumber
             request["capturedDeviceData"] = pidData
             request["consent"] = consent!!
             request["Device"] = kycDeviceList[spnDevices.selectedItemPosition].deviceName!!
             request["pan_no"] = etPanNo.text.toString()
             request["sender_pan_proof"] = "PAN"
             request["otp"] = etOtp.text.toString()
             request["app_type"] = ""
             service.doKyc(request).let { response ->
                 try {
                     progressDialog.dismiss()
                     if (response.isSuccessful) {
                         Log.d("kyc response API : ", response.toString());
                         Log.d("kyc API response :", response.body().toString());
                         val gson = Gson()
                         val jsonString: String = gson.toJson(response.body())
                         val it = Gson().fromJson(jsonString, DoKycResponse::class.java)
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
                                 val jsonObj = JSONObject(response.errorBody()!!.charStream().readText().trim())
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
}