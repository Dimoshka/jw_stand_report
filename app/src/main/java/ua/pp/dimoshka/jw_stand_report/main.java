package ua.pp.dimoshka.jw_stand_report;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.androidquery.AQuery;

import java.util.Calendar;


public class main extends ActionBarActivity {

    private AQuery aq;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        aq = new AQuery(this);
        pref = getSharedPreferences("settings", MODE_PRIVATE);


        aq.id(R.id.send).clicked(this, "send_click");
        aq.id(R.id.t_date).clicked(this, "date_click");

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
    }


    public void send_click(View v) {

        startActivity(new Intent(this, preferences.class));
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
                        aq.id(R.id.t_date).text(year + "-" + m + "-" + d);
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
}
