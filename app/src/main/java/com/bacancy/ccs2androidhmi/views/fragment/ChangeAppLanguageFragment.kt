package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.viewModels
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentChangeAppLanguageBinding
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class ChangeAppLanguageFragment : BaseFragment() {

    private lateinit var binding: FragmentChangeAppLanguageBinding
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeAppLanguageBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()
        return binding.root
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_settings)
    }

    override fun setupViews() {}

    override fun handleClicks() {
        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) return
                val selectedLanguage = parent?.getItemAtPosition(position).toString()
                setAppLanguage(selectedLanguage)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setAppLanguage(language: String) {
        val locale = when (language) {
            "English" -> Locale.ENGLISH
            "Spanish" -> Locale("es")
            "French" -> Locale("fr")
            else -> Locale.getDefault()
        }

        Locale.setDefault(locale)
        val resources = resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        //requireActivity().recreate()
    }

}