package com.example.xuewen.carpark;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.time.TimeTCPClient;



public class MainActivity extends AppCompatActivity {

    static InputStream is = null;
    NumberPicker noPicker = null;
    Date dt = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

//
//        NTPUDPClient timeClient = new NTPUDPClient();
//        InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
//        TimeInfo timeInfo = timeClient.getTime(inetAddress);
//        long returnTime = timeInfo.getReturnTime();
//        Date time = new Date(returnTime);
//        System.out.println("Time from " + TIME_SERVER + ": " + time);


          /* time-hour */
//        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        TextView textViewct = (TextView)findViewById(R.id.txtVwStartTime);
        dt = new Date();
        int hours = dt.getHours();
        int minutes = dt.getMinutes();
        int seconds = dt.getSeconds();
        String curTime = hours + ":" + minutes + ":" + seconds;
        // textView is the TextView view that should display it
        textViewct.setText(curTime);


        /* number picker for time -hour*/
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
                int nhours = newData.getHours();
                int nminutes = newData.getMinutes();
                int nseconds = newData.getSeconds();
                String newTimeString = nhours + ":" + nminutes + ":" + nseconds;
                TextView endtextView = (TextView)findViewById(R.id.txtVwEndTime);
                endtextView.setText(newTimeString);
            }
        });


        /* Spinner */
//        Spinner dropdown = (Spinner)findViewById(R.id.planets_spinner);
//        // Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.durationHour, android.R.layout.simple_spinner_dropdown_item);
//        // Specify the layout to use when the list of choices appears
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        // Apply the adapter to the spinner
//        dropdown.setAdapter(adapter);

//        Button buttnnPaypal = (Button) findViewById(R.id.btnPaypal);
//        buttnnPaypal.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                List<NameValuePair> params = new
//                        ArrayList<NameValuePair>();
//                params.add(new
//                        BasicNameValuePair("user", "popo"));
//                String strURL = "http://localhost/webServiceJSON/helloJSON.php";
//            /*JSONParser objJSONParser = new JSONParser();*/
//                JSONObject jsonObj =
//                        makeHttpRequest(strURL, "POST", params);
//                String strFromPHP = jsonObj.optString("message");
//                EditText editTextRate = (EditText) findViewById(R.id.edtxtRate);
//                editTextRate.setText(strFromPHP);
//            }
//        });
//        super.onSaveInstanceState(savedInstanceState);


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
            // Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;
    }



    public void testing(View view){
        Toast.makeText(getApplicationContext(), "testing", Toast.LENGTH_SHORT).show();
        TextView textView = (TextView)findViewById(R.id.timeTextView);
        textView.setText("hi");
    }



    public void onClickPayPal(View v) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("message", "aaa"));
                String strURL = "http://192.168.1.16/webServiceJSON/helloJSON.php";

        /*JSONParser objJSONParser = new JSONParser();*/
                JSONObject jsonObj =
                        makeHttpRequest(strURL, "POST", params);
                String strFromPHP = null;
                try {
                    strFromPHP = jsonObj.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                TextView textView = (TextView)findViewById(R.id.rateTextView);
                textView.setText(strFromPHP);

            }
        };
        Thread thr = new Thread(run);
        thr.start();
    }



}
