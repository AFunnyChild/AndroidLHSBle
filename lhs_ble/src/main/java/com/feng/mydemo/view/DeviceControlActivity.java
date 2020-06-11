package com.feng.mydemo.view;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.feng.mydemo.Model.BluetoothLeServiceModel;
import com.feng.mydemo.R;
import com.feng.mydemo.adapter.ListviewAdapter;
import com.feng.mydemo.bean.MsgInfo;
import com.feng.mydemo.bean.SampleGattAttributes;
import com.feng.mydemo.presenter.BleReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



;

/**
 * @author 刘松汉
 * @time 2016/12/19  11:10
 * @desc ${view 显示界面}
 */
public class DeviceControlActivity extends Activity implements View.OnClickListener {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    Button mBtnRight;

    EditText mEtMeg;
    private String mDeviceName;
    private String mDeviceAddress;
    ArrayList<String>  mUDs=new ArrayList<>();
    private BluetoothLeServiceModel mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    BleReceiver mBleReceiver;

    private ListviewAdapter adapter;

    private ListView listview;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private List<BluetoothGattService> mSupportedGattServices;

    public boolean isConnected() {
        return mConnected;
    }

    public void setConnected(boolean connected) {
        mConnected = connected;
    }

    // 管理服务生命周期的代码。
    private final ServiceConnection mServiceConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeServiceModel.LocalBinder) service).getService();

            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // 成功启动初始化后自动连接到设备。
            boolean connect = mBluetoothLeService.connect(mDeviceAddress);
            mSupportedGattServices = mBluetoothLeService.getSupportedGattServices();
            Log.d("DeviceControlActivity", "connect:" + connect);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);
      mBtnRight= findViewById( R.id.btn_right);
      mEtMeg= findViewById( R.id.et_meg);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Intent gattServiceIntent = new Intent(this, BluetoothLeServiceModel.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        initView();
        adapter = new ListviewAdapter(this);
        listview.setAdapter(adapter);
        mBtnRight.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBleReceiver = BleReceiver.getInstance();
        registerReceiver(mBleReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
        }
    }


    private IntentFilter makeGattUpdateIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeServiceModel.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeServiceModel.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeServiceModel.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeServiceModel.ACTION_DATA_AVAILABLE);

        return intentFilter;
        // return BleUtils.makeGattUpdateIntentFilter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBleReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);

        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    public void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // ddmConnectionState.setText(resourceId);
            }
        });
    }

    public void displayData(String data) {


        if (data != null) {
            //   String msg = et_meg.getText().toString().trim();
            adapter.addDataToAdapter(new MsgInfo(data, null));
            adapter.notifyDataSetChanged();
            listview.smoothScrollToPosition(listview.getCount() - 1);
            //   et_meg.setText("");
        }
    }

    public List<BluetoothGattService> getDatas() {


        return mBluetoothLeService.getSupportedGattServices();
    }

    public void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                mUDs.add(gattCharacteristic.getUuid().toString().substring(4,7));
                Log.d("DeviceControlActivity", gattCharacteristic.getUuid().toString().substring(4, 7));
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                uuid = gattCharacteristic.getUuid().toString();
                Log.d("DeviceControlActivity", uuid);
                if (uuid.contains("6e400003")) {
                    Log.e("console", "2gatt Characteristic: " + uuid);
                    mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                    mNotifyCharacteristic = mBluetoothLeService.getBluetoothGattCharacteristic();
                    Log.e("console", "2gatt Characteristic: " + mNotifyCharacteristic.describeContents());
                   mBluetoothLeService.readCharacteristic(gattCharacteristic);
                }

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }


    }

    public void clearUI() {

    }

    private void initView() {
        listview = (ListView) findViewById(R.id.listview);
        mBtnRight = (Button) findViewById(R.id.btn_right);
    }

    @Override
    public void onClick(View v) {
        String s = mEtMeg.getText().toString();
        adapter.addDataToAdapter(new MsgInfo(null, s));
        adapter.notifyDataSetChanged();
        //
        //                mBluetoothLeService.sendMsg(b1);
        if (mNotifyCharacteristic == null) {
            Toast.makeText(DeviceControlActivity.this, "device connect is null", Toast.LENGTH_SHORT).show();
        }
        //                Arrays.toString(b1);
        //                Toast.makeText(DeviceControlActivity.this,""+b,Toast.LENGTH_SHORT).show();
        //mNotifyCharacteristic.setValue(getHexBytes(s));
        try{
            byte[] ss=new byte[1];
            ss[0]=7;
            mNotifyCharacteristic.setValue(ss);
            mBluetoothLeService.writeCharacteristic(mNotifyCharacteristic);
        }catch (Exception e){
            System.out.println("null connect"+e.getStackTrace());
        }

        mEtMeg.setText("");
    }
    /**
     * @param message  字符串转化为byte数组
     * @return
     */
    public static byte[] getHexBytes(String message) {
        int len = message.length() / 2;
        char[] chars = message.toCharArray();
        String[] hexStr = new String[len];
        byte[] bytes = new byte[len];
        try {
            for (int i = 0, j = 0; j < len; i += 2, j++) {
                hexStr[j] = "" + chars[i] + chars[i + 1];
                bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
            }
        }catch (Exception e){

            return null;
        }

        return bytes;
    }
}
