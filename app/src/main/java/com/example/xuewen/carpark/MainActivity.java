package com.example.xuewen.carpark;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;

import android.app.PendingIntent;
import android.content.Context;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
//import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
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
import java.util.concurrent.TimeUnit;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

///////////////////////////////////////http://www.thaicreate.com/mobile/android-sqlite-autocompletetextview.html


@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    static InputStream is = null;
    NumberPicker noPickerMinute, noPickerHour = null;
    int totalMinute = 0, hminute, minute;
    Date dt = null;
    static float rate = 0;
    AutoCompleteTextView aCtxtVw;
    ArrayAdapter<String> adapter;
 //   EditText edTxtPlateNo = null;
    TextView txtVStartTime = null;
    TextView txtVEndTime = null;
    TextView txtVRate = null;
    TextView txtVTotal = null;
    TextView textViewDate = null;
    DateFormat formatter;
    //use for Notification part

    String strgDate, strTime, strMsg;

    QrSqlite dbQrCode;
    String strCode, strCredit;
//    String strTotalAmount;
    String totalPayment;

    String plateNum = null;
    String strFromPHPStatus;

    public static Context ctx;

//    String plateNumber = edTxtPlateNo.getText().toString().trim();

    Connection conn = new Connection();
    private static String strURL = "http://192.168.43.14" +
            "/parkingDB/showParking.php";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        //use for Notification part
        ctx=this;

    //    edTxtPlateNo = (EditText)findViewById(R.id.edTxtPlateNo);
        txtVStartTime = (TextView)findViewById(R.id.txtVStartTime);
        txtVEndTime = (TextView)findViewById(R.id.txtVEndTime);
        txtVRate = (TextView)findViewById(R.id.txtVPrice);
        txtVTotal = (TextView)findViewById(R.id.txtVTotalPrice);
        textViewDate = (TextView)findViewById(R.id.txtVCurrentDate);
//        strTotalAmount = txtVTotal.getText().toString();


        /*AutoCompleteTextView take suggested data from sqlite*/
        final QrSqlite plateSql = new QrSqlite(this);
        final String [] myData = plateSql.SelectAllData();
        aCtxtVw = (AutoCompleteTextView)findViewById(R.id.autoCTxtVw);

        if(myData != null){
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line, myData);
        }
        aCtxtVw.setAdapter(adapter);




        /* number picker (hour) to display end time and total parking payment amount*/
        noPickerHour = (NumberPicker)findViewById(R.id.noPkrHour);
        noPickerHour.setMaxValue(12);
        noPickerHour.setMinValue(0);
        noPickerHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                if (aCtxtVw.getText().toString().trim().length() == 0) {
                    Toast.makeText(MainActivity.this, "Please enter a plate number", Toast.LENGTH_SHORT).show();

                } else if (aCtxtVw.getText().toString().trim().length() != 0 && aCtxtVw.getText().toString().trim().length() > 2) {

                    String strHour = "" + noPickerHour.getValue();
                    hminute = Integer.parseInt(strHour) * 60;
                    totalMinute = hminute + minute;

                    long increment = new Long(totalMinute);
                    Date dtct = new Date();
                    // add duration to start time & change duration to second to get end time
                    long newTime = dt.getTime() + (increment) * 3600000 / 60;
                    Date newData = new Date(newTime);
                    String strDateFormat = "HH:mm:ss";
                    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                    txtVEndTime.setText(sdf.format(newData));
                    // add duration to start time & change duration to second to get end time

                     /* multiply the rate per hours with duration(hours) to get total parking fees */
                    float total = rate * (totalMinute) /60;
                    DecimalFormat df = new DecimalFormat("0.00");
                    totalPayment = df.format(total);
                    txtVTotal.setText(totalPayment);
                }

            }
        });

        /* number picker (minute) to display end time and total parking payment amount*/
        noPickerMinute = (NumberPicker)findViewById(R.id.noPkrMinute);
        noPickerMinute.setMaxValue(59);
        noPickerMinute.setMinValue(00);
