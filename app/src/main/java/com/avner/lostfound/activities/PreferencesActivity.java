package com.avner.lostfound.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.avner.lostfound.Constants;
import com.avner.lostfound.R;
import com.parse.ParseUser;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
@SuppressWarnings("deprecation")
public class PreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference button = findPreference(getString(R.string.logout_button));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setResult(Constants.RESULT_CODE_LOGOUT, null);
                finish();
                return true;
            }
        });
        final Preference userDisplayName = findPreference(getString(R.string.user_display_name));
        userDisplayName.setSummary((String) ParseUser.getCurrentUser().get("name"));
        userDisplayName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                userDisplayName.setSummary((String) newValue);
                ParseUser.getCurrentUser().put("name", newValue);
                ParseUser.getCurrentUser().saveInBackground();

                return true;
            }
        });

    }
}
