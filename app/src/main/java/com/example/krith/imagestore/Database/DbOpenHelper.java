package com.example.krith.imagestore.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

/**
 * Created by krith on 28/09/16.
 */

public class DbOpenHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static DbOpenHelper mInstance = null;
    private Context context;
    private static final String TABLE = "image_item";
    private static final String ROW_ID = "_id";
    private static final String DATA = "data";

    private static final String CREATE_IMAGE_ITEM = "CREATE TABLE IF NOT EXISTS "
            + TABLE + "("
            + ROW_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + DATA + " TEXT NOT NULL"
            + ")";


    public DbOpenHelper(Context context) {
        super(context, "imagestore.db", null, VERSION);
        this.context = context;
    }

    public static DbOpenHelper getInstance(Context context) {

        if (mInstance == null) {
            mInstance = new DbOpenHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_IMAGE_ITEM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean bulkInsert(int rows, String path) {
        try {
            String sql = "INSERT OR REPLACE INTO " + TABLE + " ( " + DATA + " ) VALUES ( ? )";
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransactionNonExclusive();
            // db.beginTransaction();

            SQLiteStatement stmt = db.compileStatement(sql);

            for (int x = 1; x <= rows; x++) {
                stmt.bindString(1, path);
                stmt.execute();
                stmt.clearBindings();
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void deleteRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE);
        db.close();
    }

    public String getRowData(int rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE + " WHERE " + ROW_ID + "=" + rowId, null);
        cursor.moveToFirst();
        if (cursor.getCount() <= 0)
            return null;
        String path = Db.getString(cursor, DATA);
        cursor.close();
        db.close();
        return path;
    }
}
