package ua.pp.dimoshka.jw_stand_report;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.splunk.mint.Mint;

import java.util.HashMap;

import ua.pp.dimoshka.jw_stand_report.managers.ISettingsManager;
import ua.pp.dimoshka.jw_stand_report.managers.SettingsManager;

public class SettingsActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SQLiteDatabase database = null;
    private class_sqlite dbOpenHelper = null;
    private AQuery aq;
    private SharedPreferences prefs;
    private Cursor cursor;

    private HashMap<Integer, String> savedPhoneNumbers;
    private HashMap<Integer, String> savedEmailAddresses;

    private ISettingsManager settingsManager;

    private Spinner location_spinner;

    private TextView phoneNumberCaption_textView;
    private EditText phoneNumber_editText;

    private TextView emailAddressCaption_textView;
    private EditText emailAddress_editText;
    private TextWatcher textWatcher_phoneNumber = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            Cursor cursor = (Cursor) location_spinner.getSelectedItem();

            int locationId = cursor.getInt(cursor.getColumnIndex("_id"));

            String phoneNumber = phoneNumber_editText.getText().toString();

            savedPhoneNumbers.put(locationId, phoneNumber);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private TextWatcher textWatcher_emailAddress = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            Cursor cursor = (Cursor) location_spinner.getSelectedItem();

            int locationId = cursor.getInt(cursor.getColumnIndex("_id"));

            String emailAddress = emailAddress_editText.getText().toString();

            savedEmailAddresses.put(locationId, emailAddress);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            if (adapterView.getId() == R.id.location) {
                Adapter adapter = adapterView.getAdapter();
                Cursor cursor = (Cursor) adapter.getItem(position);

                int locationId = cursor.getInt(cursor.getColumnIndex("_id"));
                String locationName = cursor.getString(cursor.getColumnIndex("name"));

                String phoneNumberCaption = getString(R.string.PhoneNumber_Format, locationName);
                phoneNumberCaption_textView.setText(phoneNumberCaption);

                String emailAddressCaption = getString(R.string.EmailAddress_Format, locationName);
                emailAddressCaption_textView.setText(emailAddressCaption);

                String phoneNumber = savedPhoneNumbers.get(locationId);
                phoneNumber_editText.removeTextChangedListener(textWatcher_phoneNumber);
                phoneNumber_editText.setText(phoneNumber);
                phoneNumber_editText.addTextChangedListener(textWatcher_phoneNumber);

                String emailAddress = savedEmailAddresses.get(locationId);
                emailAddress_editText.removeTextChangedListener(textWatcher_emailAddress);
                emailAddress_editText.setText(emailAddress);
                emailAddress_editText.addTextChangedListener(textWatcher_emailAddress);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsManager = new SettingsManager(this);

        if (savedInstanceState == null) {
            savedPhoneNumbers = settingsManager.getPhoneNumbers();
            savedEmailAddresses = settingsManager.getEmailAddresses();
        } else {
            savedPhoneNumbers = (HashMap<Integer, String>) savedInstanceState.getSerializable("savedPhoneNumbers");
            savedEmailAddresses = (HashMap<Integer, String>) savedInstanceState.getSerializable("savedEmailAddresses");
        }


        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("analytics", true)) {
            Mint.initAndStartSession(SettingsActivity.this, "354b0769");
            Tracker t = ((AnalyticsSampleApp) this.getApplication()).getTracker(AnalyticsSampleApp.TrackerName.APP_TRACKER);
            t.setScreenName("prefference");
            t.send(new HitBuilders.AppViewBuilder().build());
        }

        setContentView(R.layout.activity_settings);
        dbOpenHelper = new class_sqlite(this);
        database = dbOpenHelper.openDataBase();
        aq = new AQuery(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.settings);

        location_spinner = (Spinner) findViewById(R.id.location);
        location_spinner.setOnItemSelectedListener(itemSelectedListener);

        phoneNumberCaption_textView = (TextView) findViewById(R.id.phoneNumberCaption_textView);

        phoneNumber_editText = (EditText) findViewById(R.id.sms);
        phoneNumber_editText.addTextChangedListener(textWatcher_phoneNumber);

        emailAddressCaption_textView = (TextView) findViewById(R.id.emailAddressCaption_textView);

        emailAddress_editText = (EditText) findViewById(R.id.email);
        emailAddress_editText.addTextChangedListener(textWatcher_emailAddress);

        cursor = database.query("location",
                new String[]{"_id", "name"}, null, null, null, null, "_id");
        SimpleCursorAdapter loc_adapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_spinner_dropdown_item, cursor, new String[]{"name"}, new int[]{android.R.id.text1}, 0);
        aq.id(R.id.location).adapter(loc_adapter);


        prefs.registerOnSharedPreferenceChangeListener(this);

        aq.id(R.id.save).clicked(this, "now_click");

        set_default();
    }

    public void now_click(final View v) {

        int id = v.getId();

        switch (id) {
            case R.id.save:
                SharedPreferences.Editor ed = prefs.edit();
                ed.putString("user", aq.id(R.id.user).getText().toString());
                ed.putString("meeting", aq.id(R.id.meeting).getText().toString());

                cursor.moveToPosition(aq.id(R.id.location).getSelectedItemPosition());
                ed.putInt("location", cursor.getInt(cursor.getColumnIndex("_id")));
                ed.putInt("send_type", aq.id(R.id.send_type).getSelectedItemPosition());

                ed.putString("sms", aq.id(R.id.sms).getText().toString());
                ed.putString("email", aq.id(R.id.email).getText().toString());
                ed.putBoolean("analytics", aq.id(R.id.analytics).isChecked());
                ed.apply();

                settingsManager.setPhoneNumbers(savedPhoneNumbers);
                settingsManager.setEmailAddresses(savedEmailAddresses);

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

        outState.putSerializable("savedPhoneNumbers", savedPhoneNumbers);
        outState.putSerializable("savedEmailAddresses", savedEmailAddresses);

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
        //aq.id(R.id.location).setSelection(prefs.getInt("location", 0));
        aq.id(R.id.send_type).setSelection(prefs.getInt("send_type", 2));
        //aq.id(R.id.sms).text(prefs.getString("sms", ""));
        //aq.id(R.id.email).text(prefs.getString("email", ""));
        aq.id(R.id.analytics).checked(prefs.getBoolean("analytics", true));

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getInt(cursor.getColumnIndex("_id")) == prefs.getInt("location", 1)) {
                aq.id(R.id.location).setSelection(i);
                break;
            }
            cursor.moveToNext();
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //set_default();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null) cursor.close();
        database.close();
        dbOpenHelper.close();
    }
}
