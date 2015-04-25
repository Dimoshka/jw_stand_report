package ua.pp.dimoshka.jw_stand_report;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.splunk.mint.Mint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class main extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String SENT = "SENT_SMS_ACTION";
    private final class_transliterator translite = new class_transliterator();
    private final BroadcastReceiver br_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(main.this, getString(R.string.sms_send),
                            Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    write_statistic();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    resend();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    AlertDialog.Builder dialog1 = show_dialog(getString(R.string.sms_send), getString(R.string.error_no_cervice));
                    dialog1.setPositiveButton(android.R.string.ok, null).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    AlertDialog.Builder dialog2 = show_dialog(getString(R.string.sms_send), getString(R.string.error_null_pdu));
                    dialog2.setPositiveButton(android.R.string.ok, null).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    AlertDialog.Builder dialog3 = show_dialog(getString(R.string.sms_send), getString(R.string.error_modul_is_down));
                    dialog3.setPositiveButton(android.R.string.ok, null).show();
                    break;
            }
        }
    };
    private SQLiteDatabase database = null;
    private class_sqlite dbOpenHelper = null;
    private AQuery aq;
    private SharedPreferences prefs;
    private Cursor cursor;
    private int error_resend = 0;
    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefs.getBoolean("analytics", true)) {
                Mint.initAndStartSession(main.this, "354b0769");
                Tracker t = ((AnalyticsSampleApp) this.getApplication()).getTracker(AnalyticsSampleApp.TrackerName.APP_TRACKER);
                t.setScreenName("main");
                t.send(new HitBuilders.AppViewBuilder().build());
            }

            setContentView(R.layout.main);
            dbOpenHelper = new class_sqlite(this);
            database = dbOpenHelper.openDataBase();
            aq = new AQuery(this);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitle(R.string.app_name);

            aq.id(R.id.settings).clicked(this, "settings_click");

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
                AlertDialog.Builder dialog = show_dialog(getString(R.string.first_run_title), getString(R.string.first_run_text));
                dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        start_settings();
                    }
                }).setNegativeButton(android.R.string.no, null).show();
                prefs.edit().putBoolean("first_run", false).apply();
            }


            prefs.registerOnSharedPreferenceChangeListener(this);
            registerReceiver(br_receiver, new IntentFilter(SENT));

            clear();
        } catch (Exception ex) {
            send_error(ex);
        }
    }

    private void resend() {
        try {
            if (error_resend > 4) {
                progressDialog.dismiss();
                AlertDialog.Builder dialog = show_dialog(getString(R.string.sms_send), getString(R.string.error_generic_failture));
                dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        start_settings();
                    }
                }).show();
            } else {
                send_sms(get_message(0));
                error_resend++;
            }
        } catch (Exception e) {
            send_error(e);
        }
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
        unregisterReceiver(br_receiver);
        if (cursor != null) cursor.close();
        database.close();
        dbOpenHelper.close();
    }

    private void start_settings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void set_default() {
        try {
            aq.id(R.id.user).text(prefs.getString("user", ""));
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.getInt(cursor.getColumnIndex("_id")) == prefs.getInt("location", 1)) {
                    aq.id(R.id.location).setSelection(i);
                    break;
                }
                cursor.moveToNext();
            }
        } catch (Exception ex) {
            send_error(ex);
        }
    }

    private String get_shot_text(String text, int lenth) {
        try {
            String[] word = text.split(" ");
            text = "";
            for (String aWord : word) {
                if (text.length() > 0) text += " ";
                if (aWord.length() > lenth) text += aWord.substring(0, lenth);
                else text += aWord;
            }
        } catch (Exception ex) {
            send_error(ex);
        }
        return text;
    }

    public void send_click(View v) {
        try {
            cursor.moveToPosition(aq.id(R.id.location).getSelectedItemPosition());
            //int id_stand = cursor.getString(cursor.getColumnIndex("name"));

            String user = aq.id(R.id.user).getText().toString();
            String date = aq.id(R.id.date).getText().toString();
            String time_start = aq.id(R.id.time_start).getText().toString();
            String time_end = aq.id(R.id.time_end).getText().toString();

            if (user.length() > 4 && !date.equals(getString(R.string.test_date)) && !time_start.equals(getString(R.string.test_time)) && !time_end.equals(getString(R.string.test_time))) {
                int send_type = prefs.getInt("send_type", 2);
                if (send_type == 2) {

                    AlertDialog.Builder dialog = show_dialog(getString(R.string.app_name), getString(R.string.send_type));
                    dialog.setPositiveButton(R.string.type_sms, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            send_sms(get_message(0));
                        }
                    }).setNegativeButton(R.string.type_email, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            send_mail(get_message(1));
                        }
                    }).show();
                }

                switch (send_type) {
                    case 0:
                        send_sms(get_message(send_type));
                        break;
                    case 1:
                        send_mail(get_message(send_type));
                        break;
                }
            } else {
                Toast.makeText(this, getString(R.string.vrong), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            send_error(ex);
        }
    }

    private String get_message(int send_type) {
        String message = "";
        try {
            cursor.moveToPosition(aq.id(R.id.location).getSelectedItemPosition());

            switch (send_type) {
                case 0:
                    message = "JW_STAND " + get_shot_text(translite.transliterate(cursor.getString(cursor.getColumnIndex("name"))), 10)
                            + "\r\n" + get_shot_text(translite.transliterate(aq.id(R.id.user).getText().toString()), 10)
                            + "\r\n" + aq.id(R.id.date).getText().toString()//.replace("-", "")
                            + "\r\n" + aq.id(R.id.time_start).getText().toString()//.replace(":", "")
                            + "-" + aq.id(R.id.time_end).getText().toString()//.replace(":", "")
                            + "\r\nj:" + aq.id(R.id.journals).getText().toString()
                            + "\r\nbr:" + aq.id(R.id.broshure).getText().toString()
                            + "\r\nb:" + aq.id(R.id.books).getText().toString()
                            + "\r\nd" + aq.id(R.id.dvd).getText().toString()
                            + "\r\nt:" + aq.id(R.id.talks).getText().toString()
                            + "\r\nr:" + aq.id(R.id.repeated_visits).getText().toString()
                            + "\r\ns:" + aq.id(R.id.s43).getText().toString();
                    break;
                case 1:
                    message = getString(R.string.app_name_shot) + " " + cursor.getString(cursor.getColumnIndex("name"))
                            + "\n\r\n" + aq.id(R.id.user).getText().toString()
                            + "\n\r\n" + aq.id(R.id.date).getText().toString()
                            + " " + aq.id(R.id.time_start).getText().toString()
                            + " - " + aq.id(R.id.time_end).getText().toString()
                            + "\r\n" + getString(R.string.journals) + ": " + aq.id(R.id.journals).getText().toString()
                            + "\r\n" + getString(R.string.broshures) + ": " + aq.id(R.id.broshure).getText().toString()
                            + "\r\n" + getString(R.string.books) + ": " + aq.id(R.id.books).getText().toString()
                            + "\r\n" + getString(R.string.dvd) + ": " + aq.id(R.id.dvd).getText().toString()
                            + "\r\n" + getString(R.string.talks) + ": " + aq.id(R.id.talks).getText().toString()
                            + "\r\n" + getString(R.string.repeated_visits) + ": " + aq.id(R.id.repeated_visits).getText().toString()
                            + "\r\n" + getString(R.string.blank) + ": " + aq.id(R.id.s43).getText().toString();
                    break;
            }
            Log.e("11", message.length() + "");
            Log.e("11", message);
        } catch (Exception ex) {
            send_error(ex);
        }
        return message;
    }

    private void write_statistic() {
        try {

            AlertDialog.Builder dialog = show_dialog(getString(R.string.statistic), getString(R.string.statistic_write));
            dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
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
            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    clear();
                }
            }).show();


        } catch (Exception ex) {
            send_error(ex);
        }
    }

    private void send_sms(String message) {
        try {
            final String sms = prefs.getString("sms", "").replace("(", "").replace(")", "").replace(" ", "").replace("-", "").replace("+", "");
            if (sms.length() > 9 && sms.length() < 13) {
                PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
                final SmsManager smsManager = SmsManager.getDefault();
                final ArrayList<String> mArray = smsManager.divideMessage(message);
                final ArrayList<PendingIntent> sentArrayIntents = new ArrayList<>();
                for (int i = 0; i < mArray.size(); i++) {
                    sentArrayIntents.add(sentPI);
                    Log.d("22", mArray.get(i));
                }
                AlertDialog.Builder dialog = show_dialog(getString(R.string.sms_sending), getString(R.string.sms_text) + "\r\n\r\n" + message + "\r\n\r\n" + getString(R.string.sms_count) + " " + mArray.size());
                dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        smsManager.sendMultipartTextMessage(sms, null, mArray, sentArrayIntents, null);
                        show_progress_dialog();
                    }
                }).setNegativeButton(android.R.string.no, null).show();
            } else Toast.makeText(this, getString(R.string.sms_vrong), Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            send_error(ex);
        }
    }

    private void show_progress_dialog() {
        progressDialog = ProgressDialog.show(this, getString(R.string.sms_send), getString(R.string.sms_sending));
    }

    private void send_mail(String message) {
        try {
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
        } catch (Exception ex) {
            send_error(ex);
        }
    }


    boolean email_validate(String email) {
        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    private void clear() {
        try {
            //aq.id(R.id.date).text("0000-00-00");
            date_set(null);
            time_set();
            //aq.id(R.id.time_start).text("00:00");
            //aq.id(R.id.time_end).text("00:00");
            aq.id(R.id.journals).text(R.string.test_ziro);
            aq.id(R.id.broshure).text(R.string.test_ziro);
            aq.id(R.id.books).text(R.string.test_ziro);
            aq.id(R.id.dvd).text(R.string.test_ziro);
            aq.id(R.id.talks).text(R.string.test_ziro);
            aq.id(R.id.repeated_visits).text(R.string.test_ziro);
            aq.id(R.id.s43).text(R.string.test_ziro);
        } catch (Exception ex) {
            send_error(ex);
        }
    }

    public void time_click(final View v) {
        try {
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
                            time_set_show(id, hourOfDay, minute);
                        }
                    }, hours, minutes, true);
            tpd.show();
        } catch (Exception ex) {
            send_error(ex);
        }
    }

    public void date_click(View v) {
        try {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, monthOfYear, dayOfMonth);
                            date_set(calendar);
                        }
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        } catch (Exception ex) {
            send_error(ex);
        }
    }

    private void time_set_show(int id, int hours, int minutes) {
        try {
            String h = String.valueOf(hours);
            if (hours < 10) h = "0" + h;
            String m = String.valueOf(minutes);
            if (minutes < 10) m = "0" + m;
            aq.id(id).text(h + ":" + m);
        } catch (Exception ex) {
            send_error(ex);
        }
    }

    private void time_set() {
        try {
            Calendar c = Calendar.getInstance();
            int hours = c.get(Calendar.HOUR_OF_DAY);
            int minutes = 0;
            if (c.get(Calendar.MINUTE) < 30) minutes = 0;
            else minutes = 30;

            int hours_start = hours;
            int minutes_start = 0;

            if (minutes == 30) {
                hours_start -= 1;
                minutes_start = 0;
            } else {
                hours_start -= 2;
                minutes_start = 30;
            }

            time_set_show(R.id.time_start, hours_start, minutes_start);
            time_set_show(R.id.time_end, hours, minutes);
        } catch (Exception ex) {
            send_error(ex);
        }
    }

    private void date_set(Calendar calendar) {
        try {
            if (calendar == null) calendar = Calendar.getInstance();
            String m = String.valueOf(calendar.get(Calendar.MONTH));
            if (calendar.get(Calendar.MONTH) < 10) m = "0" + m;
            String d = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            if (calendar.get(Calendar.DAY_OF_MONTH) < 10) d = "0" + d;
            aq.id(R.id.date).text(calendar.get(Calendar.YEAR) + "-" + m + "-" + d);
        } catch (Exception ex) {
            send_error(ex);
        }
    }

    public void settings_click(View v) {
        start_settings();
    }

    public void picker_click(View v) {
        try {
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
        } catch (Exception ex) {
            send_error(ex);
        }
    }

    private void set_picket_textbox(int tv, int plus) {
        try {
            int value = Integer.parseInt(aq.id(tv).getText().toString()) + plus;
            if (value < 0) value = 0;
            aq.id(tv).text(String.valueOf(value));
        } catch (Exception ex) {
            send_error(ex);
        }
    }

    private AlertDialog.Builder show_dialog(String title, String text) {
        return new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        set_default();
    }

    private void send_error(Exception ex) {
        Log.e("EERROORR", "main", ex);
        Mint.logException(ex);
    }
}
