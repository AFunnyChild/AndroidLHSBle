package com.hnnk.lsh.ble;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ryan.socketwebrtc.MainActivity;
import com.ryan.socketwebrtc.WebRTCClient;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import pub.devrel.easypermissions.EasyPermissions;

public class CallTestActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_test);
      findViewById(R.id.btn_end).setOnClickListener(this);
      findViewById(R.id.btn_start).setOnClickListener(this);
      findViewById(R.id.btn_close).setOnClickListener(this);
      findViewById(R.id.btn_open).setOnClickListener(this);
        String[] perms = {
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "申请权限", 0, perms);
        }

        ((TextView)findViewById(R.id.ip_address)).setText(getLocalIpAddress());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    public void onBtnServer1(View view) {
        WebRTCClient.getInstance().asServer(this);

    }

    public void onBtnClient1(View view) {
        WebRTCClient.getInstance().asClient(this,"192.168.0.17");

    }


    public static String getLocalIpAddress() {
        String strIP=null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        strIP= inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("zhf-msg", ex.toString());
        }
        return strIP;
    }

    @Override
    public void onClick(View view) {
          if (view.getId()==R.id.btn_start){
              onBtnServer1(view);
          }else  if(view.getId()==R.id.btn_end){
              onBtnClient1(view);
          }else  if(view.getId()==R.id.btn_open){
              WebRTCClient.getInstance().asClient(this,"192.168.0.17");
          }else  if(view.getId()==R.id.btn_close){
              WebRTCClient.getInstance().closeClient();
          }
    }
    public  void toa(String  msg){
     runOnUiThread(new Runnable() {
         @Override
         public void run() {
             Toast.makeText(CallTestActivity.this, msg, Toast.LENGTH_SHORT).show();
         }
     });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebRTCClient.getInstance().onDestroy();
    }
}
