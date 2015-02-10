package ua.pp.dimoshka.jw_stand_report;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.androidquery.AQuery;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.splunk.mint.Mint;

public class preferences extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static SQLiteDatabase database = null;
    private AQuery aq;
    private SharedPreferences prefs;
    private Cursor cursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("analytics", true)) {
            Mint.initAndStartSession(preferences.this, "354b0769");
            Tracker t = ((AnalyticsSampleApp) this.getApplication()).getTracker(AnalyticsSampleApp.TrackerName.APP_TRACKER);
            t.setScreenName("prefference");
            t.send(new HitBuilders.AppViewBuilder().build());
        }

        setContentView(R.layout.settings);
        class_sqlite dbOpenHelper = new class_sqlite(this);
        database = dbOpenHelper.openDataBase();
        aq = new AQuery(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.settings);


        cursor = database.query("location",
                new String[]{"_id", "name"}, null, null, null, null, "_id");
        SimpleCursorAdapter loc_adapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_spinner_dropdown_item, cursor, new String[]{"name"}, new int[]{android.R.id.text1}, 0);
        aq.id(R.id.location).adapter(loc_adapter);


        prefs.registerOnSharedPreferenceChangeListener(this);

        aq.id(R.id.button_down1).clicked(this, "now_click");

        // set_default();
    }


    public void now_click(final View v) {

        int id = v.getId();

        switch (id) {
            case R.id.save:

                prefs.edit().putString("user", aq.id(R.id.user).getText().toString());
                prefs.edit().putString("meeting", aq.id(R.id.meeting).getText().toString());

                prefs.edit().putInt("location", aq.id(R.id.location).getSelectedItemPosition());
                prefs.edit().putInt("send_type", aq.id(R.id.send_type).getSelectedItemPosition());

                prefs.edit().putString("sms", aq.id(R.id.sms).getText().toString());
                prefs.edit().putString("email", aq.id(R.id.email).getText().toString());
                prefs.edit().putBoolean("analytics", aq.id(R.id.analytics).isChecked());

                finish();
                break;
        }

    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        aq.id(R.id.user).text(savedInstanceState.getString("user"));
        aq.id(R.id.meeting).text(savedInstanceState.getString("meeting"));
        aq.id(R.id.location).setSelection(savedInstanceState.getInt("location"));
        aq.id(R.id.send_type).setSelection(savedInstanceState.getInt("send_type"));
        aq.id(R.id.sms).text(savedInstanceState.getString("sms"));
        aq.id(R.id.email).text(savedInstanceState.getString("email"));
        aq.id(R.id.analytics).checked(savedInstanceState.getBoolean("analytics"));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("user", aq.id(R.id.user).getText().toString());
        outState.putString("meeting", aq.id(R.id.meeting).getText().toString());
        outState.putInt("location", aq.id(R.id.location).getSelectedItemPosition());
        outState.putInt("send_type", aq.id(R.id.send_type).getSelectedItemPosition());
        outState.putString("sms", aq.id(R.id.sms).getText().toString());
        outState.putString("email", aq.id(R.id.email).getText().toString());
        outState.putBoolean("analytics", aq.id(R.id.analytics).isChecked());
    }

    private void set_default() {

        aq.id(R.id.user).text(prefs.getString("user", ""));
        aq.id(R.id.meeting).text(prefs.getString("meeting", ""));
        aq.id(R.id.location).setSelection(prefs.getInt("location", 1));
        aq.id(R.id.send_type).setSelection(prefs.getInt("send_type", 3));
        aq.id(R.id.sms).text(prefs.getString("sms", ""));
        aq.id(R.id.email).text(prefs.getString("email", ""));
        aq.id(R.id.analytics).checked(prefs.getBoolean("analytics", true));

/*
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getInt(cursor.getColumnIndex("_id")) == Integer.parseInt(prefs.getString("location", "1"))) {
                aq.id(R.id.location).setSelection(i);
                break;
            }
            cursor.moveToNext();
        }
        */
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //set_default();
    }
}