//        noPicker.setWrapSelectorWheel(true);
        noPickerMinute.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                if (aCtxtVw.getText().toString().trim().length() == 0) {
                    Toast.makeText(MainActivity.this, "Please enter a plate number", Toast.LENGTH_SHORT).show();

                } else if (aCtxtVw.getText().toString().trim().length() != 0 && aCtxtVw.getText().toString().trim().length() > 2) {

//                    Toast.makeText(MainActivity.this, "not empty", Toast.LENGTH_SHORT).show();

                    String strMinute = "" + noPickerMinute.getValue();
                    minute = Integer.parseInt(strMinute);
                    totalMinute = hminute + minute;
//                    txtVTotalMinute.setText(String.valueOf(totalMinute));

                    long increment = new Long(totalMinute);
                    Date dtct = new Date();
                    // add duration to start time & change duration to second to get end time
                    long newTime = dt.getTime() + (increment)  * 3600000 / 60;
                    Date newData = new Date(newTime);
                    String strDateFormat = "HH:mm:ss";
                    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                    txtVEndTime.setText(sdf.format(newData));

                     /* multiply the rate per hours with duration(hours) to get total parking fees */
                    float total = rate * (totalMinute) /60;
                    DecimalFormat df = new DecimalFormat("0.00");
                    totalPayment = df.format(total);
                    txtVTotal.setText(totalPayment);

                }
            }
        });

        /* (Button Pay by PayPal) Validate the plate number textField is not empty */
        final Button pay = (Button) findViewById(R.id.btnPaypal);
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(aCtxtVw.getText().toString().trim().length() == 0 ){
                    Toast.makeText(MainActivity.this, "Please enter a plate number", Toast.LENGTH_SHORT).show();

                }else if(totalMinute == 0 ){
                    Toast.makeText(MainActivity.this, "Please select parking duration.", Toast.LENGTH_SHORT).show();

                }
                /*else if(aCtxtVw.getText().toString().trim().length() != 0){
                    long increment = Long.parseLong("1");
                    float total = rate * increment;
                    DecimalFormat df = new DecimalFormat("0.00");
                    totalPayment = df.format(total);
                    txtVTotal.setText(totalPayment);
                    startPaypal();

                }*/
                else if(aCtxtVw.getText().toString().trim().length() != 0 && aCtxtVw.getText().toString().trim().length() > 2) {
//                    Toast.makeText(MainActivity.this, "not empty", Toast.LENGTH_SHORT).show();
                    startPaypal();

                }
            }
        });

        /* (Button Pay with QR code) Validate the plate number textfield is not empty */
        final Button payQr = (Button) findViewById(R.id.btnQr);
        payQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(aCtxtVw.getText().toString().trim().length() == 0){
                    Toast.makeText(MainActivity.this, "Please enter a plate number.", Toast.LENGTH_SHORT).show();

                }else if(totalMinute == 0){
                    Toast.makeText(MainActivity.this, "Please select parking duration.", Toast.LENGTH_SHORT).show();

                }
                /*else if(aCtxtVw.getText().toString().trim().length() != 0){
                    long increment = Long.parseLong("1");
                    float total = rate * increment;
                    DecimalFormat df = new DecimalFormat("0.00");
                    totalPayment = df.format(total);
                    txtVTotal.setText(totalPayment);
                    startQrCode();

                }*/
                else if(aCtxtVw.getText().toString().trim().length() != 0 && aCtxtVw.getText().toString().trim().length() > 2) {
                    startQrCode();
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
        textViewDate = (TextView)findViewById(R.id.txtVCurrentDate);
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
                                long endTime = dt.getTime();
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

    } //end of onCreate



    private void startQrCode(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getSupportActionBar().getThemedContext());
        builder1.setMessage("Do you want to use existing QR code?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dbQrCode = new QrSqlite(getApplicationContext());

                        // Take existing QR code from sqlite databse to make payment
                        Runnable run1 = new Runnable() {
                            @Override
                            public void run() {
                                Cursor resultSet = dbQrCode.getReadableDatabase().rawQuery("Select * from qr;" , null);

                                if(resultSet.moveToFirst()){
                                    do{
                                        strCode = resultSet.getString(resultSet.getColumnIndex("qrCode"));
                                        strCredit = resultSet.getString(resultSet.getColumnIndex("qrCredit"));

                                    }while(resultSet.moveToNext());
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getSupportActionBar().getThemedContext());
                                        builder2.setTitle("Existing code     Balance ");
                                        builder2.setMessage(strCode + "              RM " + strCredit);
                                        builder2.setCancelable(true);
                                        double credit = Double.parseDouble(strCredit);
                                        if (credit == 0){
                                            builder2.setNegativeButton("The QR code credit amount is not enough.",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {

                                                        }
                                                    });
                                        }else
                                        builder2.setPositiveButton(
                                                "Pay",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {

                                                        // Make payment with existing code
                                                        Runnable runpay = new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                            /*Insert parking details*/
                                                                List<NameValuePair> paramInsert = new ArrayList<NameValuePair>();
                                                                paramInsert.add(new BasicNameValuePair("selectFN","fnInsert"));
                                                                paramInsert.add(new BasicNameValuePair("plate_num",aCtxtVw.getText().toString().trim()));
                                                                paramInsert.add(new BasicNameValuePair("parking_start_time",txtVStartTime.getText().toString()));
                                                                paramInsert.add(new BasicNameValuePair("parking_end_time",txtVEndTime.getText().toString()));
                                                                paramInsert.add(new BasicNameValuePair("parking_duration",totalMinute + ""));
                                                                paramInsert.add(new BasicNameValuePair("parking_amount",txtVTotal.getText().toString()));

                                                                final JSONObject jsonObjInsert =
                                                                        conn.makeHttpRequest(strURL, "POST", paramInsert);

                                                                            /*Update credit in QR code*/
                                                                List<NameValuePair> params = new ArrayList<NameValuePair>();
                                                                params.add(new BasicNameValuePair("selectFN", "fnUpdateQrPayment"));
                                                                params.add(new BasicNameValuePair("qrResult", strCode));
                                                                params.add(new BasicNameValuePair("total_amount", totalPayment));

                                                                final JSONObject jsonObj =
                                                                        conn.makeHttpRequest(strURL, "POST", params);

                                                                //get balance from database to update credit sqlite
                                                                String updatedBalance = jsonObj.optString("balance");

                                                                            /*update QR code credit in sqlite*/
                                                                String sqludt = "UPDATE qr set qrCredit = '" + updatedBalance + "' where qrCode = '" + strCode + "';";
                                                                dbQrCode.fnExecuteSql(sqludt, getApplicationContext());

                                                                /*insert plate number into */
                                                                plateNum = aCtxtVw.getText().toString();
                                                                String sqlPlate = "Insert into plate values( '" + plateNum + "');";
                                                                dbQrCode.fnExecuteSql(sqlPlate, getApplicationContext());

                                                                String qrcode = "QR CODE       RM";
                                                                String strQryHistory = "Insert into history values( '" +  textViewDate.getText().toString() + "', '" + txtVStartTime.getText().toString() + "', '" + qrcode + "', '" + totalPayment + "');";
                                                                dbQrCode.fnExecuteSql(strQryHistory, getApplicationContext());

                                                                            /*update payment status of parking info*/
                                                                List<NameValuePair> paramStatus = new ArrayList<NameValuePair>();
                                                                paramStatus.add(new BasicNameValuePair("selectFN","fnUpdatePayment"));
                                                                paramStatus.add(new BasicNameValuePair("plate_num",aCtxtVw.getText().toString().trim()));
                                                                paramStatus.add(new BasicNameValuePair("paymentStat","approved"));

                                                                final JSONObject jsonObjStatus =
                                                                        conn.makeHttpRequest(strURL, "POST", paramStatus);

                                                            }
                                                        };
                                                        Thread thrpay = new Thread(runpay);
                                                        thrpay.start();

                                                        /* Countdown the parking time */
                                                        double hours = totalMinute;
                                                        double noHours = hours - 0.5;
                                                        double millis = (noHours)*60000;
                                                        long timeMillis = (long)millis;

                                                        final CounterClass timer = new CounterClass(10000, 1000);
                                                        timer.start();

                                                        Toast.makeText(getApplicationContext(), aCtxtVw.getText().toString().trim() + " car parking is validated.", Toast.LENGTH_LONG).show();

                                                    }
                                                }
                                        );
                                        AlertDialog alert12 = builder2.create();
                                        alert12.show();
                                    }

                                });
                            }
                        };
                        Thread thr = new Thread(run1);
                        thr.start();

                        dialog.cancel();
                    }
                });

        //Alertdialog for "No" - start scanning new QR code
        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        AlertDialog.Builder builderCode = new AlertDialog.Builder(getSupportActionBar().getThemedContext());
                        builderCode.setTitle("Choose method to read QR code");
                        builderCode.setMessage("Key in = by Keyboard;                                      Scan = by Camera");
                        builderCode.setCancelable(true);

                        //Alertdialog for "No" - start scanning new QR code
                        builderCode.setPositiveButton(
                                "Scan",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        //                    Toast.makeText(MainActivity.this, "not empty", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this, QrActivity.class);
                                        intent.putExtra("varTotalAmount", totalPayment);
                                        intent.putExtra("plateNumber", aCtxtVw.getText().toString().trim());
                                        intent.putExtra("date", textViewDate.getText().toString());
                                        intent.putExtra("startTime", txtVStartTime.getText().toString());
                                        intent.putExtra("endTime", txtVEndTime.getText().toString());
                                        intent.putExtra("duration", totalMinute + "");
                                        intent.putExtra("total", txtVTotal.getText().toString());
                                        startActivity(intent);

                                        dialog.cancel();
                                    }
                                });
