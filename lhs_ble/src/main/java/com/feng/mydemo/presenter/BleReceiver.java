package com.feng.mydemo.presenter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.feng.mydemo.Model.BluetoothLeServiceModel;
import com.feng.mydemo.R;
import com.feng.mydemo.interf.IBleState;
import com.feng.mydemo.view.DeviceControlActivity;

/**
 * @author 刘松汉
 * @time 2016/12/19  11:37
 * @desc ${决定view显示什么界面}
 */
public class BleReceiver extends BroadcastReceiver{
    IBleState mActivity;
    static BleReceiver  mBleReceiver;
    public BleReceiver() {
    }
   public static BleReceiver  getInstance(){
        if (mBleReceiver==null){
            mBleReceiver=new BleReceiver();
        }
        return mBleReceiver;
   };
    public BleReceiver(IBleState activity) {
        mActivity = activity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (BluetoothLeServiceModel.ACTION_GATT_CONNECTED.equals(action)) {
            if (mActivity!=null)
           mActivity.setConnected(true);
//            mActivity.updateConnectionState(R.string.connected);
//            mActivity.invalidateOptionsMenu();
        } else if (BluetoothLeServiceModel.ACTION_GATT_DISCONNECTED.equals(action)) {
            if (mActivity!=null)
           mActivity.setConnected(false);
//            mActivity.updateConnectionState(R.string.disconnected);
//            mActivity.invalidateOptionsMenu();
//            mActivity.clearUI();
        } else if (BluetoothLeServiceModel.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            // Show all the supported services and characteristics on the user interface.
          //  mActivity.displayGattServices(mActivity.getDatas());

        } else if (BluetoothLeServiceModel.ACTION_DATA_AVAILABLE.equals(action)) {
            //Log.d("BleReceiver", action);
          //  Toast.makeText(context, intent.getStringExtra(BluetoothLeServiceModel.EXTRA_DATA), Toast.LENGTH_SHORT).show();
          byte[] bytearr=  intent.getByteArrayExtra(BluetoothLeServiceModel.EXTRA_DATA);
          receviedData(bytearr,bytearr.length);
        }
    }

    public static native void   receviedData(byte[] data,int len);
}
