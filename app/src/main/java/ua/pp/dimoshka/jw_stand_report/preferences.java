package ua.pp.dimoshka.jw_stand_report;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.google.analytics.tracking.android.EasyTracker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class preferences extends PreferenceActivity {
    private SharedPreferences prefs = null;
    private static SQLiteDatabase database = null;

    protected Method mLoadHeaders = null;
    protected Method mHasHeaders = null;

    /**
     * Checks to see if using new v11+ way of handling PrefsFragments.
     *
     * @return Returns false pre-v11, else checks to see if using headers.
     */
    public boolean isNewV11Prefs() {
        if (mHasHeaders != null && mLoadHeaders != null) {
            try {
                return (Boolean) mHasHeaders.invoke(this);
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return false;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        try {
            mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class);
            mHasHeaders = getClass().getMethod("hasHeaders");
        } catch (NoSuchMethodException e) {
        }
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        class_sqlite dbOpenHelper = new class_sqlite(this);
        //    database = dbOpenHelper.openDataBase();

        if (!isNewV11Prefs()) {
            addPreferencesFromResource(R.xml.preferences);
            //getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
        }
    }


    @Override
    public void onBuildHeaders(List<Header> aTarget) {
        try {
            mLoadHeaders.invoke(this, new Object[]{R.xml.preferences, aTarget});
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            addPreferencesFromResource(R.xml.preferences);

            /*

            final ListPreference listPreference = (ListPreference) findPreference("meeting");
            setListPreferenceData(listPreference);

            listPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    setListPreferenceData(listPreference);
                    return false;
                }
            });*/
        }
    }

    protected static void setListPreferenceData(ListPreference listPreference) {
        Cursor cursor = database.rawQuery("SELECT * from meeting", null);

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
        if (prefs.getBoolean("analytics", true)) {
            EasyTracker.getInstance(this).activityStart(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (prefs.getBoolean("analytics", true)) {
            EasyTracker.getInstance(this).activityStop(this);
        }
    }

}
