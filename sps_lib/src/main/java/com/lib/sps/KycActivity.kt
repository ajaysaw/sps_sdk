package com.lib.sps

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.lib.sps.java_json.XML
import com.lib.sps.model.ConsentLanguage
import com.lib.sps.model.DoKycResponse
import com.lib.sps.model.DocProofType
import com.lib.sps.model.EKycMasterDataResult
import com.lib.sps.model.EkycDeviceList
import com.lib.sps.model.EsignWalletDataResponse
import com.lib.sps.model.PanApplied
import com.lib.sps.model.ResendOtpData
import com.lib.sps.model.ValidationData
import com.lib.sps.model.VerifyOtpData
import com.lib.sps.model.VerifyPanData
import com.lib.sps.network.ApiClient
import com.lib.sps.network.WebInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale


class KycActivity : AppCompatActivity(), OnClickListener {

    private val commonMethods = CommonMethods()
    private lateinit var authenticationAdapter: AuthenticationDropDownAdapter;
    private lateinit var languageAdapter: LanguageDropDownAdapter;
    private lateinit var devicesAdapter: DeviceDropDownAdapter;
    private lateinit var confirmationAdapter: ConfirmationPanDropDownAdapter;
    private lateinit var confirmationPanAppliedAdapter: ConfirmationPanAppliedDropDownAdapter;
    private lateinit var llConsent: LinearLayout
    private lateinit var panLayout: LinearLayout
    private lateinit var panAcknowledgementLayout: LinearLayout
    private lateinit var noPanLayout: LinearLayout
    private lateinit var etMobileNo: EditText
    private lateinit var etAadhaarNo: EditText
    private lateinit var etPanNo: EditText
    private lateinit var etFatherName: EditText
    private lateinit var etAgriculturalIncome: EditText
    private lateinit var etOtherAgriculturalIncome: EditText
    private lateinit var panVerifyText: TextView
    private lateinit var etOtp: EditText
    private lateinit var etPanAcknowledgementNumber: EditText
    private lateinit var etPanAcknowledgementDate: EditText
    private lateinit var otpVerifyText: TextView
    private lateinit var tvResendOtp: TextView
    private lateinit var cbConsent: CheckBox
    private lateinit var tvConsentContent: TextView
    private lateinit var tvOtpSendMessage: TextView
    private lateinit var spnAuthentication: Spinner
    private lateinit var spnConsentLanguage: Spinner
    private lateinit var spnDevices: Spinner
    private lateinit var confirmationPanSpinner: Spinner
    private lateinit var confirmationPanAppliedSpinner: Spinner
    private lateinit var tvSubmit: TextView
    private lateinit var tvCharges: TextView
    private lateinit var tvPlayStop: TextView
    private lateinit var progressDialog: Dialog
    private var strMobileNumber = ""
    private var EkycToken = ""
    private var strAadhaarNumber = ""
    private var strMaskedAadhaarNumber = ""
    private var job: Job? = null
    private var kycAuthenticationList = ArrayList<String>()
    private var kycDeviceList = ArrayList<EkycDeviceList>()
    private var kycFilterDeviceList = ArrayList<EkycDeviceList>()
    private var kycConsent= ArrayList<ConsentLanguage>()
    private var kycConfirmPanList= ArrayList<DocProofType>()
    private var kycConfirmPanAppliedList= ArrayList<PanApplied>()
    private var isConsentExpanded = false
    private var consent:String?=""
    private var lessConsent:String?=""
    private var audioPlay:String?=""
    private var eKycToken:String?=""
    var isUpdatingText = true
    private var panName:String?=""
    private lateinit var havePanCard:DocProofType
    private lateinit var havePanCardApplied:PanApplied
    private lateinit var validationData: ValidationData
    private var selectedDate: Calendar? = null
    private var verificationType: String? = ""
    private var kycCharges: String? = ""
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (commonMethods.checkLocationPermission(this, fusedLocationClient, settingsLauncher)) {
                checkKycWalletData()
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_kyc)
        progressDialog = commonMethods.progressDialog(this)
        etMobileNo = findViewById(R.id.etMobileNo)
        etAadhaarNo = findViewById(R.id.etAadhaarNo)
        panVerifyText = findViewById(R.id.panVerifyText)
        etPanNo = findViewById(R.id.etPanNo)
        etFatherName = findViewById(R.id.etFatherName)
        etAgriculturalIncome = findViewById(R.id.etAgriculturalIncome)
        etOtherAgriculturalIncome = findViewById(R.id.etOtherAgriculturalIncome)
        otpVerifyText = findViewById(R.id.otpVerifyText)
        etOtp = findViewById(R.id.etOtp)
        tvResendOtp = findViewById(R.id.tvResendOtp)
        llConsent = findViewById(R.id.llConsent)
        panLayout = findViewById(R.id.panLayout)
        noPanLayout = findViewById(R.id.noPanLayout)
        panAcknowledgementLayout = findViewById(R.id.panAcknowledgementLayout)
        cbConsent = findViewById(R.id.cbConsent)
        tvConsentContent = findViewById(R.id.tvConsentContent)
        tvOtpSendMessage = findViewById(R.id.tvOtpSendMessage)
        spnAuthentication = findViewById(R.id.authentication)
        spnConsentLanguage = findViewById(R.id.spnConsentLanguage)
        spnDevices = findViewById(R.id.spnDevices)
        confirmationPanSpinner = findViewById(R.id.confirmationPanSpinner)
        confirmationPanAppliedSpinner = findViewById(R.id.confirmationPanAppliedSpinner)
        tvSubmit = findViewById(R.id.tvSubmit)
        tvPlayStop = findViewById(R.id.btnPlayStop)
        etPanAcknowledgementNumber = findViewById(R.id.etPanAcknowledgementNumber)
        etPanAcknowledgementDate = findViewById(R.id.etPanAcknowledgementDate)
        tvCharges = findViewById(R.id.tvCharges)
        tvSubmit.setOnClickListener(this)
        tvConsentContent.setOnClickListener(this)
        tvResendOtp.setOnClickListener(this)
        otpVerifyText.setOnClickListener(this)
        panVerifyText.setOnClickListener(this)
        etPanAcknowledgementDate.setOnClickListener(this)
        tvPlayStop.setOnClickListener(this)
        etAadhaarNo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etAadhaarNo.text.isNotEmpty() && (etAadhaarNo.text.length==12 || etAadhaarNo.text.length==16) && isUpdatingText){
                    isUpdatingText = false
                    Validation().validateAadhaar(etAadhaarNo,etAadhaarNo.text.toString())
                    strAadhaarNumber = etAadhaarNo.text.toString()
                    strMaskedAadhaarNumber = etAadhaarNo.text.toString()
                    etAadhaarNo.setText(commonMethods.getMaskNumber(strMaskedAadhaarNumber))
                    etAadhaarNo.setSelection(etAadhaarNo.text.length)
                }else if(etAadhaarNo.text.startsWith("*") && etAadhaarNo.text.length==13 && isUpdatingText){
                    val lastChar = s?.lastOrNull() ?: return
                    strAadhaarNumber +=lastChar
                    strMaskedAadhaarNumber += lastChar
                    etAadhaarNo.setText(strMaskedAadhaarNumber)
                    etAadhaarNo.setSelection(etAadhaarNo.text.length)
                }else if(etAadhaarNo.text.startsWith("*") && (etAadhaarNo.text.length==11 || etAadhaarNo.text.length==15)){
                    etAadhaarNo.text.clear()
                }else{
                   isUpdatingText = true
                }
            }
            override fun afterTextChanged(s: Editable?) {
                println("Final text: ${s.toString()}")
            }
        })

        authenticationAdapter = AuthenticationDropDownAdapter(this@KycActivity, kycAuthenticationList)
        spnAuthentication.adapter = authenticationAdapter
        spnAuthentication.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selection = parent.selectedItemPosition
                spnAuthentication.setSelection(selection)
                if(kycAuthenticationList.size>0 && kycDeviceList.size>0){
                    kycFilterDeviceList.clear()
                    for(x in kycDeviceList){
                        if(kycAuthenticationList[selection].uppercase().replace(" ","")==x.deviceType){
                            kycFilterDeviceList.add(x)
                        }
                    }
                    devicesAdapter.updateData(kycFilterDeviceList)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                print("onNothingSelected")
            }
        }

        devicesAdapter = DeviceDropDownAdapter(this@KycActivity, kycFilterDeviceList)
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
                if(kycConsent[selection].audio!=null){
                    audioPlay = kycConsent[selection].audio
                    tvPlayStop.visibility = View.VISIBLE
                }else{
                    audioPlay = null
                    tvPlayStop.visibility = View.GONE
                }
                if (isPlaying) {
                    stopAudio()
                }
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
        confirmationAdapter = ConfirmationPanDropDownAdapter(this@KycActivity, kycConfirmPanList)
        confirmationPanSpinner.adapter = confirmationAdapter
        confirmationPanSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selection = parent.selectedItemPosition
                confirmationPanSpinner.setSelection(selection)
                havePanCard = kycConfirmPanList[selection]
                if(havePanCard.value?.uppercase()=="NO"){
                    noPanLayout.visibility = View.VISIBLE
                    panLayout.visibility = View.GONE
                    etAadhaarNo.isEnabled = true
                    spnDevices.isEnabled = true
                    spnDevices.alpha = 1.0f
                    spnAuthentication.isEnabled = true
                    spnAuthentication.alpha = 1.0f
                    if(havePanCardApplied.value?.uppercase()=="NO"){
                        etFatherName.requestFocus()
                    }else{
                        etPanAcknowledgementNumber.requestFocus()
                    }
                    etPanNo.text.clear()
                    if(validationData.eKycChargesMsg?.form60Msg!!.isNotEmpty())
                        tvCharges.text = validationData.eKycChargesMsg?.form60Msg
                }else{
                    noPanLayout.visibility = View.GONE
                    panLayout.visibility= View.VISIBLE
                    etAadhaarNo.isEnabled = false
                    spnDevices.isEnabled = false
                    spnDevices.alpha = 0.5f
                    spnAuthentication.isEnabled = false
                    spnAuthentication.alpha = 0.5f
                    etPanNo.requestFocus()
                    etFatherName.text.clear()
                    etAgriculturalIncome.text.clear()
                    etOtherAgriculturalIncome.text.clear()
                    if(validationData.eKycChargesMsg?.panMsg!!.isNotEmpty())
                        tvCharges.text = validationData?.eKycChargesMsg?.panMsg
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                print("onNothingSelected")
            }
        }

        confirmationPanAppliedAdapter = ConfirmationPanAppliedDropDownAdapter(this@KycActivity, kycConfirmPanAppliedList)
        confirmationPanAppliedSpinner.adapter = confirmationPanAppliedAdapter
        confirmationPanAppliedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selection = parent.selectedItemPosition
                confirmationPanAppliedSpinner.setSelection(selection)
                havePanCardApplied = kycConfirmPanAppliedList[selection]
                if(havePanCardApplied.value?.uppercase()=="NO"){
                    etFatherName.requestFocus()
                    panAcknowledgementLayout.visibility = View.GONE
                    etPanAcknowledgementNumber.visibility = View.GONE
                    etPanAcknowledgementDate.visibility =View.GONE
                }else{
                    panAcknowledgementLayout.visibility = View.VISIBLE
                    etPanAcknowledgementNumber.visibility = View.VISIBLE
                    etPanAcknowledgementDate.visibility = View.VISIBLE
                    etPanAcknowledgementNumber.requestFocus()
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
        if(commonMethods.checkLocationPermission(this@KycActivity,fusedLocationClient,settingsLauncher)){
            checkKycWalletData()
        }
    }

    override fun onClick(v: View?) {
        if (v != null && v.id == R.id.tvSubmit) {
            val validationUtil = Validation()
            if (havePanCard?.value?.uppercase() == "YES" && validationUtil.validatePanForm(
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
                var biometricActionData = BiometricUtils().callCapture(deviceDetails = kycFilterDeviceList[spnDevices.selectedItemPosition], biometricFormat = "", wadh = "", pidBlockNodes = kycFilterDeviceList[spnDevices.selectedItemPosition].pidBlockNodes,this);
                if(!biometricActionData.isError){
                    val intent = Intent(biometricActionData.action)
                    intent.setPackage(biometricActionData.packageName)
                    if(kycAuthenticationList[spnAuthentication.selectedItemPosition].uppercase().replace(" ","")=="FACEAUTH"){
                        intent.putExtra("request", biometricActionData.pidOptXML)
                    }else{
                        intent.putExtra("PID_OPTIONS", biometricActionData.pidOptXML)
                    }
                    bioMetricInfoActivityResultLauncher.launch(intent)
                }
            }else if(havePanCard?.value?.uppercase() == "NO" && validationUtil.validateForm60(
                    etMobile = etMobileNo,
                    etAadhaar = etAadhaarNo,
                    etFatherName = etFatherName,
                    etAgriculturalIncome = etAgriculturalIncome,
                    etOtherAgriculturalIncome = etOtherAgriculturalIncome,
                    etOtp = etOtp,
                    spnConsent = spnConsentLanguage,
                    cbConsent = cbConsent,
                    spnDevice = spnDevices,
                    tvConsentContent = tvConsentContent,
                    strAadhaarNumber = strAadhaarNumber,
                    strMobileNumber = strMobileNumber,
                    validationData= validationData,
                    etAcknowledgementNumber = etPanAcknowledgementNumber,
                    etAcknowledgementNumberDate = etPanAcknowledgementDate,
                )
            ){
                var biometricActionData = BiometricUtils().callCapture(deviceDetails = kycFilterDeviceList[spnDevices.selectedItemPosition], biometricFormat = "", wadh = "", pidBlockNodes = kycFilterDeviceList[spnDevices.selectedItemPosition].pidBlockNodes,this);
                if(!biometricActionData.isError){
                    val intent = Intent(biometricActionData.action)
                    intent.setPackage(biometricActionData.packageName)
                    if(kycAuthenticationList[spnAuthentication.selectedItemPosition].uppercase().replace(" ","")=="FACEAUTH"){
                        intent.putExtra("request", biometricActionData.pidOptXML)
                    }else{
                        intent.putExtra("PID_OPTIONS", biometricActionData.pidOptXML)
                    }
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
        }else if(v != null && v.id == R.id.otpVerifyText){
            if(etOtp.text.isEmpty() || etOtp.text.length<6){
                commonMethods.showMessageDialog(this,"Please Enter OTP", "Alert!","",false,verificationType!!,kycCharges!!)
            }else if (etOtp.text.isNotEmpty() && etOtp.isEnabled){
                verifyOTP()
            }
        }else if(v != null && v.id == R.id.panVerifyText && havePanCard?.value?.uppercase() == "YES"){
            if(etOtp.text.isEmpty()){
                etOtp.requestFocus()
            }else if(etPanNo.text.isEmpty() || etPanNo.text.length<10){
                commonMethods.showMessageDialog(this,"Please Enter PAN No.", "Alert!","",false,verificationType!!,kycCharges!!)
            }else if (etPanNo.text.isNotEmpty() && etPanNo.isEnabled){
                if(Validation().validatePan(etPanNo))
                    verifyPan()
            }
        }else if(v != null && v.id == R.id.etPanAcknowledgementDate){
            datePickerDialog()
        }else if(v!=null && v.id==R.id.btnPlayStop){
            if(audioPlay!=null){
                if (isPlaying) {
                    stopAudio()
                } else {
                    playAudio()
                }
            }
        }
    }

    private fun playAudio() {
        if (mediaPlayer == null) {
//            progressDialog.show()

            val resId = when (audioPlay) {
                "english_audio.mp3" -> R.raw.english_audio
                "hindi_audio.mp3" -> R.raw.hindi_audio
                "bengali_audio.mp3" -> R.raw.bengali_audio
                "kannada_audio.mp3" -> R.raw.kannada_audio
                "tamil_audio.mp3" -> R.raw.tamil_audio
                "malayalam_audio.mp3" -> R.raw.malayalam_audio
                "telugu_audio.mp3" -> R.raw.telugu_audio
                // Add other mappings as needed
                else -> 0 // Handle if the file isn't found
            }


            if (resId != 0) {
                mediaPlayer?.reset()
            mediaPlayer = MediaPlayer.create(this, resId).apply {
                setOnPreparedListener {
//                    progressDialog.dismiss()
                    start()
                }
                setOnCompletionListener {
                    stopAudio()
                }
                mediaPlayer?.prepareAsync()
            }
            } else {
                // Handle the case where the audio file is not found
//                progressDialog.dismiss()
                Toast.makeText(this, "Audio file not found!", Toast.LENGTH_SHORT).show()
            }
        } else {
            mediaPlayer?.start()
        }
        isPlaying = true
        tvPlayStop.text = getString(R.string.stop_audio)
        tvPlayStop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.play_stop, 0, 0, 0)
    }

    private fun stopAudio() {
        mediaPlayer?.let {
            it.stop()
            it.release()
            mediaPlayer = null
        }
        isPlaying = false
        tvPlayStop.text = getString(R.string.play_audio)
        tvPlayStop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.play_arrow, 0, 0, 0)
    }

    override fun onBackPressed() {
        // Check if the media player is playing
        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            // Stop the audio and release resources
            stopAudio()
            super.onBackPressed()
        } else {
            // If mediaPlayer is not playing, just proceed with the default back action
            super.onBackPressed()
        }
    }

    override fun onPause() {
        if(audioPlay!=null){
            if (isPlaying) {
                stopAudio()
            }
        }
        super.onPause()
    }

 private val bioMetricInfoActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        runOnUiThread {
            val data: Intent? = result.data
            if (result.resultCode == Activity.RESULT_OK) {
                val pidData = data?.getStringExtra("PID_DATA")
                if (pidData != null && kycAuthenticationList[spnAuthentication.selectedItemPosition].uppercase()=="BIOMETRIC") {
                    Log.e("Morpho Data :", pidData)
                    Log.e("pid Data :", pidData)
                    try {
                        val jsonObjPidData = XML.toJSONObject(pidData)
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
                }else if(kycAuthenticationList[spnAuthentication.selectedItemPosition].uppercase().replace(" ","")=="FACEAUTH"){
                    val b = data?.extras
                    if (b != null) {
                        val jsonObject = JSONObject()
                        for (key in b.keySet()) {
                            val value = b[key]
                            jsonObject.put(key, value)
                        }
                        val pidData = jsonObject.getString("response")
                        try {
                            val jsonObjPidData: com.lib.sps.java_json.JSONObject = XML.toJSONObject(pidData)
                            val jsonPid: com.lib.sps.java_json.JSONObject? = jsonObjPidData.getJSONObject("PidData")
                            val jsonResp = jsonPid?.getJSONObject("Resp")
                            val errCode = jsonResp?.getInt("errCode")
                            if (errCode == 0) {
                                Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                                doKyc(pidData.toString())
                            } else {
                                if (jsonResp!!.has("errInfo")) {
                                    val errInfo = """${jsonResp.getString("errInfo")}Please try again."""
                                    if (!TextUtils.isEmpty(errInfo)) {
                                        Toast.makeText(this, errInfo, Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(this, errInfo, Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        Toast.makeText(this, "NULL STRING RETURNED", Toast.LENGTH_LONG).show()
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

 private val form60ActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val message = data?.getStringExtra("message")
            val status = data?.getStringExtra("status")
            if(message!=null && status!=null){
                val resultIntent = Intent().apply {
                    putExtra("message", message)
                    putExtra("status", status)
                    putExtra("kycCharges",kycCharges)
                    putExtra("verificationType",verificationType)
                }
                setResult(Activity.RESULT_OK, resultIntent)
            }
            finish()
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
                    putExtra("kycCharges",kycCharges)
                    putExtra("verificationType",verificationType)
                }
                setResult(Activity.RESULT_OK, resultIntent)
            }
            finish()
        }
    }

 private fun getMasterData() {
     job = Coroutines.io {
         withContext(Dispatchers.Main) {
             progressDialog.show()
         }
         if(commonMethods.isNetworkConnected(this)){
             val service: WebInterface = ApiClient().createService(WebInterface::class.java, this)
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
                             if(kycAuthenticationList.size>0 && kycDeviceList.size>0){
                                for(x in kycDeviceList){
                                    if(kycAuthenticationList[0].uppercase().replace(" ","")==x.deviceType){
                                      kycFilterDeviceList.add(x)
                                    }
                                }
                             }
                             kycConsent = it.ekycMasterData?.ekycConsent!!
                             kycConfirmPanList = it.ekycMasterData?.docProofType!!
                             kycConfirmPanAppliedList = it.ekycMasterData?.panApplied!!
                             withContext(Dispatchers.Main) {
                                 if(it.ekycMasterData?.isScreenCaptureRestricted!!){
                                     window.setFlags(
                                         WindowManager.LayoutParams.FLAG_SECURE,
                                         WindowManager.LayoutParams.FLAG_SECURE
                                     )
                                 }
                                 authenticationAdapter.updateData(kycAuthenticationList)
                                 devicesAdapter.updateData(kycFilterDeviceList)
                                 languageAdapter.updateData(kycConsent)
                                 confirmationAdapter.updateData(kycConfirmPanList)
                                 confirmationPanAppliedAdapter.updateData(kycConfirmPanAppliedList)
                                 tvOtpSendMessage.text = it.ekycMasterData?.ekycOtpMsg
                                 eKycToken = it.ekycMasterData?.ekycToken
                                 strMobileNumber = it.ekycMasterData?.mobileNo.toString()
                                 validationData = it.ekycMasterData?.validationData!!
                                 if(strMobileNumber.length>9)
                                    etMobileNo.setText(strMobileNumber)
                                 etPanNo.isEnabled = false
                                 etAadhaarNo.isEnabled = false
                                 spnDevices.isEnabled = false
                                 spnDevices.alpha=0.5f;
                                 spnAuthentication.isEnabled = false
                                 spnAuthentication.alpha=0.5f;
                                 confirmationPanSpinner.isEnabled = false
                                 confirmationPanSpinner.alpha=0.5f;
                                 if(it.ekycMasterData?.validationData?.eKycChargesMsg?.panMsg!!.isNotEmpty()){
                                     tvCharges.visibility = View.VISIBLE
                                     tvCharges.text =it.ekycMasterData?.validationData?.eKycChargesMsg?.panMsg
                                 }
                                 val maxLength = it.ekycMasterData?.validationData?.panAckNoLengthMax?.toInt()
                                 etPanAcknowledgementNumber.filters = arrayOf(InputFilter.LengthFilter(maxLength!!))

                                 val defaultIndex = kycConfirmPanAppliedList.indexOfFirst { it.defaultSelection == "YES" }
                                 val selectedIndex = if (defaultIndex != -1) defaultIndex else 0
                                 confirmationPanAppliedSpinner.setSelection(selectedIndex)
                             }
                         } else {
                             runOnUiThread{commonMethods.showMessageDialog(this,it.errorMessage,"Error","",false,verificationType!!,kycCharges!!)}
                         }
                     } else {
                         runOnUiThread {
                             try {
                                 val decryptData = commonMethods.aesDecrypt(JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim())
                                 val jsonObj = JSONObject(decryptData)
                                 if (jsonObj.has("message") && jsonObj.getString("message").uppercase() == "FAILURE"){
                                     if(jsonObj.has("kyc_status") && jsonObj.getString("kyc_status").uppercase() == "SUCCESS"){
                                         commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error",jsonObj.getString("kyc_status"),true,verificationType!!,kycCharges!!)
                                     }else{
                                         commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false,verificationType!!,kycCharges!!)
                                     }
                                 } else
                                     commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false,verificationType!!,kycCharges!!)
                             } catch (e: Exception) {
                                 commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false,verificationType!!,kycCharges!!)
                             }
                         }
                     }
                 } catch (e: Exception) {
                     //onError("$response", true)
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

 private fun resendOTP(){
     job = Coroutines.io {
         withContext(Dispatchers.Main) {
             progressDialog.show()
         }
         if(commonMethods.isNetworkConnected(this)){
             val service: WebInterface = ApiClient().createService(WebInterface::class.java,this)
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
                                 commonMethods.showMessageDialog(this@KycActivity,it.eKycOtpData?.eKycOtpMsg,"Success","",false,verificationType!!,kycCharges!!)
                             }
                         } else {
                             runOnUiThread{commonMethods.showMessageDialog(this,it.errorMessage,"Error","",false,verificationType!!,kycCharges!!)}
                         }
                     } else {
                         runOnUiThread {
                             try {
                                 val decryptData = commonMethods.aesDecrypt(JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim())
                                 val jsonObj = JSONObject(decryptData)
                                 if (jsonObj.has("message") && jsonObj.getString("message").uppercase() == "FAILURE") {
                                     commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false,verificationType!!,kycCharges!!)
                                 } else
                                     commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false,verificationType!!,kycCharges!!)
                             } catch (e: Exception) {
                                 commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false,verificationType!!,kycCharges!!)
                             }
                         }
                     }
                 } catch (e: Exception) {
                     //onError("$response", true)
                     print(e);
                     progressDialog.dismiss()
                     runOnUiThread {commonMethods.showMessageDialog(this,e.toString(),"Error","",false,verificationType!!,kycCharges!!)}
                 }
             }
         }else{
             progressDialog.dismiss()
             runOnUiThread {
                 commonMethods.showMessageDialog(this, "No Internet....Please be connected to a working internet", "Alert!","",false,verificationType!!,kycCharges!!)
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
             val service: WebInterface = ApiClient().createService(WebInterface::class.java,this)
             val jsonObject = JSONObject()
             jsonObject.put("kyc_token", eKycToken!!)
             jsonObject.put("sender_mobile_no", strMobileNumber)
             jsonObject.put("aadhaar_no", strAadhaarNumber)
             jsonObject.put("capturedDeviceData", pidData)
             jsonObject.put("consent", consent!!)
             jsonObject.put("Device", kycFilterDeviceList[spnDevices.selectedItemPosition].deviceName!!)
             jsonObject.put("authentication_type", kycAuthenticationList[spnAuthentication.selectedItemPosition].replace(" ","").uppercase())
             if(havePanCard.value?.uppercase()=="NO"){
                 jsonObject.put("father_name", etFatherName.text.toString())
                 jsonObject.put("agricultural_income", etAgriculturalIncome.text.toString())
                 jsonObject.put("other_than_agricultural_income", etOtherAgriculturalIncome.text.toString())
                 jsonObject.put("pan_applied",havePanCardApplied.value)
                 if(havePanCardApplied.value?.uppercase()=="YES"){
                     jsonObject.put("pan_acknowledgement_no",etPanAcknowledgementNumber.text.toString())
                     jsonObject.put("pan_acknowledgement_date",etPanAcknowledgementDate.text.toString())
                 }
             }else{
                 jsonObject.put("pan_no", etPanNo.text.toString())
                 jsonObject.put("panName", panName)
             }
             jsonObject.put("sender_pan_proof", havePanCard.key)
             jsonObject.put("otp", etOtp.text.toString())
             jsonObject.put("app_type", "MOBILE")
             jsonObject.put("latitude", CommonMethods.latitude.toString())
             jsonObject.put("longitude", CommonMethods.longitude.toString())
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
                                     kycCharges = it.eKycResponse?.fORM60PdfGenResponse?.kycCharges
                                     verificationType = it.eKycResponse?.fORM60PdfGenResponse?.verificationType;
                                     if(havePanCard.key?.uppercase()=="PAN"){
                                         commonMethods.showMessageDialog(this@KycActivity,it.eKycResponse?.eKycMessage,it.eKycResponse?.status,it.eKycResponse?.status!!,true,verificationType!!,kycCharges!!)
                                     }else{
                                         if(it.eKycResponse?.fORM60PdfGenResponse!=null){
                                             if(it.eKycResponse?.fORM60PdfGenResponse!!.isWebView!!){
                                                 val intent = Intent(this@KycActivity, Form60Activity::class.java)
                                                intent.putExtra("EkycToken", eKycToken)
                                                intent.putExtra("PDF_URL", it.eKycResponse?.fORM60PdfGenResponse!!.pdfUrl)
                                                intent.putExtra("REDIRECT_URL", it.eKycResponse?.fORM60PdfGenResponse!!.eSignUrl)
                                                intent.putExtra("ESIGN_SUCCESS_URL", it.eKycResponse?.fORM60PdfGenResponse!!.eSignSuccessUrl)
                                                intent.putExtra("ESIGN_FAILED_URL", it.eKycResponse?.fORM60PdfGenResponse!!.eSignFailedUrl)
                                                intent.putStringArrayListExtra("content", it.eKycResponse?.fORM60PdfGenResponse!!.content)
                                                intent.putExtra("verificationType", verificationType)
                                                intent.putExtra("kycCharges", kycCharges)
                                                form60ActivityResultLauncher.launch(intent)
                                             }
                                         }
                                     }
                                 }else{
                                     commonMethods.showMessageDialog(this@KycActivity,it.eKycResponse?.eKycMessage,it.eKycResponse?.status,it.eKycResponse?.status!!,false,verificationType!!,kycCharges!!)
                                 }
                             }
                         } else {
                             runOnUiThread{commonMethods.showMessageDialog(this,it.errorMessage,"Error","",false,verificationType!!,kycCharges!!)}
                         }
                     } else {
                         runOnUiThread {
                             try {
                                 val decryptData = commonMethods.aesDecrypt(JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim())
                                 val jsonObj = JSONObject(decryptData)
                                 if (jsonObj.has("message") && jsonObj.getString("message").uppercase() == "FAILURE") {
                                     commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false,verificationType!!,kycCharges!!)
                                 } else {
                                     commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false,verificationType!!,kycCharges!!)
                                 }
                             } catch (e: Exception) {
                                 commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false,verificationType!!,kycCharges!!)
                             }
                         }
                     }
                 } catch (e: Exception) {
                     //onError("$response", true)
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

 private fun verifyOTP(){
        job = Coroutines.io {
            withContext(Dispatchers.Main) {
                progressDialog.show()
            }
            if(commonMethods.isNetworkConnected(this)){
                val service: WebInterface = ApiClient().createService(WebInterface::class.java,this)
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
                                    otpVerifyText.background = ContextCompat.getDrawable(this@KycActivity, R.drawable.edittext_verified_drawable)
                                    otpVerifyText.setText(R.string.verified)
                                    otpVerifyText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this@KycActivity, R.color.white)))
                                    panVerifyText.background = ContextCompat.getDrawable(this@KycActivity, R.drawable.edittext_verify_drawable)
                                    panVerifyText.setText(R.string.verify)
                                    panVerifyText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this@KycActivity, R.color.white)))
                                    etPanNo.requestFocus()
                                    tvResendOtp.visibility = View.GONE
                                    tvOtpSendMessage.visibility = View.GONE
                                    confirmationPanSpinner.isEnabled = true
                                    confirmationPanSpinner.alpha=1.0f
                                }
                            } else {
                                runOnUiThread{commonMethods.showMessageDialog(this,it.errorMessage,"Error","",false,verificationType!!,kycCharges!!)}
                            }
                        } else {
                            runOnUiThread {
                                try {
                                    val decryptData = commonMethods.aesDecrypt(JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim())
                                    val jsonObj = JSONObject(decryptData)
                                    if (jsonObj.has("message") && jsonObj.getString("message").uppercase() == "FAILURE") {
                                        commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false,verificationType!!,kycCharges!!)
                                    } else
                                        commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false,verificationType!!,kycCharges!!)
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false,verificationType!!,kycCharges!!)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e);
                        progressDialog.dismiss()
                        runOnUiThread {commonMethods.showMessageDialog(this,e.toString(),"Error","",false,verificationType!!,kycCharges!!)}
                    }
                }
            }else{
                progressDialog.dismiss()
                runOnUiThread {
                    commonMethods.showMessageDialog(this, "No Internet....Please be connected to a working internet", "Alert!","",false,verificationType!!,kycCharges!!)
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
                val service: WebInterface = ApiClient().createService(WebInterface::class.java,this)
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
                                    spnDevices.alpha = 1.0f
                                    spnAuthentication.isEnabled = true
                                    spnAuthentication.alpha = 1.0f
                                    confirmationPanSpinner.isEnabled = false
                                    confirmationPanSpinner.alpha =1.0f
                                    panVerifyText.background = ContextCompat.getDrawable(this@KycActivity, R.drawable.edittext_verified_drawable)
                                    panVerifyText.setText(R.string.verified)
                                    panVerifyText.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this@KycActivity, R.color.white)))
                                    etAadhaarNo.requestFocus()
                                }
                            } else {
                                runOnUiThread{commonMethods.showMessageDialog(this,it.errorMessage,"Error","",false,verificationType!!,kycCharges!!)}
                            }
                        } else {
                            runOnUiThread {
                                try {
                                    val decryptData = commonMethods.aesDecrypt(JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim())
                                    val jsonObj = JSONObject(decryptData)
                                    if (jsonObj.has("message") && jsonObj.getString("message").uppercase() == "FAILURE") {
                                        commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false,verificationType!!,kycCharges!!)
                                    } else
                                        commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false,verificationType!!,kycCharges!!)
                                } catch (e: Exception) {
                                    commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false,verificationType!!,kycCharges!!)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        //onError("$response", true)
                        print(e);
                        progressDialog.dismiss()
                        runOnUiThread {commonMethods.showMessageDialog(this,e.toString(),"Error","",false,verificationType!!,kycCharges!!)}
                    }
                }
            }else{
                progressDialog.dismiss()
                runOnUiThread {
                    commonMethods.showMessageDialog(this, "No Internet....Please be connected to a working internet", "Alert!","",false,verificationType!!,kycCharges!!)
                }
            }
        }
    }

 private fun checkKycWalletData(){
     job = Coroutines.io {
         withContext(Dispatchers.Main) {
             progressDialog.show()
         }
         if(commonMethods.isNetworkConnected(this)){
             val service: WebInterface = ApiClient().createService(WebInterface::class.java,this)
             val jsonObject = JSONObject()
             jsonObject.put("EkycToken", EkycToken)
             jsonObject.put("app_type", "MOBILE")
             val requestData = HashMap<String, String>()
             requestData["data"] = commonMethods.aesEncrypt(jsonObject.toString())
             service.getKycWalletData(requestData).let { response ->
                 try {
                     progressDialog.dismiss()
                     if (response.isSuccessful) {
                         Log.d("wallet data response API : ", response.toString());
                         Log.d("wallet data API response :", response.body().toString());
                         val jsonString: String = Gson().toJson(response.body())
                         val decryptData = commonMethods.aesDecrypt(JSONObject(jsonString).getString("data"))
                         val it = Gson().fromJson(decryptData, EsignWalletDataResponse::class.java)
                         if (it.message!!.uppercase() == "SUCCESS") {
                             withContext(Dispatchers.Main) {
                                 kycCharges = if(it.eKycWalletData?.VerificationType=="FORM-60"){
                                     it.eKycWalletData?.kycCharges?.form60?.kycChargesAmount
                                 }else{
                                     it.eKycWalletData?.kycCharges?.pan?.kycChargesAmount
                                 }
                                 verificationType = it.eKycWalletData?.VerificationType;
                               if(it.eKycWalletData?.KycStatus?.uppercase()=="Y" && it.eKycWalletData?.VerificationType=="FORM-60"){
                                   if(it.eKycWalletData?.Form60EsignStatus?.uppercase()=="PENDING"){
                                       if(it.eKycWalletData?.isWebView!!){
                                           val intent = Intent(this@KycActivity, EsignActivity::class.java)
                                           intent.putExtra("EkycToken",EkycToken)
                                           intent.putExtra("ESIGN_URL",it.eKycWalletData?.Form60EsignUrl)
                                           intent.putExtra("ESIGN_SUCCESS_URL",it.eKycWalletData?.eSignSuccessUrl)
                                           intent.putExtra("ESIGN_FAILED_URL", it.eKycWalletData?.eSignFailedUrl)
                                           intent.putExtra("verificationType", verificationType)
                                           intent.putExtra("kycCharges", kycCharges)
                                           eSignActivityResultLauncher.launch(intent)
                                       }
                                   }else{
                                       commonMethods.showMessageDialog(this@KycActivity,it.eKycWalletData?.form60EsignSuccessMsg,it.eKycWalletData?.Form60EsignStatus!!,it.eKycWalletData?.Form60EsignStatus!!,true,verificationType!!,kycCharges!!)
                                   }
                               }else{
                                   getMasterData() //call to master API
                               }
                             }
                         } else {
                             runOnUiThread{commonMethods.showMessageDialog(this,it.errorMessage,"Error","",false,verificationType!!,kycCharges!!)}
                         }
                     } else {
                         runOnUiThread {
                             try {
                                 val decryptData = commonMethods.aesDecrypt(JSONObject(response.errorBody()!!.charStream().readText().trim()).getString("data").trim())
                                 val jsonObj = JSONObject(decryptData)
                                 if (jsonObj.has("message") && jsonObj.getString("message").uppercase() == "FAILURE") {
                                     commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false,verificationType!!,kycCharges!!)
                                 } else
                                     commonMethods.showMessageDialog(this, jsonObj.getString("error_message"), "Error","",false,verificationType!!,kycCharges!!)
                             } catch (e: Exception) {
                                 commonMethods.showMessageDialog(this, e.message.toString(), "Error","",false,verificationType!!,kycCharges!!)
                             }
                         }
                     }
                 } catch (e: Exception) {
                     //onError("$response", true)
                     print(e);
                     progressDialog.dismiss()
                     runOnUiThread {commonMethods.showMessageDialog(this,e.toString(),"Error","",false,verificationType!!,kycCharges!!)}
                 }
             }
         }else{
             progressDialog.dismiss()
             runOnUiThread {
                 commonMethods.showMessageDialog(this, "No Internet....Please be connected to a working internet", "Alert!","",false,verificationType!!,kycCharges!!)
             }
         }
     }
 }

    private fun datePickerDialog(){
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, validationData.panAckDateDuration!!.toInt())
        val thirtyDaysAgo = calendar.timeInMillis
        selectedDate?.let {
            calendar.timeInMillis = it.timeInMillis
        } ?: run {
            calendar.timeInMillis = today
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDayOfMonth)
                }
                //val selectedDateFormatted = "$selectedDayOfMonth-${selectedMonth + 1}-$selectedYear"
                val dateFormat = SimpleDateFormat(validationData.panAckDateFormat, Locale.getDefault())
                val selectedDateFormatted = dateFormat.format(selectedDate!!.time)
                etPanAcknowledgementDate.setText(selectedDateFormatted)
            },
            year, month, dayOfMonth
        )
        datePickerDialog.datePicker.minDate = thirtyDaysAgo
        datePickerDialog.datePicker.maxDate = today
        datePickerDialog.show()
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == commonMethods.LOCATION_PERMISSION_REQUEST) {
            if(commonMethods.checkLocationPermission(this@KycActivity,fusedLocationClient,settingsLauncher)){
                checkKycWalletData()
            }
        }
    }
}