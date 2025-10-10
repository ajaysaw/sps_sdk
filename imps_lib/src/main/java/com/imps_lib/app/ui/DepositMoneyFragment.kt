package com.imps_lib.app.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.imps_lib.app.R

class DepositMoneyFragment : Fragment() {
    private lateinit var tableLayout: TableLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_deposit_money, container, false)
        tableLayout = rootView.findViewById(R.id.table_layout)
        val data = listOf(
            Pair("100-1000", "Rs. 10"),
            Pair("1001-2000", "Rs. 15"),
            Pair("2001-3000", "Rs. 20"),
            Pair("3001-4000", "Rs. 25"),
            Pair("4001-5000", "Rs. 30"),
            Pair("5001-10000", "Rs. 50"),
            Pair("10001-25000", "Rs. 100")
        )
        data.forEach { row ->
            val tableRow = TableRow(requireContext()).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT,
                )
            }
            val amountCell = TextView(requireContext()).apply {
                text = row.first
                setPadding(0, 16, 0, 16)
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#194086"))
                TextViewCompat.setTextAppearance(this, R.style.TextViewStyleBold)
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            val chargeCell = TextView(requireContext()).apply {
                text = row.second
                setPadding(0, 16, 0, 16)
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#194086"))
                TextViewCompat.setTextAppearance(this, R.style.TextViewStyleBold)
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            tableRow.addView(amountCell)
            tableRow.addView(chargeCell)
            val rowIndex = tableLayout.childCount
            tableRow.setBackgroundColor(
                if (rowIndex % 2 == 0) Color.parseColor("#F5F5F5") else Color.WHITE
            )
            tableLayout.addView(tableRow)
        }
        return rootView
    }
}