//                        AlertDialog alertCamera = builderCode.create();
//                        alertCamera.show();

//                        dialog.cancel();


                        //Alertdialog for "No" - start scanning new QR code
                        builderCode.setNegativeButton(
                                "Key in",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        Intent intent = new Intent(MainActivity.this, QrInsertCode.class);
                                        intent.putExtra("varTotalAmount", totalPayment);
                                        intent.putExtra("plateNumber", aCtxtVw.getText().toString().trim());
                                        intent.putExtra("date", textViewDate.getText().toString());
                                        intent.putExtra("startTime", txtVStartTime.getText().toString());
                                        intent.putExtra("endTime", txtVEndTime.getText().toString());
                                        intent.putExtra("duration", totalMinute + "");
                                        intent.putExtra("total", txtVTotal.getText().toString());

                                        dialog.cancel();
                                        startActivity(intent);


                                    }
                                });

                        AlertDialog alertNum = builderCode.create();
                        alertNum.show();


//                        dialog.cancel();
                    }
                });


        AlertDialog alert11 = builder1.create();
        alert11.show();

    }

    public void fnDisplayToastMsg(String strText){
        Toast tst = Toast.makeText(getApplicationContext(), strText, Toast.LENGTH_LONG);
        tst.show();
    }


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
                "Green Electronic Parking Payment", PayPalPayment.PAYMENT_INTENT_SALE);
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
                params.add(new BasicNameValuePair("plate_num",aCtxtVw.getText().toString().trim()));
                params.add(new BasicNameValuePair("parking_start_time",txtVStartTime.getText().toString()));
                params.add(new BasicNameValuePair("parking_end_time",txtVEndTime.getText().toString()));
                params.add(new BasicNameValuePair("parking_duration",totalMinute + ""));
                params.add(new BasicNameValuePair("parking_amount",txtVTotal.getText().toString()));

            /*JSONParser objJSONParser = new JSONParser();*/
                final JSONObject jsonObj =
                        conn.makeHttpRequest(strURL, "POST", params);


            }
        };
        Thread thr = new Thread(run);
        thr.start();
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
                           System.out.println("payment id: -==" + paymentID);

