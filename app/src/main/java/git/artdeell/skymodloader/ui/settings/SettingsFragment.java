package git.artdeell.skymodloader.ui.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import git.artdeell.skymodloader.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName("package_configs");
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}