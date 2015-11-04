package com.example.xuewen.carpark;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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


        Button pay = (Button) findViewById(R.id.btnPaypal);
        pay.setOnClickListener(this);

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);


//        NTPUDPClient timeClient = new NTPUDPClient();
//        InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
//        TimeInfo timeInfo = timeClient.getTime(inetAddress);
//        long returnTime = timeInfo.getReturnTime();
//        Date time = new Date(returnTime);
//        System.out.println("Time from " + TIME_SERVER + ": " + time);


          /* display start time */
//        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        dt = new Date();
        String strDateFormat = "HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        txtVStartTime.setText(sdf.format(dt));


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
                long newTime = dt.getTime() + (increment)*3600000;
                Date newData = new Date(newTime);
                String strDateFormat = "HH:mm:ss";
                SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                txtVEndTime.setText(sdf.format(newData));

                float total = rate * increment;
                txtVTotal.setText(String.valueOf(total));

            }
        });

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
//                params.add(new BasicNameValuePair("email", "kuhn96@gmail.com"));
//                params.add(new BasicNameValuePair("password", "Kuhn@@@@"));
                String strURL = "http://172.27.35.1/parkingDB/showParking.php";
//                String strURL = "http://pmot-web.192.168.1.13.xip.io/api/v1/auth/login";
                /*JSONParser objJSONParser = new JSONParser();*/
                final JSONObject jsonObj =
                        makeHttpRequest(strURL, "POST", params);

                new Thread() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String strFromPHP = null;
                                strFromPHP = jsonObj.optString("rate");
                                rate = Float.parseFloat(strFromPHP);

                                txtVRate.setText(strFromPHP);
                            }
                        });
                    }
                }.start();
            }
        };
        Thread thr = new Thread(run);
        thr.start();

    }


    public JSONObject makeHttpRequest(String url, final String method,
            final List<NameValuePair> params)
    {
        InputStream is = null;
        String json = "";
        JSONObject jObj = null;

        // Making HTTP request
        try {

            // check for request method
            if(method == "POST"){
                // request method is POST
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                url = URLDecoder.decode(url);
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            }else if(method == "GET"){
                // request method is GET
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(params,
                        "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            //Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            try {
                JSONArray jsnArr = new JSONArray(json);
                jObj = jsnArr.getJSONObject(0);
            }catch(Exception ex)
            {
            // Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
        }

        // return JSON String
        return jObj;
    }



    public void testing(View view){
        Toast.makeText(getApplicationContext(), "testing", Toast.LENGTH_SHORT).show();
        TextView textView = (TextView)findViewById(R.id.txtVTime);
        textView.setText("hi");
    }

    public void onClickPayPal(View v) {

    }

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

                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("selectFN","fnInsert"));
                        params.add(new BasicNameValuePair("plate_num",edTxtPlateNo.getText().toString().trim()));
                        params.add(new BasicNameValuePair("parking_time",txtVEndTime.getText().toString()));
                        params.add(new BasicNameValuePair("parking_duration",noPicker.getValue() + ""));
                        params.add(new BasicNameValuePair("parking_amount",txtVTotal.getText().toString()));

                        String strURL = "http://172.27.35.1/parkingDB/showParking.php";

                /*JSONParser objJSONParser = new JSONParser();*/
                        final JSONObject jsonObj =
                                makeHttpRequest(strURL, "POST", params);

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

                           Runnable run = new Runnable() {
                               @Override
                               public void run() {
                                   List<NameValuePair> params = new ArrayList<NameValuePair>();
                                   params.add(new BasicNameValuePair("selectFN","fnUpdatePayment"));
                                   params.add(new BasicNameValuePair("plate_num",edTxtPlateNo.getText().toString().trim()));
                                   params.add(new BasicNameValuePair("paymentStat",objPaymentStatus));
                                   String strURL = "http://172.27.35.1/parkingDB/showParking.php";

                /*JSONParser objJSONParser = new JSONParser();*/
                                   final JSONObject jsonObj =
                                           makeHttpRequest(strURL, "POST", params);

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
    }

    /* stop PayPalService */
    public void onDestroy(){
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }
}