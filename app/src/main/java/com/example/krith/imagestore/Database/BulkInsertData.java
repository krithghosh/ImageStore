package com.example.krith.imagestore.Database;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.widget.EditText;

import com.example.krith.imagestore.Events.InsertionCompleteEvent;

/**
 * Created by krith on 28/09/16.
 */

public class BulkInsertData extends AsyncTask<Void, Integer, Boolean> {

    Context context;
    String path;
    DbOpenHelper dbOpenHelper;
    int rows;
    ProgressDialog progressDialog;
    SQLiteDatabase db;
    private static final String TABLE = "image_item";
    private static final String DATA = "data";

    public BulkInsertData(int rows, String path, Context context, ProgressDialog progressDialog, SQLiteDatabase db) {
        this.rows = rows;
        this.path = path;
        this.dbOpenHelper = DbOpenHelper.getInstance(context);
        this.progressDialog = progressDialog;
        this.db = db;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        //return dbOpenHelper.bulkInsert(rows, path);
        String sql = "INSERT OR REPLACE INTO " + TABLE + " ( " + DATA + " ) VALUES ( ? )";
        db.beginTransactionNonExclusive();

        SQLiteStatement stmt = db.compileStatement(sql);

        for (int x = 1; x <= rows; x++) {
            stmt.bindString(1, path);
            stmt.execute();
            stmt.clearBindings();
            publishProgress(x, rows);
            if (isCancelled()) break;
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressDialog.setProgress((values[0]));
    }

    @Override
    protected void onPostExecute(Boolean isDone) {
        super.onPostExecute(isDone);
        progressDialog.dismiss();
        new InsertionCompleteEvent().post();
    }
}
