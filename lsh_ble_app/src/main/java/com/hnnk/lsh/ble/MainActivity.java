package com.hnnk.lsh.ble;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.feng.mydemo.activity.BleScanActivity;
import com.feng.mydemo.activity.BleTestMainActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(this);
     //   startActivity(new Intent(this, BleTestMainActivity.class));

//        Intent dialogIntent = new Intent(getBaseContext(), BleScanActivity.class);
//        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        getApplication().startActivity(dialogIntent);
    }

    @Override
    public void onClick(View view) {
        BleScanActivity  bleScanActivity=new BleScanActivity(this);
        bleScanActivity.showBleWindow();
    }
}
