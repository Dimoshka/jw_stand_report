package ua.pp.dimoshka.jw_stand_report;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.analytics.tracking.android.EasyTracker;
import com.splunk.mint.Mint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class main extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private AQuery aq;
    private SharedPreferences prefs;
    private static SQLiteDatabase database = null;
    private Cursor cursor;
    private class_transliterator translite = new class_transliterator();
    private class_send_sms sendSms = new class_send_sms();

    private final static String SENT = "SENT_SMS_ACTION", DELIVERED = "DELIVERED_SMS_ACTION", ISNULL = "Entered, not all data";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.initAndStartSession(main.this, "354b0769");
        setContentView(R.layout.main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        class_sqlite dbOpenHelper = new class_sqlite(this);
        database = dbOpenHelper.openDataBase();

        aq = new AQuery(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        aq.id(R.id.send).clicked(this, "send_click");
        aq.id(R.id.date).clicked(this, "date_click");

        aq.id(R.id.time_start).clicked(this, "time_click");
        aq.id(R.id.time_end).clicked(this, "time_click");

        aq.id(R.id.button_down1).clicked(this, "picker_click");
        aq.id(R.id.button_down2).clicked(this, "picker_click");
        aq.id(R.id.button_down3).clicked(this, "picker_click");
        aq.id(R.id.button_down4).clicked(this, "picker_click");
        aq.id(R.id.button_down5).clicked(this, "picker_click");
        aq.id(R.id.button_down6).clicked(this, "picker_click");
        aq.id(R.id.button_down7).clicked(this, "picker_click");

        aq.id(R.id.button_up1).clicked(this, "picker_click");
        aq.id(R.id.button_up2).clicked(this, "picker_click");
        aq.id(R.id.button_up3).clicked(this, "picker_click");
        aq.id(R.id.button_up4).clicked(this, "picker_click");
        aq.id(R.id.button_up5).clicked(this, "picker_click");
        aq.id(R.id.button_up6).clicked(this, "picker_click");
        aq.id(R.id.button_up7).clicked(this, "picker_click");

        cursor = database.query("location",
                new String[]{"_id", "name"}, null, null, null, null, "_id");
        SimpleCursorAdapter loc_adapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_spinner_dropdown_item, cursor, new String[]{"name"}, new int[]{android.R.id.text1}, 0);
        aq.id(R.id.location).adapter(loc_adapter);

        set_default();

        if (prefs.getBoolean("first_run", true)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.first_run_title))
                    .setMessage(getString(R.string.first_run_text))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //startActivity(new Intent(main.this, preferences.class));
                            start_settings();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
            prefs.edit().putBoolean("first_run", false).apply();
        }

        prefs.registerOnSharedPreferenceChangeListener(this);

        registerReceiver(sendSms, new IntentFilter(SENT));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        aq.id(R.id.user).text(savedInstanceState.getString("user"));
        aq.id(R.id.date).text(savedInstanceState.getString("date"));
        aq.id(R.id.time_start).text(savedInstanceState.getString("time_start"));
        aq.id(R.id.time_end).text(savedInstanceState.getString("time_end"));
        aq.id(R.id.location).setSelection(savedInstanceState.getInt("location"));
        aq.id(R.id.journals).text(String.valueOf(savedInstanceState.getInt("journals")));
        aq.id(R.id.broshure).text(String.valueOf(savedInstanceState.getInt("broshure")));
        aq.id(R.id.books).text(String.valueOf(savedInstanceState.getInt("books")));
        aq.id(R.id.dvd).text(String.valueOf(savedInstanceState.getInt("dvd")));
        aq.id(R.id.talks).text(String.valueOf(savedInstanceState.getInt("talks")));
        aq.id(R.id.repeated_visits).text(String.valueOf(savedInstanceState.getInt("repeated_visits")));
        aq.id(R.id.s43).text(String.valueOf(savedInstanceState.getInt("s43")));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("user", aq.id(R.id.user).getText().toString());
        outState.putString("date", aq.id(R.id.date).getText().toString());
        outState.putString("time_start", aq.id(R.id.time_start).getText().toString());
        outState.putString("time_end", aq.id(R.id.time_end).getText().toString());
        outState.putInt("location", aq.id(R.id.location).getSelectedItemPosition());
        outState.putInt("journals", Integer.parseInt(aq.id(R.id.journals).getText().toString()));
        outState.putInt("broshure", Integer.parseInt(aq.id(R.id.broshure).getText().toString()));
        outState.putInt("books", Integer.parseInt(aq.id(R.id.books).getText().toString()));
        outState.putInt("dvd", Integer.parseInt(aq.id(R.id.dvd).getText().toString()));
        outState.putInt("talks", Integer.parseInt(aq.id(R.id.talks).getText().toString()));
        outState.putInt("repeated_visits", Integer.parseInt(aq.id(R.id.repeated_visits).getText().toString()));
        outState.putInt("s43", Integer.parseInt(aq.id(R.id.s43).getText().toString()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                System.exit(0);
                break;
            case R.id.settings:
                //(new Intent(this, preferences.class));
                start_settings();
                break;
            case R.id.statistic:

                break;
            default:
                break;
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(sendSms);
    }

    private void start_settings() {
        if (Build.VERSION.SDK_INT < 11) {
            startActivity(new Intent(this, preferences_old.class));
        } else {
            startActivity(new Intent(this, preferences_new.class));
        }
    }

    private void set_default() {
        aq.id(R.id.user).text(prefs.getString("user", ""));
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getInt(cursor.getColumnIndex("_id")) == Integer.parseInt(prefs.getString("location", "1"))) {
                aq.id(R.id.location).setSelection(i);
                break;
            }
            cursor.moveToNext();
        }
    }

    private String get_shot_text(String text, int lenth) {
        String[] word = text.split(" ");
        text = "";
        for (int i = 0; i < word.length; i++) {
            if (text.length() > 0) text += " ";
            if (word[i].length() > lenth) text += word[i].substring(0, lenth);
            else text += word[i];
        }
        return text;
    }

    public void send_click(View v) {

        cursor.moveToPosition(aq.id(R.id.location).getSelectedItemPosition());
        //int id_stand = cursor.getString(cursor.getColumnIndex("name"));

        String user = aq.id(R.id.user).getText().toString();
        String date = aq.id(R.id.date).getText().toString();
        String time_start = aq.id(R.id.time_start).getText().toString();
        String time_end = aq.id(R.id.time_end).getText().toString();

        if (user.length() > 4 && !date.equals("0000-00-00") && !time_start.equals("00:00") && !time_end.equals("00:00")) {
            int send_type = Integer.parseInt(prefs.getString("send_type", "3"));
            if (send_type == 3) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.send_type))
                        .setPositiveButton(R.string.type_sms, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                send_sms(get_message(1));
                                return;
                            }
                        })
                        .setNegativeButton(R.string.type_email, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                send_mail(get_message(2));
                                return;
                            }
                        })
                        .show();
            }

            switch (send_type) {
                case 1:
                    send_sms(get_message(send_type));
                    break;
                case 2:
                    send_mail(get_message(send_type));
                    break;
            }
        } else {
            Toast.makeText(this, getString(R.string.vrong), Toast.LENGTH_LONG).show();
        }
    }

    private String get_message(int send_type) {
        cursor.moveToPosition(aq.id(R.id.location).getSelectedItemPosition());
        String message = "";

        switch (send_type) {
            case 1:
                message = "JW_STAND " + get_shot_text(translite.transliterate(cursor.getString(cursor.getColumnIndex("name"))), 10)
                        + "; " + get_shot_text(translite.transliterate(aq.id(R.id.user).getText().toString()), 10)
                        + "; " + aq.id(R.id.date).getText().toString()//.replace("-", "")
                        + "; " + aq.id(R.id.time_start).getText().toString()//.replace(":", "")
                        + "-" + aq.id(R.id.time_end).getText().toString()//.replace(":", "")
                        + "; j:" + aq.id(R.id.journals).getText().toString()
                        + "; br:" + aq.id(R.id.broshure).getText().toString()
                        + "; b:" + aq.id(R.id.books).getText().toString()
                        + "; d:" + aq.id(R.id.dvd).getText().toString()
                        + "; t:" + aq.id(R.id.talks).getText().toString()
                        + "; r:" + aq.id(R.id.repeated_visits).getText().toString()
                        + "; s:" + aq.id(R.id.s43).getText().toString();
                break;
            case 2:
                message = "JW_STAND " + cursor.getString(cursor.getColumnIndex("name"))
                        + "\n\r\n" + aq.id(R.id.user).getText().toString()
                        + "\n\r\n" + aq.id(R.id.date).getText().toString()
                        + " " + aq.id(R.id.time_start).getText().toString()
                        + " - " + aq.id(R.id.time_end).getText().toString()
                        + "\r\njournal: " + aq.id(R.id.journals).getText().toString()
                        + "\r\nbroshure: " + aq.id(R.id.broshure).getText().toString()
                        + "\r\nbook: " + aq.id(R.id.books).getText().toString()
                        + "\r\ndvd: " + aq.id(R.id.dvd).getText().toString()
                        + "\r\ntalk: " + aq.id(R.id.talks).getText().toString()
                        + "\r\nrepeated visits: " + aq.id(R.id.repeated_visits).getText().toString()
                        + "\r\ns43: " + aq.id(R.id.s43).getText().toString();
                break;
        }
        Log.e("11", message.length() + "");
        Log.e("11", message);
        return message;
    }

    private void write_statistic() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.statistic))
                .setMessage(getString(R.string.statistic_write))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        cursor.moveToPosition(aq.id(R.id.location).getSelectedItemPosition());
                        int id_stand = cursor.getInt(cursor.getColumnIndex("_id"));
                        String user = aq.id(R.id.user).getText().toString();
                        String date = aq.id(R.id.date).getText().toString();
                        String time_start = aq.id(R.id.time_start).getText().toString();
                        String time_end = aq.id(R.id.time_end).getText().toString();

                        int journal = Integer.parseInt(aq.id(R.id.journals).getText().toString());
                        int broshure = Integer.parseInt(aq.id(R.id.broshure).getText().toString());
                        int book = Integer.parseInt(aq.id(R.id.books).getText().toString());
                        int dvd = Integer.parseInt(aq.id(R.id.dvd).getText().toString());
                        int talk = Integer.parseInt(aq.id(R.id.talks).getText().toString());
                        int repeated_visit = Integer.parseInt(aq.id(R.id.repeated_visits).getText().toString());
                        int s43 = Integer.parseInt(aq.id(R.id.s43).getText().toString());

                        ContentValues init = new ContentValues();
                        init.put("id_stand", id_stand);
                        init.put("user", user);
                        init.put("date", date);
                        init.put("time_start", time_start);
                        init.put("time_end", time_end);
                        init.put("journal", journal);
                        init.put("broshure", broshure);
                        init.put("book", book);
                        init.put("dvd", dvd);
                        init.put("repeated_visit", repeated_visit);
                        init.put("talk", talk);
                        init.put("s_blank", s43);
                        database.insert("statistic", null, init);
                        clear();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        clear();
                    }
                })
                .show();

    }

    private void send_sms(String message) {
        final String sms = prefs.getString("sms", "").replace("(", "").replace(")", "").replace(" ", "").replace("-", "");
        if (sms.length() > 8) {

            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
            final SmsManager smsManager = SmsManager.getDefault();
            final ArrayList<String> mArray = smsManager.divideMessage(message);
            final ArrayList<PendingIntent> sentArrayIntents = new ArrayList<PendingIntent>();
            for (int i = 0; i < mArray.size(); i++) {
                sentArrayIntents.add(sentPI);
                Log.e("22", mArray.get(i));
            }
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.sms_sending))
                    .setMessage(getString(R.string.sms_count) + " " + mArray.size())
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            smsManager.sendMultipartTextMessage(sms, null, mArray, sentArrayIntents, null);
                            write_statistic();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        } else Toast.makeText(this, getString(R.string.sms_vrong), Toast.LENGTH_LONG).show();
    }

    private void send_mail(String message) {
        final String email = prefs.getString("email", "");
        if (email.length() > 6 && email_validate(email)) {
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "JW_STAND " + aq.id(R.id.user).getText().toString());
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(emailIntent, getString(R.string.email_sending)));
            write_statistic();
        } else Toast.makeText(this, getString(R.string.email_vrong), Toast.LENGTH_LONG).show();
    }


    public boolean email_validate(String email) {
        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    private void clear() {
        aq.id(R.id.date).text("0000-00-00");
        aq.id(R.id.time_start).text("00:00");
        aq.id(R.id.time_end).text("00:00");
        aq.id(R.id.journals).text("0");
        aq.id(R.id.broshure).text("0");
        aq.id(R.id.books).text("0");
        aq.id(R.id.dvd).text("0");
        aq.id(R.id.talks).text("0");
        aq.id(R.id.repeated_visits).text("0");
        aq.id(R.id.s43).text("0");
    }

    public void time_click(final View v) {
        Calendar c = Calendar.getInstance();

        final int id = v.getId();
        int hours = c.get(Calendar.HOUR_OF_DAY);
        int minutes = 0;
        if (c.get(Calendar.MINUTE) < 30) minutes = 0;
        else minutes = 30;


        if (id == R.id.time_start) {
            if (minutes == 30) {
                hours -= 1;
                minutes = 0;
            } else {
                hours -= 2;
                minutes = 30;
            }
        }

        TimePickerDialog tpd = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        String h = String.valueOf(hourOfDay);
                        if (hourOfDay < 10) h = "0" + h;
                        String m = String.valueOf(minute);
                        if (minute < 10) m = "0" + m;
                        aq.id(id).text(h + ":" + m);
                    }
                }, hours, minutes, true);
        tpd.show();
    }

    public void date_click(View v) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        String m = String.valueOf(monthOfYear);
                        if (monthOfYear < 10) m = "0" + m;
                        String d = String.valueOf(dayOfMonth);
                        if (dayOfMonth < 10) d = "0" + d;
                        aq.id(R.id.date).text(year + "-" + m + "-" + d);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    public void picker_click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.button_up1:
                set_picket_textbox(R.id.journals, 1);
                break;
            case R.id.button_up2:
                set_picket_textbox(R.id.broshure, 1);
                break;
            case R.id.button_up3:
                set_picket_textbox(R.id.books, 1);
                break;
            case R.id.button_up4:
                set_picket_textbox(R.id.dvd, 1);
                break;
            case R.id.button_up5:
                set_picket_textbox(R.id.talks, 1);
                break;
            case R.id.button_up6:
                set_picket_textbox(R.id.repeated_visits, 1);
                break;
            case R.id.button_up7:
                set_picket_textbox(R.id.s43, 1);
                break;
            case R.id.button_down1:
                set_picket_textbox(R.id.journals, -1);
                break;
            case R.id.button_down2:
                set_picket_textbox(R.id.broshure, -1);
                break;
            case R.id.button_down3:
                set_picket_textbox(R.id.books, -1);
                break;
            case R.id.button_down4:
                set_picket_textbox(R.id.dvd, -1);
                break;
            case R.id.button_down5:
                set_picket_textbox(R.id.talks, -1);
                break;
            case R.id.button_down6:
                set_picket_textbox(R.id.repeated_visits, -1);
                break;
            case R.id.button_down7:
                set_picket_textbox(R.id.s43, -1);
                break;
        }

    }

    private void set_picket_textbox(int tv, int plus) {
        int value = Integer.parseInt(aq.id(tv).getText().toString()) + plus;
        if (value < 0) value = 0;
        aq.id(tv).text(String.valueOf(value));
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        set_default();
    }
}
