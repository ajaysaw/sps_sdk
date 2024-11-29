package com.lib.sps.mr

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.lib.sps.R

class MrHomeActivity : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mr_home)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        val adapter = TabPagerAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Add Beneficiary"
                1 -> "Deposit Money"
                2 -> "Transfer To Bank Account"
                else -> "Tab ${position + 1}"
            }
        }.attach()
    }

    class TabPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3 // Number of tabs

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AddBeneficiaryFragment()
                1 -> DepositMoneyFragment()
                2 -> TransferToBankAccountFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }
}