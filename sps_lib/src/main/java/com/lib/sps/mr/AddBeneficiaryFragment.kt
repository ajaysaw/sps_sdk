package com.lib.sps.mr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.lib.sps.CommonMethods
import com.lib.sps.R
class AddBeneficiaryFragment : Fragment() , OnClickListener {
    private lateinit var etBeneficiaryName: EditText
    private lateinit var etSelectBank: EditText
    private lateinit var etIfscCode: EditText
    private lateinit var etBeneficiaryAccountNumber: EditText
    private lateinit var etConfirmBeneficiaryAccountNumber: EditText
    private lateinit var tvVerify: TextView
    private lateinit var tvCharge: TextView
    private lateinit var tvSubmit: TextView
    private val commonMethods = CommonMethods()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_add_beneficiary, container, false)
        etBeneficiaryName = rootView.findViewById(R.id.etBeneficiaryName)
        etSelectBank = rootView.findViewById(R.id.etSelectBank)
        etIfscCode = rootView.findViewById(R.id.etIfscCode)
        etBeneficiaryAccountNumber = rootView.findViewById(R.id.etBeneficiaryAccountNumber)
        etConfirmBeneficiaryAccountNumber = rootView.findViewById(R.id.etConfirmBeneficiaryAccountNumber)
        tvVerify = rootView.findViewById(R.id.tvVerify)
        tvCharge = rootView.findViewById(R.id.tvCharge)
        tvSubmit = rootView.findViewById(R.id.tvSubmit)
        tvSubmit.setOnClickListener(this)
        tvVerify.setOnClickListener(this)
        tvCharge.text = getString(R.string.charges)+" "+getString(R.string.rupee_symbol)+"4"
        return rootView
    }

    override fun onClick(v: View?) {
        if (v != null && v.id == R.id.tvSubmit) {
            tvSubmit.background =ContextCompat.getDrawable(requireContext(), R.drawable.button_drawable)
            tvSubmit.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tvVerify.background =ContextCompat.getDrawable(requireContext(), R.drawable.border_button_drawable)
            tvVerify.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }else if(v != null && v.id == R.id.tvVerify){
            tvVerify.background =ContextCompat.getDrawable(requireContext(), R.drawable.button_drawable)
            tvVerify.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tvSubmit.background =ContextCompat.getDrawable(requireContext(), R.drawable.border_button_drawable)
            tvSubmit.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }
}