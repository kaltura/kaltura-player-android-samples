package com.kaltura.playkit.samples.fulldemo

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle

import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat


class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {

        // Add visualizer preferences, defined in the XML file in res->xml->pref_visualizer
        addPreferencesFromResource(R.xml.pref_visualizer)

        val sharedPreferences = preferenceScreen.sharedPreferences
        val prefScreen = preferenceScreen
        val count = prefScreen.preferenceCount

        // Go through all of the preferences, and set up their preference summary.
        for (i in 0 until count) {
            val p = prefScreen.getPreference(i)
            // You don't need to set up preference summaries for checkbox preferences because
            // they are already set up in xml using summaryOff and summary On
            if (p !is CheckBoxPreference) {
                val value = sharedPreferences?.getString(p.key, "")
                value?.let { setPreferenceSummary(p, it) }
            }
        }

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // Figure out which preference was changed
        val preference: Preference?  = findPreference(key)
        preference?.let { pref ->
            // Updates the summary for the preference
            if (preference !is CheckBoxPreference) {
                val value = sharedPreferences.getString(pref.key, "")
                value?.let { setPreferenceSummary(pref, it) }
            }
        }
    }

    /**
     * Updates the summary for the preference
     *
     * @param preference The preference to be updated
     * @param value      The value that the preference was updated to
     */
    private fun setPreferenceSummary(preference: Preference, value: String) {
        // COMPLETED (3) Don't forget to add code here to properly set the summary for an EditTextPreference
        if (preference is ListPreference) {
            // For list preferences, figure out the label of the selected value
            val prefIndex = preference.findIndexOfValue(value)
            if (prefIndex >= 0) {
                // Set the summary to that label
                preference.summary = preference.entries[prefIndex]
            }
        } else if (preference is EditTextPreference) {
            // For EditTextPreferences, set the summary to the value's simple string representation.
            preference.setSummary(value)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceScreen.sharedPreferences
                ?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences
                ?.unregisterOnSharedPreferenceChangeListener(this)
    }
}