package com.example.david.easycheckmeasureapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by David on 4/27/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "checkMeasureManager";

    // table names
    private static final String TABLE_CHECK_MEASURE = "Check_Measure";
    private static final String TABLE_MEASUREMENT = "Measurement";

    // CHECK MEASURE Table Columns names

    private static final String KEY_CHECK_MEASURE_ID = "Check_Measure_ID";

    private static final String ADDRESS = "Address";
    private static final String COMPANY = "Company";
    private static final String CUSTOMER = "Customer";
    private static final String DATE = "Date";

    // MEASUREMENT Table Columns names

    private static final String KEY_MEASUREMENT_ID = "Measurement_ID";

    private static final String WIDTH = "Width";
    private static final String LENGTH = "Length";
    private static final String ROOM = "Room";
    private static final String CONTROL = "Control";
    private static final String MOUNT = "Mount";
    private static final String SPECIAL_NOTE = "Special_Note";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_CHECK_MEASURE_TABLE = "CREATE TABLE " + TABLE_CHECK_MEASURE + " ( "
                + KEY_CHECK_MEASURE_ID + " INTEGER PRIMARY KEY," + ADDRESS + " TEXT , "
                + COMPANY + " TEXT , " + CUSTOMER + " TEXT , " + DATE + " TEXT )";
        String CREATE_MEASUREMENT_TABLE = "CREATE TABLE " + TABLE_MEASUREMENT + " ( "
                + KEY_CHECK_MEASURE_ID + " INTEGER NOT NULL , " + KEY_MEASUREMENT_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , "
                + WIDTH + " TEXT , " + LENGTH + " TEXT , " + ROOM + " TEXT , " + CONTROL +
                " TEXT , " + MOUNT + " TEXT , " + SPECIAL_NOTE + " )";
        db.execSQL(CREATE_CHECK_MEASURE_TABLE);
        db.execSQL(CREATE_MEASUREMENT_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECK_MEASURE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEASUREMENT);

        // Create tables again
        onCreate(db);
    }

    void createCheckMeasure(int cm_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CHECK_MEASURE_ID, cm_id);
        db.insert(TABLE_CHECK_MEASURE, null, values);
        db.close(); // Closing database connection
    }

    void updateCheckMeasure(int cm_id, String address, String company, String customer, Date date) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CHECK_MEASURE_ID, cm_id);
        values.put(ADDRESS, address);
        values.put(COMPANY, company);
        values.put(CUSTOMER, customer);
        SimpleDateFormat dfDate = new SimpleDateFormat("EEE, MMM d yyyy hh:mm aaa");
        values.put(DATE, dfDate.format(date));

        int num_affected = db.update(TABLE_CHECK_MEASURE, values, KEY_CHECK_MEASURE_ID + " = ? ",
                new String[]{String.valueOf(cm_id)});
        if (num_affected == 0) {
            values.put(KEY_CHECK_MEASURE_ID, cm_id);
            db.insert(TABLE_CHECK_MEASURE, null, values);
            // Inserting Row
        }
        db.close(); // Closing database connection
    }


    boolean updateMeasurement(int cm_id, int measurement_id, String width, String length, String room,
                              String control, String mount, String special_note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WIDTH, width);
        values.put(LENGTH, length);
        values.put(ROOM, room);
        values.put(CONTROL, control);
        values.put(MOUNT, mount);
        values.put(SPECIAL_NOTE, special_note);

        int updatedRecordCount = db.update(TABLE_MEASUREMENT, values, KEY_CHECK_MEASURE_ID + " = ? and " +
                        KEY_MEASUREMENT_ID + "=?",
                new String[]{String.valueOf(cm_id), String.valueOf(measurement_id)});
        return updatedRecordCount > 0;
    }

    void createMeasurement(int cm_id, String width, String length, String room,
                           String control, String mount, String special_note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WIDTH, width);
        values.put(LENGTH, length);
        values.put(ROOM, room);
        values.put(CONTROL, control);
        values.put(MOUNT, mount);
        values.put(SPECIAL_NOTE, special_note);
        values.put(KEY_CHECK_MEASURE_ID, cm_id);
        db.insert(TABLE_MEASUREMENT, null, values);
        db.close(); // Closing database connection
    }


    // Deleting single cm
    public void deleteCheckMeasure(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CHECK_MEASURE, KEY_CHECK_MEASURE_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.delete(TABLE_MEASUREMENT, KEY_CHECK_MEASURE_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    // Deleting single measurement
    public void deleteMeasurement(int cm_id, int m_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.e("MAK", "Deleting measurement: " + String.valueOf(m_id));
        db.delete(TABLE_MEASUREMENT, KEY_CHECK_MEASURE_ID + " = ?" + " and " + KEY_MEASUREMENT_ID + " = ?",
                new String[]{String.valueOf(cm_id), String.valueOf(m_id)});
        db.close();
    }

    public ArrayList<ArrayList<String>> selectRecordsFromDBList(String rawQuery, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<ArrayList<String>> retList = new ArrayList<ArrayList<String>>();
        ArrayList<String> list = new ArrayList<String>();
        Cursor cursor = db.rawQuery(rawQuery, selectionArgs);
        if (cursor.moveToFirst()) {
            do {
                list = new ArrayList<String>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    list.add(cursor.getString(i));
                }
                retList.add(list);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return retList;

    }

    // Getting single contact
    ArrayList<ArrayList<String>> getCheckMeasure(int id) {

        String query = "SELECT " + COMPANY + "," + CUSTOMER + "," + ADDRESS + "," + DATE + " " +
                "FROM " + TABLE_CHECK_MEASURE + " " +
                "WHERE " + KEY_CHECK_MEASURE_ID + " = " + Integer.toString(id);
        return selectRecordsFromDBList(query, null);
    }

    // Getting single contact
    ArrayList<ArrayList<String>> getAllCheckMeasures() {

        String query = "SELECT " + COMPANY + "," + CUSTOMER + "," + ADDRESS + "," + DATE + " " +
                "FROM " + TABLE_CHECK_MEASURE;
        return selectRecordsFromDBList(query, null);
    }

    ArrayList<ArrayList<String>> getAllMeasurements(int id) {

        String query = "SELECT " + WIDTH + "," + LENGTH + "," + ROOM + "," + CONTROL + "," + MOUNT + "," + SPECIAL_NOTE + "," + KEY_MEASUREMENT_ID + " " +
                "FROM " + TABLE_MEASUREMENT + " " +
                "WHERE " + KEY_CHECK_MEASURE_ID + " = " + Integer.toString(id);
        return selectRecordsFromDBList(query, null);
    }

    ArrayList<ArrayList<String>> getSingleMeasurement(int cm_id, int measurement_id) {

        String query = "SELECT " + WIDTH + "," + LENGTH + "," + ROOM + "," + CONTROL + "," + MOUNT + "," + SPECIAL_NOTE + "," + KEY_MEASUREMENT_ID + " " +
                "FROM " + TABLE_MEASUREMENT + " " +
                "WHERE " + KEY_CHECK_MEASURE_ID + " = " + Integer.toString(cm_id) + " and " +
                KEY_MEASUREMENT_ID + " = " + Integer.toString(measurement_id);
        return selectRecordsFromDBList(query, null);
    }

    int getCheckMeasureCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CHECK_MEASURE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int ret = cursor.getCount();
        cursor.close();

        // return count
        return ret;
    }

    int getMeasurementCount(int cm_id) {
        String countQuery = "SELECT  * FROM " + TABLE_MEASUREMENT +
                " WHERE " + KEY_CHECK_MEASURE_ID + " = " + String.valueOf((cm_id));
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int ret = cursor.getCount();
        cursor.close();

        // return count
        return ret;
    }

}