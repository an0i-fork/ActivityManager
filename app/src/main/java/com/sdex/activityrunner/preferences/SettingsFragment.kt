package com.sdex.activityrunner.preferences

import android.os.Bundle
import android.widget.Toast
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.sdex.activityrunner.R
import com.sdex.activityrunner.app.root.GetRootDialog
import com.sdex.activityrunner.premium.GetPremiumDialog
import com.sdex.activityrunner.util.CheckRootTask
import com.sdex.activityrunner.util.ThemeHelper

class SettingsFragment : PreferenceFragmentCompat() {

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.pref_advanced, rootKey)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val appPreferences = AppPreferences(activity!!)
    val rootIntegration = findPreference(AppPreferences.KEY_ROOT_INTEGRATION)
      as SwitchPreferenceCompat
    rootIntegration.setOnPreferenceChangeListener { _, newValue ->
      if (newValue is Boolean) {
        if (!newValue) {
          return@setOnPreferenceChangeListener true
        }
      }
      if (appPreferences.isProVersion) {
        checkRoot(rootIntegration)
        return@setOnPreferenceChangeListener true
      } else {
        val dialog = GetRootDialog.newInstance()
        dialog.show(childFragmentManager, GetPremiumDialog.TAG)
        return@setOnPreferenceChangeListener false
      }
    }

    val themePreference = findPreference(AppPreferences.KEY_THEME) as ListPreference
    themePreference.setOnPreferenceChangeListener { _, _ ->
      activity?.recreate()
      return@setOnPreferenceChangeListener true
    }

    val blackThemePreference = findPreference(AppPreferences.KEY_THEME_BLACK) as CheckBoxPreference
    val isDarkTheme = themePreference.value.toInt() == ThemeHelper.DARK_THEME
    blackThemePreference.isEnabled = isDarkTheme

    blackThemePreference.setOnPreferenceChangeListener { _, _ ->
      activity?.recreate()
      return@setOnPreferenceChangeListener true
    }
  }

  private fun checkRoot(rootIntegration: SwitchPreferenceCompat) {
    val checkRootTask = CheckRootTask(object : CheckRootTask.Callback {
      override fun onStatusChanged(status: Int) {
        if (activity != null && !activity!!.isFinishing && isAdded) {
          if (status != CheckRootTask.RESULT_OK) {
            rootIntegration.isChecked = false
            Toast.makeText(activity, R.string.settings_error_root_not_granted,
              Toast.LENGTH_SHORT).show()
          }
        }
      }
    })
    checkRootTask.execute()
  }
}