//                           Toast.makeText(getApplicationContext(), paymentID, Toast.LENGTH_LONG).show();
                           Toast.makeText(getApplicationContext(), aCtxtVw.getText().toString().trim() + " car parking is validated.", Toast.LENGTH_LONG).show();

//                           Toast.makeText(getApplicationContext(), edTxtPlateNo.getText().toString().trim() + " parking is valided.", Toast.LENGTH_LONG).show();

                           dbQrCode = new QrSqlite(getApplicationContext());

                           /* update payment status to valid after payment is done successfully */
                           Runnable run = new Runnable() {
                               @Override
                               public void run() {
                                   List<NameValuePair> params = new ArrayList<NameValuePair>();
                                   params.add(new BasicNameValuePair("selectFN","fnUpdatePayment"));
                                   params.add(new BasicNameValuePair("plate_num",aCtxtVw.getText().toString().trim()));
                                   params.add(new BasicNameValuePair("paymentStat",objPaymentStatus));

                                    /*JSONParser objJSONParser = new JSONParser();*/
                                   final JSONObject jsonObj =
                                           conn.makeHttpRequest(strURL, "POST", params);

                                   runOnUiThread(new Runnable() {
                                       @Override
                                       public void run() {
                                           if(objPaymentStatus.equalsIgnoreCase("approved")){

                                               try {
                                                   final CounterClass timer = new CounterClass(10000, 1000);
                                                   timer.start();
                                               }catch(Exception e){
                                                   System.out.println("hereee");
                                               }
                                           }

                                       }
                                   });




                                   String paypal = "PayPal        RM";

                                   String strQryHistory = "Insert into history values( '" +  textViewDate.getText().toString() + "', '" + txtVStartTime.getText().toString() + "', '" + paypal + "', '" + totalPayment + "');";
                                   dbQrCode.fnExecuteSql(strQryHistory, getApplicationContext());

                                   plateNum = aCtxtVw.getText().toString().trim();
                                   dbQrCode.InsertData(plateNum);
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

        /* Countdown the parking time */
        double hours = totalMinute;
        double noHours = hours - 0.5;
        double millis = (noHours)*60000;
        long timeMillis = (long)millis;

        Runnable checkStatus = new Runnable() {
            @Override
            public void run() {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("selectFN","fnGetParkStatus"));
                params.add(new BasicNameValuePair("plate_num", aCtxtVw.getText().toString().trim()));
                params.add(new BasicNameValuePair("parking_end_time", txtVEndTime.getText().toString()));

                final JSONObject jsonObj =
                        conn.makeHttpRequest(strURL, "POST", params);


                strFromPHPStatus = jsonObj.optString("status");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(strFromPHPStatus.equalsIgnoreCase("approved")){

                            try {
                                CounterClass timer = new CounterClass(10000, 1000);
                                timer.start();
                            }catch(Exception e){
                                System.out.println("hereee");
                            }
                        }
                    }
                });



            }
        };
        Thread thr = new Thread(checkStatus);
        thr.start();

    }

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

    @Override
    public void handleResult(Result result) {
        Log.e("handler", result.getText()); // Prints scan results
        Log.e("handler", result.getBarcodeFormat().toString()); // Prints the scan format (qrcode)

        // show the scanner result into dialog box.
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Scan Result");
        builder.setMessage(result.getText());
        AlertDialog alert1 = builder.create();
        alert1.show();

        // If you would like to resume scanning, call this method below:
        // mScannerView.resumeCameraPreview(this);

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

            // Alert Notification once countdown end.
            PendingIntent intent = PendingIntent.getActivity(MainActivity.this, 100, new Intent(MainActivity.this, MoreInfoNotification.class), 0);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.QrHis) {
            fnQrHistory(this.getCurrentFocus());

            return true;
        }
//        else if (id == R.id.PlateNoList) {
//            fnPlateNo(this.getCurrentFocus());
//
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private void fnQrHistory(View currentFocus){
        Intent intent = new Intent(this, QrHistory.class);
        startActivityForResult(intent, 0);
    }

    private void fnPlateNo(View currentFocus){
        Intent intent = new Intent(this, PlateNumberList.class);
        startActivityForResult(intent, 0);
    }

}