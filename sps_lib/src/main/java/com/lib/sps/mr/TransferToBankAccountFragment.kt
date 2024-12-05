package com.lib.sps.mr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.lib.sps.CommonMethods
import com.lib.sps.R
class TransferToBankAccountFragment : Fragment() ,OnClickListener{
    private val commonMethods = CommonMethods()
    private lateinit var tvTransferBeneficiaryName: TextView
    private lateinit var tvTransferAccountNumber: TextView
    private lateinit var tvTransferBank: TextView
    private lateinit var tvTransferIfscCode: TextView
    private lateinit var btnVerify: TextView
    private lateinit var tvProceedToPay: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_transfer_to_bank_account, container, false)
        tvTransferBeneficiaryName = rootView.findViewById(R.id.tvTransferBeneficiaryName)
        tvTransferAccountNumber = rootView.findViewById(R.id.tvTransferAccountNumber)
        tvTransferBank = rootView.findViewById(R.id.tvTransferBank)
        tvTransferIfscCode = rootView.findViewById(R.id.tvTransferIfscCode)
        btnVerify = rootView.findViewById(R.id.btnVerify)
        tvProceedToPay = rootView.findViewById(R.id.tvProceedToPay)
        btnVerify.setOnClickListener(this)
        tvProceedToPay.setOnClickListener(this)
        return rootView
    }

    override fun onClick(v: View?) {
        if (v != null && v.id == R.id.btnVerify) {
            commonMethods.verifyDialog(requireContext())
        }else if(v != null && v.id == R.id.btnVerify){

        }
    }
}