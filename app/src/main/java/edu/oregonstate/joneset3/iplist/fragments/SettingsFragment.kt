package edu.oregonstate.joneset3.iplist.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import edu.oregonstate.joneset3.iplist.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}