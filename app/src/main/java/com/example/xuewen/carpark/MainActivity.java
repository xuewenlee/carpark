package com.example.xuewen.carpark;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;

import android.content.Intent;
import android.media.RingtoneManager;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.renderscript.RenderScript;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static android.widget.Toast.*;


@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")

public class MainActivity extends AppCompatActivity  {

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
    int notificationID = 1;
    int notificationId = 1;
    private Context context = this;
    private Button bt;
    public static Context ctx;

//    implements View.OnClickListener

//    private ProgressDialog progressDialog;
//    private MessageHandler messageHandler;


//    String endTime = txtVEndTime.getText().toString();

    Connection conn = new Connection();
    private static String strURL = "http://192.168.1.8/parkingDB/showParking.php";

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

        ctx=this;
        edTxtPlateNo = (EditText)findViewById(R.id.edTxtPlateNo);
        txtVStartTime = (TextView)findViewById(R.id.txtVStartTime);
        txtVEndTime = (TextView)findViewById(R.id.txtVEndTime);
        txtVRate = (TextView)findViewById(R.id.txtVPrice);
        txtVTotal = (TextView)findViewById(R.id.txtVTotalPrice);
        txtVNoti = (TextView)findViewById(R.id.txtVNoti);
        txtVNoti.setText("00:03:00");
        bt= (Button) findViewById(R.id.buttonTest);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.more_info_notific);
                dialog.setTitle("Alert!!!");
                Button btOK = (Button) dialog.findViewById(R.id.button1);
                btOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });


//        /*test2 countdown time*/
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Counting...");
//        progressDialog.setMessage("Please wait...");
//        progressDialog.setCancelable(true);
//
//        messageHandler = new MessageHandler();

//            public void onTick(long millisUntilFinished) {
//                txtVNoti.setText("seconds remaining: " + millisUntilFinished / 1000);
//            }
//
//            public void onFinish() {
//                txtVNoti.setText("done!");
//            }
//        }.start();



        final Button pay = (Button) findViewById(R.id.btnPaypal);
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String strPlateNumber = edTxtPlateNo.getText().toString();
                if(edTxtPlateNo.getText().toString().trim().length() == 0){
                    Toast.makeText(MainActivity.this, "You did not enter a plate number", Toast.LENGTH_SHORT).show();
//                    return;
                }else if(edTxtPlateNo.getText().toString().trim().length() != 0) {
//                    pay.setOnClickListener(this);
                    Toast.makeText(MainActivity.this, "not empty", Toast.LENGTH_SHORT).show();
                    startPaypal();

                }
            }
        });


        /* Start PayPalService when activity is created */
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);


        /* Display Today Date*/
        Date date = new Date();
        String strDate = "dd-MM-yyyy";
        SimpleDateFormat simpleDate = new SimpleDateFormat(strDate);
        TextView textViewDate = (TextView)findViewById(R.id.txtVCurrentDate);
        textViewDate.setText(simpleDate.format(date));

        /* Get parking rate and start time from server */
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

                                // get parking rate per hour
                                String strFromPHP = null;
                                strFromPHP = jsonObj.optString("rate");
                                rate = Float.parseFloat(strFromPHP);
                                txtVRate.setText(strFromPHP);

                                // get parking start time
                                String strFromPHP2 = null;
                                strFromPHP2 = jsonObjTime.optString("time");
                                formatter = new SimpleDateFormat("HH:mm:ss");
                                try {
                                    dt = formatter.parse(strFromPHP2);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                txtVStartTime.setText(jsonObjTime.optString("time").trim());

                                // set end time when open the apps
                                long endTime = dt.getTime() + 3600000;
                                Date newData = new Date(endTime);
                                String strDateFormat = "HH:mm:ss";
                                SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                                txtVEndTime.setText(sdf.format(newData));
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

                if(edTxtPlateNo.getText().toString().trim().length() == 0){
                    Toast.makeText(MainActivity.this, "You did not enter a plate number", Toast.LENGTH_SHORT).show();
//                    return;
                }else if(edTxtPlateNo.getText().toString().trim().length() != 0) {
//                    pay.setOnClickListener(this);
                    Toast.makeText(MainActivity.this, "not empty", Toast.LENGTH_SHORT).show();

                    String number = "" + noPicker.getValue();
                    long increment = Long.parseLong(number);
                    Date dtct = new Date();
                    // add duration to start time & change duration to second
                    long newTime = dt.getTime() + (increment) * 3600000;
                    Date newData = new Date(newTime);
                    String strDateFormat = "HH:mm:ss";
                    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                    txtVEndTime.setText(sdf.format(newData));

                /* multiply the rate per hours with duration(hours) to get total parking fees */
                    float total = rate * increment;
                    DecimalFormat df = new DecimalFormat("0.00");
                    String totalPayment = df.format(total);
                    txtVTotal.setText(totalPayment);
                }


            }
        });

    } //end of onCreate

    public void fnCallNotif()
    {

        CounterClass timerTest = new CounterClass(3000, 1000);
        timerTest.start();

    }

    /* Create the PayPalPayment object and launch the PaymentActivity intent, for example, when a button is pressed */
    private void startPaypal() {

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

            /*JSONParser objJSONParser = new JSONParser();*/
                final JSONObject jsonObj =
                        conn.makeHttpRequest(strURL, "POST", params);

            }
        };
        Thread thr = new Thread(run);
        thr.start();
    }


