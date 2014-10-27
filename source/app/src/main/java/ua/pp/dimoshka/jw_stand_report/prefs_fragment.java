package ua.pp.dimoshka.jw_stand_report;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by designers on 27.10.2014.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class prefs_fragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}