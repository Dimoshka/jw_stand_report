package ua.pp.dimoshka.jw_stand_report;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.splunk.mint.Mint;

import java.util.ArrayList;
import java.util.List;

public class preferences extends PreferenceActivity {
    private static SQLiteDatabase database = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Mint.initAndStartSession(preferences.this, "354b0769");
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.preference);

        class_sqlite dbOpenHelper = new class_sqlite(this);
        database = dbOpenHelper.openDataBase();
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            addPreferencesFromResource(R.xml.preferences);

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
}
