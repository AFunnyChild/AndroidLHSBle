package com.hnnk.lsh.ble;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.feng.mydemo.activity.BleScanActivity;
import com.feng.mydemo.activity.BleTestMainActivity;
import com.feng.mydemo.presenter.BleReceiver;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static Activity  activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_state).setOnClickListener(this);
        activity=this;
     //   startActivity(new Intent(this, BleTestMainActivity.class));

//        Intent dialogIntent = new Intent(getBaseContext(), BleScanActivity.class);
//        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        getApplication().startActivity(dialogIntent);
    }

    @Override
    public void onClick(View view) {
//        BleScanActivity  bleScanActivity=new BleScanActivity(this);
//        bleScanActivity.showBleWindow();
    if (view.getId()==R.id.btn_start){
        onStartBlueTooth(1);
    }
    if (view.getId()==R.id.btn_state){

        Toast.makeText(activity, "connect", Toast.LENGTH_SHORT).show();
    }
    }
    public static void onStartBlueTooth(int isStart) {
        BleScanActivity  bleScanActivity=new BleScanActivity(activity);
        bleScanActivity.show();
    }

}
