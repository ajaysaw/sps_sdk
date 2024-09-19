package com.lib.sps

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.LayoutInflater
import android.widget.TextView


class CommonMethods {
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val n = cm.activeNetwork
        if (n != null) {
            val nc = cm.getNetworkCapabilities(n)
            return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        }
        return false
    }

    fun showMessageDialog(ctx: Context?, messageTxt: String?, argTitle: String?,kycStatus:String,isSuccess:Boolean) {
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
        if(number.isNotEmpty() && number.length>9){
            val lastFour = number.takeLast(4)
            val maskedPart = "*".repeat(number.length - 4)
            return maskedPart + lastFour
        }else{
            return ""
        }
    }
}