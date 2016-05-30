package com.example.xuewen.carpark;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.Result;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

//    SurfaceView cameraView;
//    CameraSource cameraSource;
//    TextView codeInfo;
    private ZXingScannerView mScannerView;
    String strTotal, plateNumber, startTime, endTime, duration, total, date;

    QrSqlite dbQrCode;
    String strCode, strCredit;
    AlertDialog.Builder builder;
    String strFromPHP;
     AlertDialog alert1;
    public static Context ctx;

    Connection conn = new Connection();
    private static String strURL = "http://192.168.43.14/parkingDB/showParking.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ctx=this;

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();         // Start camera

        Intent intent = getIntent();
        strTotal = intent.getStringExtra("varTotalAmount");
//        noPicker = intent.getStringExtra("noPicker");
        plateNumber = intent.getStringExtra("plateNumber");
        date=intent.getStringExtra("date");
        startTime = intent.getStringExtra("startTime");
        endTime = intent.getStringExtra("endTime");
        duration = intent.getStringExtra("duration");
        total = intent.getStringExtra("total");


    }

//    public void QrScanner(View view){
//
//
//    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();   // Stop camera on pause
    }



    @Override
    public void handleResult(final Result rawResult) {
        // Do something with the result here
        Log.e("handler", rawResult.getText()); // Prints scan results
        Log.e("handler", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode)

        // show the scanner result into dialog box.
        builder = new AlertDialog.Builder(getSupportActionBar().getThemedContext());

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.brush_opts_dialog,null);
        builder.setView(dialogView);
        Button btnPay = (Button)dialogView.findViewById(R.id.btnPay);


        builder.setTitle("Total Payment Amount = RM " + strTotal);

        Runnable runCredit = new Runnable() {
            AlertDialog dialog = null;
            @Override
            public void run() {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("selectFN","fnGetCredit"));
                params.add(new BasicNameValuePair("qrResult", rawResult.getText()));

                final JSONObject jsonObj =
                        conn.makeHttpRequest(strURL, "POST", params);


                        strFromPHP = jsonObj.optString("credit");
//                        char amount = strFromPHP.charAt(0);
//                        char[] amountArray = strFromPHP.toCharArray();
//                        builder.setMessage(rawResult.getText() + strFromPHP);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(strFromPHP + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

                        builder.setMessage("Credit Amount is RM " + strFromPHP);
                        alert1=builder.create();
                        alert1.show();
                    }
                });

//                dialog = builder.create();

            }
        };
        Thread thr = new Thread(runCredit);
        thr.start();

//        builder.setMessage(rawResult.getText());


        strCode = rawResult.getText();
//        strCredit = strFromPHP;

        btnPay .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dbQrCode = new QrSqlite(getApplicationContext());

                Runnable run = new Runnable() {
                    @Override
                    public void run() {

                        /*Insert parking details*/
                        List<NameValuePair> paramInsert = new ArrayList<NameValuePair>();
                        paramInsert.add(new BasicNameValuePair("selectFN","fnInsert"));
                        paramInsert.add(new BasicNameValuePair("plate_num",plateNumber));
                        paramInsert.add(new BasicNameValuePair("parking_start_time",startTime));
                        paramInsert.add(new BasicNameValuePair("parking_end_time",endTime));
                        paramInsert.add(new BasicNameValuePair("parking_duration",duration));
                        paramInsert.add(new BasicNameValuePair("parking_amount",total));

                        final JSONObject jsonObjInsert =
                                conn.makeHttpRequest(strURL, "POST", paramInsert);

                        /*Insert QR code into sqlite*/
                        String strQry = "Insert into qr values( '" + strCode + "', '" + strFromPHP + "');";
                        dbQrCode.fnExecuteSql(strQry, getApplicationContext());

                        String qrcode = "QR CODE       RM";
                        String strQryHistory = "Insert into history values( '" + date + "', '" + startTime + "', '" + qrcode + "', '" + strTotal + "');";
                        dbQrCode.fnExecuteSql(strQryHistory, getApplicationContext());

                        /*Update credit in QR code*/
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("selectFN", "fnUpdateQrPayment"));
                        params.add(new BasicNameValuePair("qrResult", rawResult.getText()));
                        params.add(new BasicNameValuePair("total_amount", strTotal));

                        final JSONObject jsonObj =
                                conn.makeHttpRequest(strURL, "POST", params);

                        //get balance from database to update credit sqlite
                        String updatedBalance = jsonObj.optString("balance");

                        /*update QR code credit in sqlite*/
                        String sqludt = "UPDATE qr set qrCredit = '" + updatedBalance + "' where qrCode = '" + strCode + "';";
                        dbQrCode.fnExecuteSql(sqludt, getApplicationContext());

                        /*update payment status of parking info*/
                        List<NameValuePair> paramStatus = new ArrayList<NameValuePair>();
                        paramStatus.add(new BasicNameValuePair("selectFN","fnUpdatePayment"));
                        paramStatus.add(new BasicNameValuePair("plate_num",plateNumber));
                        paramStatus.add(new BasicNameValuePair("paymentStat", "approved"));

                        final JSONObject jsonObjStatus =
                                conn.makeHttpRequest(strURL, "POST", paramStatus);

                    }
                };
                Thread thr = new Thread(run);
                thr.start();

      /* Countdown the parking time */
//                double hours = Double.parseDouble(noPicker);
//                double noHours = hours - 0.5;
//                double millis = (noHours)*60000;
//                long timeMillis = (long)millis;

                final CounterClass timer = new CounterClass(10000, 1000);
                timer.start();

                Toast.makeText(getApplicationContext(), plateNumber + " car parking is validated.", Toast.LENGTH_LONG).show();

                alert1.dismiss();
            }
        });



        // If you would like to resume scanning, call this method below:
        // mScannerView.resumeCameraPreview(this);



    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this activity?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }



    /*countdown the time*/
    //    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    //    @SuppressLint("NewApi")
    public class CounterClass extends CountDownTimer {

        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        //        @SuppressLint("NewApi")
//        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void onTick(long millisUntilFinished) {

            long millis = millisUntilFinished;
            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            System.out.println(hms);
//            txtVNoti.setText(hms);
        }

        @Override
//        @SuppressLint("NewApi")
        public void onFinish() {

            // Alert Notification once countdown end.
            PendingIntent intent = PendingIntent.getActivity(QrActivity.this, 100, new Intent(QrActivity.this, MoreInfoNotification.class), 0);
            NotificationCompat.Builder nb =   new NotificationCompat.Builder(ctx);
            nb.setSmallIcon(R.drawable.ic_launcher);
            nb.setContentTitle("Alert!!!");
            nb.setContentText("less than 30 minutes.");
            nb.setContentIntent(intent);
            nb.setAutoCancel(true);
            nb.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            long[] vibrate = {100,200,300,400};
            nb.setVibrate(vibrate);

            Notification notification = nb.build();
            NotificationManager NM = (NotificationManager)
                    ctx.getSystemService(NOTIFICATION_SERVICE);
            NM.notify(0, notification);
            finish();

        }

    }
}
