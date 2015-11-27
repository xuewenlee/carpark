package com.example.xuewen.carpark;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static InputStream is = null;
    NumberPicker noPicker = null;
    Date dt = null;
    static float rate = 0;
    EditText edTxtPlateNo = null;
    TextView txtVStartTime = null;
    TextView txtVEndTime = null;
    TextView txtVRate = null;
    TextView txtVTotal = null;
    TextView txtVNoti = null;
    static Long endTime = null;
    DateFormat formatter;
    NotificationManager notificationManager;
    boolean isNotificActive = false;
    int notifID = 33;


//    String endTime = txtVEndTime.getText().toString();

    Connection conn = new Connection();
    private static String strURL = "http://172.27.35.1/parkingDB/showParking.php";

    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
    // note that these credentials will differ between live & sandbox
// environments.
    private static final String CONFIG_CLIENT_ID = "AWUgPpSHbB9wnK2Oalw3KBEZOo2eb8B0alr6mnFkDk7MqnyIK5PHGSYTfHepJox1fq32m6LbJHdZ3HLV";
    private static final int REQUEST_CODE_PAYMENT = 1;

    private static PayPalConfiguration config = new PayPalConfiguration()
        .environment(CONFIG_ENVIRONMENT)
        .clientId(CONFIG_CLIENT_ID)
        // the following are only used in PayPalFuturePaymentActivity.
        .merchantName("Hipster Store")
        .merchantPrivacyPolicyUri(
            Uri.parse("https://www.example.com/privacy"))
        .merchantUserAgreementUri(
            Uri.parse("https://www.example.com/legal"));


//    PayPalPaymentthingToBuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        edTxtPlateNo = (EditText)findViewById(R.id.edTxtPlateNo);
        txtVStartTime = (TextView)findViewById(R.id.txtVStartTime);
        txtVEndTime = (TextView)findViewById(R.id.txtVEndTime);
        txtVRate = (TextView)findViewById(R.id.txtVPrice);
        txtVTotal = (TextView)findViewById(R.id.txtVTotalPrice);
        txtVNoti = (TextView)findViewById(R.id.txtVNoti);
        txtVNoti.setText("00:03:00");
//        new CountDownTimer(30000, 1000) {
//
//            public void onTick(long millisUntilFinished) {
//                txtVNoti.setText("seconds remaining: " + millisUntilFinished / 1000);
//            }
//
//            public void onFinish() {
//                txtVNoti.setText("done!");
//            }
//        }.start();

        Button pay = (Button) findViewById(R.id.btnPaypal);
        pay.setOnClickListener(this);

        /* Start PayPalService when activity is created */
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);



          /* display start time */
//        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
//        dt = new Date();
//        String strDateFormat = "HH:mm:ss";
//        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
//        txtVStartTime.setText(sdf.format(dt));



        /* Display Today Date*/
        Date date = new Date();
        String strDate = "dd-MM-yyyy";
        SimpleDateFormat simpleDate = new SimpleDateFormat(strDate);
        TextView textViewDate = (TextView)findViewById(R.id.txtVCurrentDate);
        textViewDate.setText(simpleDate.format(date));


        Runnable run = new Runnable() {
            @Override
            public void run() {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("selectFN","fnGetParkRate"));

                /*JSONParser objJSONParser = new JSONParser();*/
                final JSONObject jsonObj =
                        conn.makeHttpRequest(strURL, "POST", params);

                List<NameValuePair> paramsTime = new ArrayList<NameValuePair>();
                paramsTime.add(new BasicNameValuePair("selectFN","fnServerTime"));

                /*JSONParser objJSONParser = new JSONParser();*/
                final JSONObject jsonObjTime =
                        conn.makeHttpRequest(strURL, "POST", paramsTime);

                new Thread() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                String strFromPHP = null;
                                strFromPHP = jsonObj.optString("rate");
                                rate = Float.parseFloat(strFromPHP);
                                txtVRate.setText(strFromPHP);

                                String strFromPHP2 = null;
                                strFromPHP2 = jsonObjTime.optString("time");
                                formatter = new SimpleDateFormat("HH:mm:ss");
                                try {
                                    dt = formatter.parse(strFromPHP2);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                txtVStartTime.setText(jsonObjTime.optString("time").trim());

                            }
                        });
                    }
                }.start();
            }
        };
        Thread thr = new Thread(run);
        thr.start();


        /* number picker for time -hour and display end time*/
        noPicker = (NumberPicker)findViewById(R.id.numberPicker);
        noPicker.setMaxValue(9);
        noPicker.setMinValue(1);
        noPicker.setWrapSelectorWheel(true);
        noPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                String number = "" + noPicker.getValue();
                long increment = Long.parseLong(number);
                Date dtct = new Date();
                // change duration to second
                long newTime = dt.getTime() + (increment) * 3600000;
//                String addedEndTime = Long.toString(newTime);
                Date newData = new Date(newTime);
                String strDateFormat = "HH:mm:ss";
                SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                txtVEndTime.setText(sdf.format(newData));
//                txtVEndTime.setText(addedEndTime);

                /* multiply the rate per hours with duration(hours) to get total parking fees */
                float total = rate * increment;
                DecimalFormat df = new DecimalFormat("0.00");
                String totalPayment = df.format(total);
//                txtVTotal.setText(String.valueOf(total));
                txtVTotal.setText(totalPayment);

            }
        });

    }

