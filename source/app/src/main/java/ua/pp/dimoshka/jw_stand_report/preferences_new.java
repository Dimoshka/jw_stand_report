package ua.pp.dimoshka.jw_stand_report;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.google.analytics.tracking.android.EasyTracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by designers on 27.10.2014.
 */
public class preferences_new extends PreferenceActivity {

    private static SQLiteDatabase database = null;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        class_sqlite dbOpenHelper = new class_sqlite(this);
        database = dbOpenHelper.openDataBase();

        final ListPreference listPreference = (ListPreference) findPreference("location");
        setListPreferenceData(listPreference);

        listPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setListPreferenceData(listPreference);
                return false;
            }
        });
    }

    protected static void setListPreferenceData(ListPreference listPreference) {
        Cursor cursor = database.rawQuery("SELECT _id, name from location", null);
        List<String> entries_list = new ArrayList<String>();
        List<String> entryValues_list = new ArrayList<String>();
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                entries_list.add(cursor.getString(cursor.getColumnIndex("name")));
                entryValues_list.add(cursor.getString(cursor.getColumnIndex("_id")));
                cursor.moveToNext();
            }
        }

        final CharSequence[] entries = entries_list.toArray(new CharSequence[entries_list.size()]);
        final CharSequence[] entryValues = entryValues_list.toArray(new CharSequence[entryValues_list.size()]);

        listPreference.setEntries(entries);
        listPreference.setDefaultValue("1");
        listPreference.setEntryValues(entryValues);
    }

    @Override
    public void onStart() {
        super.onStart();
        //if (prefs.getBoolean("analytics", true)) {
        EasyTracker.getInstance(this).activityStart(this);
        //}
    }

    @Override
    public void onStop() {
        super.onStop();
        //if (prefs.getBoolean("analytics", true)) {
        EasyTracker.getInstance(this).activityStop(this);
        //}
    }
}