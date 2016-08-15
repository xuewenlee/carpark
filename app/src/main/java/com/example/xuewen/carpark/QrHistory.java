package com.example.xuewen.carpark;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class QrHistory extends AppCompatActivity {

    public static final String strHisDate = QrSqlite.colDate;
    private static final String strHisTime = QrSqlite.colTime;
    public static final String strHisCode = QrSqlite.colCode;
    private static final String strHisAmount = QrSqlite.colAmount;

    QrSqlite dbQrSqlite;
    ListView lvHistory;
    ArrayList<HashMap<String, String>> alHis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        dbQrSqlite = new QrSqlite(getApplicationContext());
        lvHistory = (ListView)findViewById(R.id.listViewHistory);

        alHis = new ArrayList<HashMap<String, String>>();

        Runnable run = new Runnable() {
            @Override
            public void run() {
                String strSql = "Select * from " +QrSqlite.tblNameHistory;
                Cursor currExp = dbQrSqlite.getReadableDatabase().rawQuery(strSql, null);

                while(currExp.moveToNext())
                {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(strHisDate, currExp.getString(currExp.getColumnIndex(QrSqlite.colDate)));
                    map.put(strHisTime, currExp.getString(currExp.getColumnIndex(QrSqlite.colTime)));
                    map.put(strHisCode, currExp.getString(currExp.getColumnIndex(QrSqlite.colCode)));
                    map.put(strHisAmount, currExp.getString(currExp.getColumnIndex(QrSqlite.colAmount)));

                    alHis.add(map);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListAdapter adapter = new SimpleAdapter(QrHistory.this, alHis, R.layout.qrhistoryinfo,
                                new String[]{strHisDate,strHisTime,strHisCode,strHisAmount},
                                new int[]{R.id.txtVwHDate,R.id.txtVwHTime,R.id.txtVwHCode,R.id.txtVwHAmount});

                        lvHistory.setAdapter(adapter);
                    }
                });
            }
        };
        Thread thr = new Thread(run);
        thr.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivityForResult(intent, 0);
    }
}
