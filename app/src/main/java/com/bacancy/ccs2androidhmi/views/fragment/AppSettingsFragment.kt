package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentChangeAppLanguageBinding
import com.bacancy.ccs2androidhmi.util.AppConfig
import com.bacancy.ccs2androidhmi.util.DialogUtils.showPasswordPromptDialog
import com.bacancy.ccs2androidhmi.util.DialogUtils.showSelectAppLanguageDialog
import com.bacancy.ccs2androidhmi.util.LanguageConfig.getAppLanguage
import com.bacancy.ccs2androidhmi.util.LanguageConfig.languageList
import com.bacancy.ccs2androidhmi.util.LanguageConfig.setAppLanguage
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.util.ToastUtils.showCustomToast
import com.bacancy.ccs2androidhmi.util.gone
import com.bacancy.ccs2androidhmi.util.visible
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppSettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentChangeAppLanguageBinding

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeAppLanguageBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()
        handleSettingsVisibility()
        return binding.root
    }

    private fun handleSettingsVisibility() {
        binding.apply {
            if (AppConfig.SHOW_LOCAL_START_STOP) {
                binding.tvLocalStartStop.visible()
            } else {
                binding.tvLocalStartStop.gone()
            }

            if (AppConfig.SHOW_TEST_MODE) {
                binding.tvTestMode.visible()
            } else {
                binding.tvTestMode.gone()
            }
        }
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_settings)
    }

    override fun setupViews() {
        binding.apply {
            tvSelectLanguage.text = getString(R.string.lbl_change_app_language, getLanguageName())
            if (prefHelper.getBoolean(
                    PrefHelper.IS_DARK_THEME,
                    false
                )
            ) {
                tvSwitchMode.text = getString(R.string.lbl_switch_to_light_mode)
            } else {
                tvSwitchMode.text = getString(R.string.lbl_switch_to_dark_mode)
            }
        }
    }

    private fun getLanguageName(): String {
        val languageCode = getAppLanguage(prefHelper)
        return languageList.find { it.code == languageCode }?.name ?: "English"
    }

    override fun handleClicks() {
        binding.tvSelectLanguage.setOnClickListener {
            requireActivity().showSelectAppLanguageDialog(prefHelper) {
                requireActivity().setAppLanguage(it.code, prefHelper)
                (requireActivity() as HMIDashboardActivity).goBack()
            }
        }

        binding.tvSwitchMode.setOnClickListener {
            (requireActivity() as HMIDashboardActivity).toggleTheme()
        }

        binding.tvFirmwareInformation.setOnClickListener {
            (requireActivity() as HMIDashboardActivity).addNewFragment(FirmwareVersionInfoFragment())
        }

        binding.tvLocalStartStop.setOnClickListener {
            requireActivity().showPasswordPromptDialog(
                getString(R.string.title_authorize_for_local_start_stop),
                isCancelable = true,
                {
                    (requireActivity() as HMIDashboardActivity).addNewFragment(
                        LocalStartStopFragment()
                    )
                },
                {
                    requireActivity().showCustomToast(
                        getString(R.string.msg_invalid_password),
                        false
                    )
                })
        }

        binding.tvTestMode.setOnClickListener {
            requireActivity().showPasswordPromptDialog(
                getString(R.string.title_authorize_for_test_mode),
                isCancelable = true,
                {
                    (requireActivity() as HMIDashboardActivity).addNewFragment(TestModeHomeFragment())
                },
                {
                    requireActivity().showCustomToast(
                        getString(R.string.msg_invalid_password),
                        false
                    )
                })
        }

        binding.tvFaultInformation.setOnClickListener {
            (requireActivity() as HMIDashboardActivity).addNewFragment(NewFaultInfoFragment())
        }

        binding.tvNotification.setOnClickListener {
            (requireActivity() as HMIDashboardActivity).addNewFragment(AppNotificationsFragment())
        }
    }

}