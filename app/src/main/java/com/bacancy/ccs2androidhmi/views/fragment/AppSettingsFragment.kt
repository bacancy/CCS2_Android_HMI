package com.bacancy.ccs2androidhmi.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bacancy.ccs2androidhmi.R
import com.bacancy.ccs2androidhmi.base.BaseFragment
import com.bacancy.ccs2androidhmi.databinding.FragmentAppSettingsBinding
import com.bacancy.ccs2androidhmi.network.models.LoginRequest
import com.bacancy.ccs2androidhmi.util.AppConfig
import com.bacancy.ccs2androidhmi.util.CommonUtils
import com.bacancy.ccs2androidhmi.util.CommonUtils.CDM_CONFIG_OPTION_ENTERED
import com.bacancy.ccs2androidhmi.util.CommonUtils.LOADING
import com.bacancy.ccs2androidhmi.util.CommonUtils.SUCCESS
import com.bacancy.ccs2androidhmi.util.DialogUtils.showLoginDialog
import com.bacancy.ccs2androidhmi.util.DialogUtils.showSelectAppLanguageDialog
import com.bacancy.ccs2androidhmi.util.LanguageConfig.getLanguageName
import com.bacancy.ccs2androidhmi.util.LanguageConfig.setAppLanguage
import com.bacancy.ccs2androidhmi.util.LanguageConfig.setupLanguagesList
import com.bacancy.ccs2androidhmi.util.PrefHelper
import com.bacancy.ccs2androidhmi.viewmodel.AppViewModel
import com.bacancy.ccs2androidhmi.views.HMIDashboardActivity
import com.bacancy.ccs2androidhmi.views.adapters.AppSettingsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AppSettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentAppSettingsBinding
    val appViewModel: AppViewModel by viewModels()

    @Inject
    lateinit var prefHelper: PrefHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppSettingsBinding.inflate(layoutInflater)
        (requireActivity() as HMIDashboardActivity).showHideBackIcon()
        (requireActivity() as HMIDashboardActivity).showHideHomeIcon()
        (requireActivity() as HMIDashboardActivity).showHideSettingOptions()
        setupSettingsList()
        return binding.root
    }

    override fun setScreenHeaderViews() {
        binding.incHeader.tvHeader.text = getString(R.string.lbl_settings)
    }

    override fun setupViews() {}

    override fun handleClicks() {}

    private fun setupSettingsList() {

        requireActivity().setupLanguagesList()

        binding.rvSettings.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = AppSettingsAdapter(getSettingsList()) { selectedItem ->
                when (selectedItem.first) {
                    SET_01 -> {
                        requireActivity().showSelectAppLanguageDialog(prefHelper) {
                            requireActivity().setAppLanguage(it.code, prefHelper)
                            requireActivity().recreate()
                        }
                    }

                    SET_02 -> {
                        (requireActivity() as HMIDashboardActivity).toggleTheme()
                    }

                    SET_03 -> {
                        openNewSettings(NewFaultInfoFragment())
                    }

                    SET_04 -> {
                        openNewSettings(FirmwareVersionInfoFragment())
                    }

                    SET_05 -> {
                        validateUserAndOpenFragment {
                            openNewSettings(LocalStartStopFragment())
                        }
                    }

                    SET_06 -> {
                        validateUserAndOpenFragment {
                            openNewSettings(TestModeHomeFragment())
                        }
                    }

                    SET_07 -> {
                        openNewSettings(AppNotificationsFragment())
                    }

                    SET_08 -> {
                        (requireActivity() as HMIDashboardActivity).resetPorts()
                        requireActivity().stopLockTask()
                        prefHelper.setBoolean(CommonUtils.IS_APP_PINNED, false)
                        (requireActivity() as HMIDashboardActivity).openEVSEApp()
                    }

                    SET_09 -> {
                        validateUserAndOpenFragment {
                            prefHelper.setBoolean(CDM_CONFIG_OPTION_ENTERED, true)
                        }
                    }
                }
            }
        }
    }

    private fun openNewSettings(fragment: Fragment) {
        (requireActivity() as HMIDashboardActivity).addNewFragment(
            fragment
        )
    }

    private fun validateUserAndOpenFragment(onSuccess: () -> Unit) {
        requireActivity().showLoginDialog { dialog, loginBinding, username, password ->
            lifecycleScope.launch(Dispatchers.IO) {
                appViewModel.login(LoginRequest(username, password))
                withContext(Dispatchers.Main) {
                    appViewModel.loginResponse.collect { response ->
                        when (response) {
                            LOADING -> {
                                loginBinding.tvPopupTitle.text = getString(R.string.lbl_loading)
                                loginBinding.btnSubmit.isEnabled = false
                            }

                            SUCCESS -> {
                                loginBinding.tvPopupTitle.text = getString(R.string.lbl_login)
                                loginBinding.btnSubmit.isEnabled = true
                                dialog.dismiss()
                                onSuccess()
                            }

                            else -> {
                                loginBinding.tvPopupTitle.text =
                                    getString(R.string.msg_login_failed)
                                loginBinding.btnSubmit.isEnabled = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getSettingsList(): List<Pair<String, String>> {

        val appLanguageTitle = getString(
            R.string.lbl_change_app_language,
            requireActivity().getLanguageName(prefHelper)
        )

        val switchModeTitle = if (prefHelper.getBoolean(
                PrefHelper.IS_DARK_THEME,
                false
            )
        ) {
            getString(R.string.lbl_switch_to_light_mode)
        } else {
            getString(R.string.lbl_switch_to_dark_mode)
        }

        val settingsList = mutableListOf(
            SET_01 to appLanguageTitle,
            SET_02 to switchModeTitle,
            SET_03 to getString(R.string.lbl_fault_information),
            SET_04 to getString(R.string.lbl_firmware_version_information),
            SET_05 to getString(R.string.lbl_local_start_or_stop_charging),
            SET_06 to getString(R.string.lbl_test_mode),
            SET_07 to getString(R.string.lbl_notifications),
            SET_08 to getString(R.string.charger_commissioning),
            //SET_09 to getString(R.string.cdm_configuration)
        )

        if (!AppConfig.SHOW_LOCAL_START_STOP) {
            settingsList.remove(SET_05 to getString(R.string.lbl_local_start_or_stop_charging))
        }

        if (!AppConfig.SHOW_TEST_MODE) {
            settingsList.remove(SET_06 to getString(R.string.lbl_test_mode))
        }

        return settingsList
    }

    companion object {
        const val SET_01 = "SET_01"
        const val SET_02 = "SET_02"
        const val SET_03 = "SET_03"
        const val SET_04 = "SET_04"
        const val SET_05 = "SET_05"
        const val SET_06 = "SET_06"
        const val SET_07 = "SET_07"
        const val SET_08 = "SET_08"
        const val SET_09 = "SET_09"
        const val ENGLISH = "English"
    }
}