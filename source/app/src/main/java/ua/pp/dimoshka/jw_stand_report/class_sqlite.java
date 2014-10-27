package ua.pp.dimoshka.jw_stand_report;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class class_sqlite extends SQLiteAssetHelper {

    public class_sqlite(Context context) {
        super(context, context.getString(R.string.db_name), null, Integer
                .valueOf(context.getString(R.string.db_version)).intValue());
        setForcedUpgrade(Integer
                .valueOf(context.getString(R.string.db_version)).intValue());
    }

    public SQLiteDatabase openDataBase() {
        return this.getWritableDatabase();
    }
}