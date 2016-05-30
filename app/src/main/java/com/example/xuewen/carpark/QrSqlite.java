package com.example.xuewen.carpark;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by xuewen on 17/05/2016.
 */
public class QrSqlite extends SQLiteOpenHelper {

    public static final String dbName = "dbQrCode";
    public static final String tblNameQr = "qr";
    public static final String tblNameHistory = "history";
    public static final String colDate = "qrDate";
    public static final String colTime = "qrTime";
    public static final String colCode = "qrCode";
    public static final String colCredit = "qrCredit";
    public static final String colAmount = "qrAmount";

    public static final String tableQR ="CREATE TABLE IF NOT EXISTS qr(qrCode VARCHAR, qrCredit VARCHAR);";
    public static final String tableHistory = "CREATE TABLE IF NOT EXISTS history(qrDate DATE, qrTime VARCHAR, qrCode VARCHAR, qrAmount VARCHAR);";

    public QrSqlite(Context context) {
        super(context, dbName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(tableQR);
        db.execSQL(tableHistory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS qr");
        onCreate(db);
    }

    public void fnExecuteSql(String strSql, Context appContext)   {
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            db.execSQL(strSql);
        }catch (Exception e){
            Log.d("unable to run query", "error1") ;
        }
    }

    public Cursor fnResultSet(String sql){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cur =  db.rawQuery(sql, null);
        return cur;
    }
}
