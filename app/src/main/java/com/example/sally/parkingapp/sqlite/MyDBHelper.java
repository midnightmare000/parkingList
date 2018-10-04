package com.example.sally.parkingapp.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MyDBHelper extends SQLiteOpenHelper {

    private final static int _DBVersion=1;
    private final static String _DBNAME = "parking.db";
    private final static String PARKING="Parking";

    private static SQLiteDatabase database;

    public MyDBHelper(Context context) {
        super(context, _DBNAME, null, _DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL = "CREATE TABLE IF NOT EXISTS " + PARKING + "( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "area TEXT, " +
                "name TEXT," +
                "address TEXT," +
                "serviceTime TEXT," +
                "lat REAL," +
                "lon REAL" +
                ");";
        sqLiteDatabase.execSQL(SQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new MyDBHelper(context).getWritableDatabase();
        }
        return database;
    }
}
