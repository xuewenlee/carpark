package com.example.xuewen.carpark;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by xuewen on 27/11/2015. new notification for the aps
 */
public class MoreInfoNotification extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_info_notific);
    }

    public void onClickMain(View view) {
        Intent a = new Intent(MoreInfoNotification.this, MainActivity.class);
        startActivity(a);
        finish();
    }

    public void onClickEnd(View view) {
       finish();
    }
}
