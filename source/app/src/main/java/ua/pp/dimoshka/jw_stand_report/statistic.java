package ua.pp.dimoshka.jw_stand_report;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.androidquery.AQuery;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.splunk.mint.Mint;


public class statistic extends ActionBarActivity {

    private SQLiteDatabase database = null;
    private class_sqlite dbOpenHelper = null;
    private AQuery aq;
    private Cursor cursor;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefs.getBoolean("analytics", true)) {
                Mint.initAndStartSession(statistic.this, "354b0769");
                Tracker t = ((AnalyticsSampleApp) this.getApplication()).getTracker(AnalyticsSampleApp.TrackerName.APP_TRACKER);
                t.setScreenName("statistic");
                t.send(new HitBuilders.AppViewBuilder().build());
            }
            setContentView(R.layout.statistic);
            dbOpenHelper = new class_sqlite(this);
            database = dbOpenHelper.openDataBase();
            aq = new AQuery(this);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitle(R.string.statistic);


            Cursor cur1 = database.rawQuery("select * from (select sum(`journal`) as `count_journal` from `statistic`) as `count_journal`, (select sum(`broshure`) as `count_broshure` from `statistic`) as `count_broshure`, (select sum(`dvd`) as `count_dvd` from `statistic`) as `count_dvd`, (select sum(`book`) as `count_book` from `statistic`) as `count_book`, (select sum(`talk`) as `count_talk` from `statistic`) as `count_talk`, (select sum(`repeated_visit`) as `count_repeated_visit` from `statistic`) as `count_repeated_visit`, (select sum(`s_blank`) as `count_s_blank` from `statistic`) as `count_s_blank`;", null);

            if (cur1.getCount() > 0) {
                cur1.moveToFirst();

                aq.id(R.id.journals).text(cur1.getString(cur1.getColumnIndex("count_journal")));
                aq.id(R.id.journals).text(cur1.getString(cur1.getColumnIndex("count_broshure")));
                aq.id(R.id.journals).text(cur1.getString(cur1.getColumnIndex("count_dvd")));
                aq.id(R.id.journals).text(cur1.getString(cur1.getColumnIndex("count_book")));
                aq.id(R.id.journals).text(cur1.getString(cur1.getColumnIndex("count_talk")));
                aq.id(R.id.journals).text(cur1.getString(cur1.getColumnIndex("count_repeated_visit")));
                aq.id(R.id.journals).text(cur1.getString(cur1.getColumnIndex("count_s_blank")));


            }


        } catch (Exception ex) {
            send_error(ex);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null) cursor.close();
        database.close();
        dbOpenHelper.close();
    }

    private void send_error(Exception ex) {
        Log.e("EERROORR", "statistic", ex);
        Mint.logException(ex);
    }
}
