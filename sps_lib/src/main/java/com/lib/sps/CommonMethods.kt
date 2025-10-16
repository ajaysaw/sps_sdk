package com.lib.sps

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.LayoutInflater
import android.widget.TextView
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import android.util.Base64
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import javax.crypto.spec.SecretKeySpec


class CommonMethods {
    private val ALGORITHM = "AES"
    private val TRANSFORMATION = "AES/CBC/PKCS7Padding"
    val LOCATION_PERMISSION_REQUEST = 1001
    private val TAG = "KycActivity"
    companion object {
        var latitude: Double? = 0.0
        var longitude: Double? = 0.0
    }


    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val n = cm.activeNetwork
        if (n != null) {
            val nc = cm.getNetworkCapabilities(n)
            return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        }
        return false
    }

    fun showMessageDialog(ctx: Context?, messageTxt: String?, argTitle: String?,kycStatus:String,isSuccess:Boolean,verificationType:String,kycCharges:String) {
        val dialog = Dialog(ctx!!, R.style.CustomDialogStyle)
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
            if(isSuccess){
                val activity = ctx as? Activity
                val resultIntent = Intent()
                resultIntent.putExtra("message", messageTxt)
                resultIntent.putExtra("status", kycStatus)
                resultIntent.putExtra("kycCharges",kycCharges)
                resultIntent.putExtra("verificationType",verificationType)
                activity?.setResult(Activity.RESULT_OK, resultIntent)
                activity?.finish()
            }
        }
        dialog.show()
    }

    fun progressDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        val inflate = LayoutInflater.from(context).inflate(R.layout.progress_bar_view, null)
        dialog.setContentView(inflate)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        return dialog
    }

    fun getUserAgent(): String{
        return System.getProperty("http.agent")?:"";
    }

    fun getMaskNumber(number:String):String{
        if(number.isNotEmpty() && (number.length==12 || number.length==16)){
            val lastFour = number.takeLast(4)
            val maskedPart = "*".repeat(number.length - 4)
            return maskedPart + lastFour
        }else{
            return ""
        }
    }

    fun aesEncrypt(data: String): String {
        val plainText = data.toByteArray()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, generateSecretKey(), generateIV())
        val encrypt = cipher.doFinal(plainText)
        return Base64.encodeToString(encrypt, Base64.DEFAULT)
    }

    fun aesDecrypt(encryptedData: String): String {
        val textToDecrypt = Base64.decode(encryptedData, Base64.DEFAULT)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, generateSecretKey(), generateIV())
        val decrypt = cipher.doFinal(textToDecrypt)
        return String(decrypt)
    }

    private fun generateSecretKey(): SecretKey {
        val secretKey = SecretKeySpec(BuildConfig.secretKey.toByteArray(), ALGORITHM)
        return secretKey
    }

    private fun generateIV(): IvParameterSpec {
        return IvParameterSpec(BuildConfig.IvParameterSpec.toByteArray())
    }

    private fun getUserLocation(activity: Activity,fusedLocationClient: FusedLocationProviderClient) {
        try {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    try {
                        if (location != null) {
                           latitude = location.latitude
                            longitude = location.longitude
                        } else {
                            Log.e(TAG, "Location not available")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error reading location: ${e.message}", e)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to get location", e)
                }

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
        }
    }

    fun checkLocationPermission(activity: Activity, fusedLocationClient: FusedLocationProviderClient, settingsLauncher: ActivityResultLauncher<Intent>): Boolean {
        val fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION
        return when {
            ContextCompat.checkSelfPermission(activity, fineLocation) == PackageManager.PERMISSION_GRANTED -> {
                getUserLocation(activity, fusedLocationClient)
                true
            }
            ActivityCompat.shouldShowRequestPermissionRationale(activity, fineLocation) -> {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(fineLocation, coarseLocation),
                    LOCATION_PERMISSION_REQUEST
                )
                false
            }
            else -> {
                val prefs = activity.getSharedPreferences("sps_location_permissions", Context.MODE_PRIVATE)
                val askedBefore = prefs.getBoolean("askedLocation", false)
                if (askedBefore) {
                    openAppSettings(settingsLauncher)
                } else {
                    ActivityCompat.requestPermissions(activity, arrayOf(fineLocation), LOCATION_PERMISSION_REQUEST)
                    prefs.edit().putBoolean("askedLocation", true).apply()
                }
                false
            }
        }
    }


    private fun openAppSettings(settingsLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            android.net.Uri.fromParts("package", "com.sps.sdk", null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        settingsLauncher.launch(intent)
    }
}