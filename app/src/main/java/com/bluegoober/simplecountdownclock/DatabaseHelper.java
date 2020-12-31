package com.bluegoober.simplecountdownclock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "CountdownsDB.db";
    public static final String COUNTDOWNS_TABLE_NAME = "countdowns";
    public static final String COUNTDOWNS_COUNTDOWN_ID = "id";
    public static final String COUNTDOWNS_COUNTDOWN_NAME = "name";
    public static final String COUNTDOWNS_COUNTDOWN_DATE_MILLI = "datemilli";
    public static final String COUNTDOWNS_COUNTDOWN_DATETIME = "datetime";
    public static final String COUNTDOWNS_COUNTDOWN_DESC = "description";

    public final static String CREATE_TABLE = "";



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("create table countdowns " +
                "(id integer primary key, name text, datemilli text, datetime text, description text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if(oldVersion < 2) {

        }
    }

    public boolean insertCountdown(CountdownObject countdown) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", countdown.getName());
        contentValues.put("datemilli", countdown.getLongDate());
        contentValues.put("datetime", countdown.getEventDate());
        contentValues.put("description", countdown.getDesc());
        database.insert("countdowns", null, contentValues);
        database.close();
        return true;
    }

    public CountdownObject getCountdown(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + COUNTDOWNS_TABLE_NAME + " WHERE " + COUNTDOWNS_COUNTDOWN_ID + " = " + id;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor != null && cursor.moveToFirst()) {
            int countdownId = cursor.getInt(cursor.getColumnIndex(COUNTDOWNS_COUNTDOWN_ID));
            long dateMilli = Long.parseLong(cursor.getString(cursor.getColumnIndex(COUNTDOWNS_COUNTDOWN_DATE_MILLI)));
            String countdownName = cursor.getString(cursor.getColumnIndex(COUNTDOWNS_COUNTDOWN_NAME));
            String countdownDescription = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COUNTDOWNS_COUNTDOWN_DESC));
            String countdownDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COUNTDOWNS_COUNTDOWN_DATETIME));
            CountdownObject countdownObject = new CountdownObject(countdownId, dateMilli, countdownName, countdownDescription, countdownDate);
            cursor.close();
            db.close();
            return countdownObject;
        }

        else {
            db.close();
            return  null;
        }
    }

    public boolean updateCountdown (CountdownObject countdownObject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", countdownObject.getName());
        contentValues.put("datemilli", countdownObject.getLongDate());
        contentValues.put("datetime", countdownObject.getEventDate());
        contentValues.put("description", countdownObject.getDesc());
        db.update("countdowns", contentValues, "id = ? ", new String[] { Integer.toString(countdownObject.getId()) } );
        db.close();
        return true;
    }

    public Integer deleteCountdown (int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int dbDelete = db.delete("countdowns", "id = ? ", new String[] { Integer.toString(id) });;
        db.close();
        return dbDelete;
    }

    public ArrayList<CountdownObject> getAllCountdowns() {
        ArrayList<CountdownObject> countdownList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "select * from countdowns", null );
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            int countdownId = cursor.getInt(cursor.getColumnIndex(COUNTDOWNS_COUNTDOWN_ID));
            long dateMilli = Long.parseLong(cursor.getString(cursor.getColumnIndex(COUNTDOWNS_COUNTDOWN_DATE_MILLI)));
            String countdownName = cursor.getString(cursor.getColumnIndex(COUNTDOWNS_COUNTDOWN_NAME));
            String countdownDescription = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COUNTDOWNS_COUNTDOWN_DESC));
            String countdownDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COUNTDOWNS_COUNTDOWN_DATETIME));
            CountdownObject countdownObject = new CountdownObject(countdownId, dateMilli, countdownName, countdownDescription, countdownDate);
            countdownList.add(countdownObject);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return countdownList;
    }
}
