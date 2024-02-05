package com.bacancy.ccs2androidhmi.views.listener

import androidx.fragment.app.Fragment

interface FragmentChangeListener {
    fun replaceFragment(fragment: Fragment?, shouldMoveToHomeScreen: Boolean = false)
}