//    public void showNotification(View view) {
//        NotificationCompat.Builder notificationBuilder = new
//                NotificationCompat.Builder(this)
//                .setContentTitle("Message")
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
//        notificationManager.notify(notifID, notificationBuilder.build());
//
//        isNotificActive = true;
//
//    }



    public void setAlarm(View view){

    }

    public void testing(View view){
        Toast.makeText(getApplicationContext(), "testing", Toast.LENGTH_SHORT).show();
        TextView textView = (TextView)findViewById(R.id.txtVTime);
        textView.setText("hi");
    }

    public void onClickPayPal(View v) {

    }


    /* Once the transaction done, Paypal send back response (approved of payment status) */
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


//        progressDialog.show();
//
//        Thread thread = new Thread(new Timer());
//        thread.start();

        /* Countdown the parking time */
        double hours = noPicker.getValue();
        double noHours = hours - 0.5;
        double millis = (noHours)*60000;
        long timeMillis = (long)millis;

        final CounterClass timer = new CounterClass(3000, 1000);
        timer.start();

    }

//    public void onClickTest(View view) {
//    }
//
//    private class Timer implements Runnable {
//        int number = noPicker.getValue();
//        @Override
//        public void run() {
//            for(int i = number; i>=0; i--){
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                Bundle bundle = new Bundle();
//                bundle.putInt("current count", i);
//
//                Message message = new Message();
//                message.setData(bundle);
//
//                messageHandler.sendMessage(message);
//
//                if(number == 0){
//                    doNotify();
//                }
//            }
//            progressDialog.dismiss();
//        }
//    }
//
//    private class MessageHandler extends Handler{
//
//        @Override
//        public void handleMessage (Message message){
//
//            int currentCount = message.getData().getInt("current count");
//            progressDialog.setMessage("Please wait in .." + currentCount);
//
//            if(currentCount == 0){
//                doNotify();
//            }
//        }
//    }
//
//    public void doNotify(){
//        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        PendingIntent intent = PendingIntent.getActivity(this, 100, new Intent(this, MoreInfoNotification.class), 0);
//
//        NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
//        nb.setSound(sound);
//        nb.setContentTitle("Alert!!!");
//        nb.setContentText("Parking time less than half hours.");
//        nb.setContentIntent(intent);
//
//        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        nm.notify(100, nb.build());
//    }



    /* stop PayPalService */
    public void onDestroy(){
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*countdown the time*/
//    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
//    @SuppressLint("NewApi")
    public class CounterClass extends CountDownTimer{

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

//            final Dialog dialog = new Dialog(context);
//            dialog.setContentView(R.layout.more_info_notific);
//            dialog.setTitle("Alert!!!");
//            Button btOK = (Button) dialog.findViewById(R.id.button1);
//            btOK.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog.dismiss();
//                }
//            });


//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this);
//            mBuilder.setContentTitle("Notification Alert, Click Me!");
//            mBuilder.setContentText("Hi, This is Android Notification Detail!");
//
//            Intent resultIntent = new Intent(MainActivity.this, MoreInfoNotification.class);
//            TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
//            stackBuilder.addParentStack(MoreInfoNotification.class);
//
//            // Adds the Intent that starts the Activity to the top of the stack
//            stackBuilder.addNextIntent(resultIntent);
//            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
//            mBuilder.setContentIntent(resultPendingIntent);
//
//            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//            // notificationID allows you to update the notification later on.
//            mNotificationManager.notify(notificationID, mBuilder.build());


//            Long alertTime = new GregorianCalendar().getTimeInMillis()+5*1000;

//            int alertTime = 1*1000;
//            Intent aletIntent = new Intent(MainActivity.this, AlertReceiver.class);
//            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//            alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
//                    PendingIntent.getBroadcast(MainActivity.this, 1, aletIntent,
//                            PendingIntent.FLAG_UPDATE_CURRENT));
            txtVNoti.setText("Completed.");


////            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            PendingIntent intent = PendingIntent.getActivity(MainActivity.this, 100, new Intent(MainActivity.this, MoreInfoNotification.class), 0);
//
//            NotificationCompat.Builder nb = new NotificationCompat.Builder(getApplicationContext());
//            //nb.setSound(sound);
//            nb.setTicker("Times up");
//            nb.setContentTitle("Alert!!!");
//            nb.setContentText("Parking time less than half hours.");
//            nb.setContentIntent(intent);
//
//            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            nm.notify(0, nb.build());

            PendingIntent intent = PendingIntent.getActivity(MainActivity.this, 100, new Intent(MainActivity.this, MoreInfoNotification.class), 0);
            NotificationCompat.Builder nb =   new NotificationCompat.Builder(ctx);
            nb.setSmallIcon(R.drawable.ic_launcher);
            nb.setContentTitle("Alert!!!");
            nb.setContentText("less than 30 minutes.");
            nb.setContentIntent(intent);
            nb.setAutoCancel(true);

            Notification notification = nb.build();
            NotificationManager NM = (NotificationManager)
                    ctx.getSystemService(NOTIFICATION_SERVICE);
            NM.notify(0, notification);


/* IJAH coding*/
//            Intent intentRenew = new Intent(getApplicationContext(),MoreInfoNotification.class);
            // use System.currentTimeMillis() to have a unique ID for the pending intent
//            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intentRenew, 0);
//
//            Notification builder = new Notification.Builder(MainActivity.this)
//                    .setContentTitle("ALert")
//                    .setContentText("time left less than 30 minutes.")
//                    .setContentIntent(pIntent).build();


////            builder.setTicker("Master Lock - Session End");
//            builder.setContentTitle("Master Lock");
//            builder.setContentText("The session is end");
////            builder.setSmallIcon(R.mipmap-xhdpi.ic_launcher);
//            builder.setContentIntent(pIntent);
////            builder.addAction(0, "Continue", pIntent);
////            builder.addAction(0, "Cancel", pIntent).build();
////            builder.setAutoCancel(true);
////            objSound.soundVibrate();
//            long[] vibrate = {100,200,300,400};
//            builder.setVibrate(vibrate);

//            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            nm.notify(0, nm.build);

        }
    }
}