package com.imps_lib.app.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3 // Number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AddBeneficiaryFragment()
            1 -> DepositMoneyFragment()
            2 -> TransferToBankAccountFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }

}