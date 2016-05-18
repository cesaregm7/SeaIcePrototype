package com.seaice.csar.seaiceprototype;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocationDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "StoredLocations.db";

    public static final String TABLE_NAME_LOCATIONS = "Locations";
    public static final String TABLE_NAME_REPORTS = "Reports";

    public static final String COLUMN_NAME_ID = "Id";
    public static final String COLUMN_NAME_ID_SERVER = "IdServer";
    public static final String COLUMN_NAME_LATITUD = "Latitud";
    public static final String COLUMN_NAME_LONGITUD = "Longitud";
    public static final String COLUMN_NAME_INFO= "Info";
    public static final String COLUMN_NAME_TIPO= "Tipo";


    public static final String COLUMN_NAME_TITULO= "Titulo";
    public static final String COLUMN_NAME_DESCRIPCION= "Descripcion";
    public static final String COLUMN_NAME_PATH= "Path";

    private static final String SQL_CREATE_TABLE_LOCATIONS =
            "CREATE TABLE " + TABLE_NAME_LOCATIONS + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY," + // PRIMARY KEY
                    COLUMN_NAME_ID_SERVER + " INTEGER, " +
                    COLUMN_NAME_LATITUD + " REAL, " +
                    COLUMN_NAME_LONGITUD + " REAL," +
                    COLUMN_NAME_INFO + " TEXT," +
                    COLUMN_NAME_TIPO + " INTEGER" +
                    " )";

    private static final String SQL_CREATE_TABLE_REPORTS =
            "CREATE TABLE " + TABLE_NAME_REPORTS + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_TITULO + " TEXT, " +
                    COLUMN_NAME_DESCRIPCION + " TEXT," +
                    COLUMN_NAME_PATH + " TEXT" +
                    " )";

    public LocationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_LOCATIONS);
        db.execSQL(SQL_CREATE_TABLE_REPORTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void updateInfo(int id, String info)
    {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_INFO, info);

        String where = COLUMN_NAME_ID + " = ?";

        String whereArgs[] = {id+""};

        int newRowId;
        newRowId = db.update(
                TABLE_NAME_LOCATIONS,
                values,
                where,
                whereArgs);

    }

    public void updateReport(int id, String titulo, String descripcion, String path)
    {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_TITULO, titulo);
        values.put(COLUMN_NAME_DESCRIPCION, descripcion);
        values.put(COLUMN_NAME_PATH, path);

        String where = COLUMN_NAME_ID + " = ?";

        String whereArgs[] = {id+""};

        int newRowId;
        newRowId = db.update(
                TABLE_NAME_REPORTS,
                values,
                where,
                whereArgs);

    }

    private long insertReport(String titulo, String descripcion, String path)
    {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_TITULO, titulo);
        values.put(COLUMN_NAME_DESCRIPCION, descripcion);
        values.put(COLUMN_NAME_PATH, path);

        long newRowId;
        newRowId = db.insert(
                TABLE_NAME_REPORTS,
                null,
                values);

        return newRowId;
    }

    private long insertLocation(double latitud, double longitud, long tipo, long id_server)
    {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_ID_SERVER, id_server);
        values.put(COLUMN_NAME_LATITUD, (float)latitud);
        values.put(COLUMN_NAME_LONGITUD, (float)longitud);
        values.put(COLUMN_NAME_INFO, "");
        values.put(COLUMN_NAME_TIPO, tipo);

        long newRowId;
        newRowId = db.insert(
                TABLE_NAME_LOCATIONS,
                null,
                values);

        return newRowId;
    }

    public long insertFullLocation(double latitud, double longitud)
    {
        return insertLocation(latitud,longitud,-1, -1);
    }

    public long insertFullReport(double latitud, double longitud,String titulo, String descripcion, String path, long id_server)
    {
        long tipo = insertReport(titulo, descripcion, path);
        if(tipo < 0)
        {
            Log.e("ERROR Tipo:","tipo: "+tipo);
        }
        return insertLocation(latitud,longitud, tipo, id_server);
    }

    public boolean deleteLocation(long id)
    {
        SQLiteDatabase db = getWritableDatabase();

        // Define 'where' part of query.
        String selection = COLUMN_NAME_ID + " = ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(id) };
// Issue SQL statement.
        return db.delete(TABLE_NAME_LOCATIONS, selection, selectionArgs) > 0;
    }

    public boolean deleteReport(long id_server)
    {
        SQLiteDatabase db = getWritableDatabase();

        // Define 'where' part of query.
        String selection = COLUMN_NAME_ID_SERVER + " = ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(id_server) };
// Issue SQL statement.
        return db.delete(TABLE_NAME_LOCATIONS, selection, selectionArgs) > 0;
    }

    public Cursor readSingleReport(long id)
    {
        SQLiteDatabase db = getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                COLUMN_NAME_ID,
                COLUMN_NAME_TITULO,
                COLUMN_NAME_DESCRIPCION,
                COLUMN_NAME_PATH
        };

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                COLUMN_NAME_ID + " ASC";

        //String selection = COLUMN_NAME_ID + " = " + String.valueOf(id);

        String selection = COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        return db.query(
                TABLE_NAME_REPORTS,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
    }

    public Cursor readAllLocation()
    {
        SQLiteDatabase db = getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                COLUMN_NAME_ID,
                COLUMN_NAME_ID_SERVER,
                COLUMN_NAME_LATITUD,
                COLUMN_NAME_LONGITUD,
                COLUMN_NAME_INFO,
                COLUMN_NAME_TIPO
        };

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                COLUMN_NAME_ID + " ASC";

        return db.query(
                TABLE_NAME_LOCATIONS,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
    }
}