package com.dfrobot.angelo.blunobasicdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class SettingsFragment extends PreferenceFragment {
    private final static String TAG = SettingsFragment.class.getSimpleName();
    private SettingsFragment me = this;
    private StringBuffer parameterString;
    private SharedPreferences sharedPref;

    interface Sender {
        public void serialSend(String msg);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_preferences);

        ArrayList<Preference> bars = new ArrayList<>();
        bars.add(findPreference("L_Min"));
        bars.add(findPreference("L_Max"));
        bars.add(findPreference("A_Min"));
        bars.add(findPreference("A_Max"));
        bars.add(findPreference("B_Min"));
        bars.add(findPreference("B_Max"));

        sharedPref = ((SettingsFragment) me).getPreferenceScreen().getSharedPreferences();
        parameterString = new StringBuffer(String.format(Locale.UK, "{CR%03d%03d%03d%03d%03d%03d}",
                sharedPref.getInt("L_Min", 0), sharedPref.getInt("L_Max", 100),
                2 * sharedPref.getInt("A_Min", 0), 2 * sharedPref.getInt("A_Max", 100),
                2 * sharedPref.getInt("B_Min", 0), 2 * sharedPref.getInt("B_Max", 100)));

        Set<String> ss = new TreeSet<String>();
        bars.forEach(b -> b.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                SharedPreferences sharedPref = ((SettingsFragment) me).getPreferenceScreen().getSharedPreferences();
                int index = bars.indexOf(preference);
                if (index % 2 == 1) {
                    if (sharedPref.getInt(bars.get(index - 1).getKey(), 0) > (int) o - 5) {
                        return false;
                    }
                } else {
                    if (sharedPref.getInt(bars.get(index + 1).getKey(), 0) < (int) o + 5) {
                        return false;
                    }
                }
                try {
                    parameterString.replace(3 * (index + 1), 3 * (index + 2), String.format("%03d", (index < 2 ? 1 : 2) * (int) o));
                    ((Sender) getActivity()).serialSend(String.valueOf(parameterString));
                } catch (ClassCastException cce) {
                }

                return true;
            }
        }));
    }

}