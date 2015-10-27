package com.example.xuewen.carpark;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static InputStream is = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        /* Spinner */
        /////////////////Spinner spinner = (Spinner) findViewById(R.id.durationSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        /////////////////ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
           ///////////// R.array.durationHour, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        /////////////////adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        ////////spinner.setAdapter(adapter);

        Spinner dropdown = (Spinner)findViewById(R.id.durationSpinner);
//        String[] items = new String[]{"1", "2", "3"};
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.durationHour, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        dropdown.setAdapter(adapter);

        Button buttnnPaypal = (Button) findViewById(R.id.btnPaypal);
        buttnnPaypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<NameValuePair> params = new
                        ArrayList<NameValuePair>();
                params.add(new
                        BasicNameValuePair("user", "popo"));
                String strURL = "http://localhost/webServiceJSON/helloJSON.php";
            /*JSONParser objJSONParser = new JSONParser();*/
                JSONObject jsonObj =
                        makeHttpRequest(strURL, "POST", params);
                String strFromPHP = jsonObj.optString("message");
                EditText editTextRate = (EditText) findViewById(R.id.edtxtRate);
                editTextRate.setText(strFromPHP);
            }
        });
        super.onSaveInstanceState(savedInstanceState);

    }

    public JSONObject makeHttpRequest(String url, String method,
            List<NameValuePair> params)  {

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

        return null;
    }


}
