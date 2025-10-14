package com.imps_lib.app.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.imps_lib.app.R
import com.imps_lib.app.model.BeneficiaryData

class BeneficiaryAdapter(
    private var list: List<BeneficiaryData>,
    private val onActionClick: (BeneficiaryData, String) -> Unit
) : RecyclerView.Adapter<BeneficiaryAdapter.BeneficiaryViewHolder>() {

    inner class BeneficiaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBeneficiaryName: TextView = view.findViewById(R.id.tvBeneficiaryName)
        val tvBankName: TextView = view.findViewById(R.id.tvBankName)
        val tvAccountNumber: TextView = view.findViewById(R.id.tvAccountNumber)
        val tvIfsc: TextView = view.findViewById(R.id.tvIfsc)

        //        val btnVerify: TextView = view.findViewById(R.id.btnVerify)
        val btnTransfer: TextView = view.findViewById(R.id.btnTransfer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeneficiaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_beneficiary, parent, false)
        return BeneficiaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: BeneficiaryViewHolder, position: Int) {
        val beneficiary = list[position]
        holder.tvBeneficiaryName.text = beneficiary.beneficiaryName
        holder.tvBankName.text = beneficiary.bankName
        holder.tvAccountNumber.text = beneficiary.accountNumber
        holder.tvIfsc.text = beneficiary.ifsc



        if (beneficiary.isCoolingPeriodPassed == true) {
            // Enabled transfer
            holder.btnTransfer.isEnabled = true
            holder.btnTransfer.setBackgroundResource(R.drawable.button_drawable_rectangle)
            holder.btnTransfer.setTextColor(Color.WHITE)
            holder.btnTransfer.setOnClickListener {
                onActionClick(beneficiary, "transfer")
            }
        } else {
            // Disabled look & click behavior
            holder.btnTransfer.isEnabled = true // Keep clickable to show message
            holder.btnTransfer.setBackgroundResource(R.drawable.button_drawable_rectangle_disabled)
            holder.btnTransfer.setTextColor(Color.DKGRAY)
            holder.btnTransfer.setOnClickListener {
                Toast.makeText(
                    holder.itemView.context,
                    "Cooling period not yet over for this beneficiary.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<BeneficiaryData>) {
        list = newList
        notifyDataSetChanged()
    }
}
