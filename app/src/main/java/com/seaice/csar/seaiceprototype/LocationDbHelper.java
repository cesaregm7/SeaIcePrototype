package com.seaice.csar.seaiceprototype;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocationDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "StoredLocations.db";
    public static final String TABLE_NAME = "Locations";
    public static final String COLUMN_NAME_ID = "Id";
    public static final String COLUMN_NAME_LATITUD = "Latitud";
    public static final String COLUMN_NAME_LONGITUD = "Longitud";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_LATITUD + " REAL, " +
                    COLUMN_NAME_LONGITUD + " REAL" +
                    " )";

    public LocationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertLocation(float longitud, float latitud)
    {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_LATITUD, latitud);
        values.put(COLUMN_NAME_LONGITUD, longitud);

        long newRowId;
        newRowId = db.insert(
                TABLE_NAME,
                null,
                values);

        return newRowId;
    }

    public boolean deleteLocation(long id)
    {
        SQLiteDatabase db = getWritableDatabase();

        // Define 'where' part of query.
        String selection = COLUMN_NAME_ID + " = ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(id) };
// Issue SQL statement.
        return db.delete(TABLE_NAME, selection, selectionArgs) > 0;
    }


    public Cursor readAllLocation()
    {
        SQLiteDatabase db = getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                COLUMN_NAME_ID,
                COLUMN_NAME_LATITUD,
                COLUMN_NAME_LONGITUD
        };

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                COLUMN_NAME_ID + " ASC";

        return db.query(
                TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
    }
}