//    public void showNotification(View view) {
//        NotificationCompat.Builder notificationBuilder = new
//                NotificationCompat.Builder(this)
//                .setContentTitle("Alert")
//                .setContentText("New Message")
//                .setTicker("Alert New Message");
////                .setSmallIcon(R.drawable.name);
//
//        Intent moreInfoIntent = new Intent(this, MoreInfoNotification.class);
//
//        TaskStackBuilder tStackBuilder = TaskStackBuilder.create(this);
//
//        tStackBuilder.addParentStack(MoreInfoNotification.class);
//
//        tStackBuilder.addNextIntent(moreInfoIntent);
//
//        PendingIntent pendingIntent = tStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        notificationBuilder.setContentIntent(pendingIntent);
//
//        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.Notify(notifID, notificationBuilder.build());
//
//        isNotificActive = true;
//
//    }

    /*countdown the time*/
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("NewApi")
    public class CounterClass extends CountDownTimer{

        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @SuppressLint("NewApi")
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
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
        public void onFinish() {
            txtVNoti.setText("Completed.");

        }
    }


    public void setAlarm(View view){
//        Long alertTime = new GregorianCalendar().getTimeInMillis()+5*1000;
//
////        Intent aletIntent = new Intent(this, AlertReceiver.class);
//
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//
//        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
//                PendingIntent.getBroadcast(this, 1, aletIntent,
//                        PendingIntent.FLAG_IMMUTABLE));
    }

    public void testing(View view){
        Toast.makeText(getApplicationContext(), "testing", Toast.LENGTH_SHORT).show();
        TextView textView = (TextView)findViewById(R.id.txtVTime);
        textView.setText("hi");
    }

    public void onClickPayPal(View v) {

    }

    /* Create the PayPalPayment object and launch the PaymentActivity intent, for example, when a button is pressed */
    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.btnPaypal:

                // PAYMENT_INTENT_SALE will cause the payment to complete immediately.
                // Change PAYMENT_INTENT_SALE to
                //   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
                //   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
                //     later via calls from your server.

                PayPalPayment thingToBuy= new PayPalPayment(new BigDecimal(txtVTotal.getText().toString()), "MYR",
                        "HeadSet", PayPalPayment.PAYMENT_INTENT_SALE);
                Intent intent = new Intent(MainActivity.this,
                        PaymentActivity.class);
                // send the same configuration for restart resiliency
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
                startActivityForResult(intent, REQUEST_CODE_PAYMENT);

                /* send details to server */
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("selectFN","fnInsert"));
                        params.add(new BasicNameValuePair("plate_num",edTxtPlateNo.getText().toString().trim()));
                        params.add(new BasicNameValuePair("parking_time",txtVEndTime.getText().toString()));
                        params.add(new BasicNameValuePair("parking_duration",noPicker.getValue() + ""));
                        params.add(new BasicNameValuePair("parking_amount",txtVTotal.getText().toString()));

//                        String strURL = "http://192.168.43.132/parkingDB/showParking.php";

                /*JSONParser objJSONParser = new JSONParser();*/
                        final JSONObject jsonObj =
                                conn.makeHttpRequest(strURL, "POST", params);

                    }
                };
                Thread thr = new Thread(run);
                thr.start();


                break;


        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_CODE_PAYMENT){
            if(resultCode == Activity.RESULT_OK){
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                   if(confirm != null){
                       try{
                           System.out.println("Responses" + confirm);

                           // TODO: send 'confirm' to your server for verification.

                           Log.i("Paypal Payments", confirm.toJSONObject().toString());

                           JSONObject obj= new JSONObject(confirm.toJSONObject().toString());

                           String paymentID = obj.getJSONObject("response").getString("id");
                           final String objPaymentStatus = obj.getJSONObject("response").getString("state");
                           System.out.println("payment id: -==" +paymentID);

                           Toast.makeText(getApplicationContext(), paymentID, Toast.LENGTH_LONG).show();

                           /* update payment status to valid after payment is done successfully */
                           Runnable run = new Runnable() {
                               @Override
                               public void run() {
                                   List<NameValuePair> params = new ArrayList<NameValuePair>();
                                   params.add(new BasicNameValuePair("selectFN","fnUpdatePayment"));
                                   params.add(new BasicNameValuePair("plate_num",edTxtPlateNo.getText().toString().trim()));
                                   params.add(new BasicNameValuePair("paymentStat",objPaymentStatus));
//                                   String strURL = "http://192.168.43.132/parkingDB/showParking.php";

                                    /*JSONParser objJSONParser = new JSONParser();*/
                                   final JSONObject jsonObj =
                                           conn.makeHttpRequest(strURL, "POST", params);

                               }
                           };
                           Thread thr = new Thread(run);
                           thr.start();


                       }catch(JSONException e){
                           Log.e("Payment demo", "failure occured!", e);
                       }
                   }
                    else if(requestCode == Activity.RESULT_CANCELED){
                       Log.i("Payment demo", "The user cancelled");

                   }
                    else if(requestCode == PaymentActivity.RESULT_EXTRAS_INVALID){
                       Log.i("Payment demo", "Invalid payment Submitted");

                    }
            }
        }

        /*countdown*/
        double hours = noPicker.getValue();
        double noHours = hours - 0.5;
        double millis = (noHours)*60000;
        long timeMillis = (long)millis;

        final CounterClass timer = new CounterClass(timeMillis, 1000);
        timer.start();

    }

    /* stop PayPalService */
    public void onDestroy(){
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }
}