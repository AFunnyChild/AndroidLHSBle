package com.feng.mydemo.Model;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.feng.mydemo.R;
import com.feng.mydemo.bean.BleConstant;
import com.feng.mydemo.bean.SampleGattAttributes;
import com.feng.mydemo.presenter.BleReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * @author 刘松汉
 * @time 2016/12/19  9:45
 * @desc ${处理不同的数据请求}
 */
public class BluetoothLeServiceModel extends Service  implements SensorEventListener {



    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private ArrayList<String> mDs=new ArrayList<>();
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED           = "com.feng.mydemo.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED        = "com.feng.mydemo.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.feng.mydemo.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE           = "com.feng.mydemo.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA                      = "com.feng.mydemo.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT       = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                onConnectStateChange(1);
               broadcastUpdate(intentAction);
                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                onConnectStateChange(0);

                broadcastUpdate(intentAction);
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                mDs.add("servicesUUID=  "+ service.getUuid());
                Log.d("onServicesDiscovered", "servicesUUID=  "+ service.getUuid());
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    Log.d("onServicesDiscovered", characteristic.getUuid().toString());
                }
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                displayGattServices(getSupportedGattServices());
            } else {
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
         //   Log.d(TAG, "onCharacteristicRead: "+"读取到数据");
            //Toast.makeText(BluetoothLeServiceModel.this, "读取到数据", Toast.LENGTH_SHORT).show();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };
    private String mDeviceAddress;
    private String mDeviceName;
    private BleReceiver mBleReceiver;

    public void writeCharacteristic(BluetoothGattCharacteristic paramBluetoothGattCharacteristic)
    {
        if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null))
        {
            Log.w("BluetoothLeService", "BluetoothAdapter not initialized");
            return;
        }
        this.mBluetoothGatt.writeCharacteristic(paramBluetoothGattCharacteristic);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        //这是心率测量剖面的特殊处理,数据解析
        //按每个规格进行.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                //格式化特征为16进制
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
            } else {
                //格式化特征为8进制
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
            }
            //1为整数值的偏移量

//            final int heartRate = characteristic.getIntValue(format, 1);
          //  Log.d(TAG, bytes2HexString(characteristic.getValue()));
            byte[] bytes = characteristic.getValue();
            receviedData(bytes,bytes.length);
        //    intent.putExtra(EXTRA_DATA,characteristic.getValue());
          //  Toast.makeText(this, "heartRate:" + heartRate, Toast.LENGTH_SHORT).show();
        }
      //  sendBroadcast(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x=event.values[0];
        float y=event.values[1];
        float z=event.values[2];
         angleSensorChanged(x,y,z);
//        tvAzimuth.setText("Azimuth 方位角: " + event.values[0] + "\n(0 - 359) 0=北, 90=东, 180=南, 270=西");
//        tvPitch.setText("Pitch 倾斜角: " + event.values[1] + "\n(-180 to 180)");
//        tvRoll.setText("Roll 旋转角: " + event.values[2] + "\n(-90 to 90)");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class LocalBinder extends Binder {
     public BluetoothLeServiceModel getService() {
            return BluetoothLeServiceModel.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * 初始化蓝牙适配器
     *
     * @return 如果成功返回true
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {

                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {

            return false;
        }

        return true;
    }

    /**
     * 连接ble设备的方法
     *
     * @param address 蓝牙设备的地址
     *
     * @return  返回结果,成功返回true ,失败false
     *
     *
     *
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            return false;
        }

        // 尝试连接设备
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {

            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            //没有发现设备返回false
            return false;
        }
        // 连接一个蓝牙
        // 建立连接并得到mBluetoothGatt,mBluetoothGatt软件的核心,进行数据交互的关键对象
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     *
     * 取消连接
     *
     *
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * 关闭蓝牙连接
     *
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     *
     *
     *
     *
     * @param characteristic 写入的特征
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {

            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     *   设置通知
     *
     * @param characteristic 行为特征
     * @param enabled 如果成功返回true
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {

            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     *
     *  获得所有的服务
     *
     * @return A 服务总和
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public BluetoothGattCharacteristic getBluetoothGattCharacteristic()
    {

        return this.mBluetoothGatt.getService(UUID.fromString(BleConstant.service)).getCharacteristic(UUID.fromString(BleConstant.Characteristic1a));
    }
    public BluetoothGattCharacteristic getWriteCharacteristic()
    {

        return this.mBluetoothGatt.getService(UUID.fromString(BleConstant.service)).getCharacteristic(UUID.fromString(BleConstant.WriteCharacteristic1a));
    }

    /**
     * @param bytes byte数组转换为string
     * @return
     */
    public static String bytes2HexString(byte[] bytes) {
        String ret = "";
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase(Locale.CHINA);
        }
        return ret;
    }
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static BluetoothLeServiceModel  bluetoothLeServiceModel;
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

 //       mSupportedGattServices = mBluetoothLeService.getSupportedGattServices();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
          if (mBluetoothGatt==null){
              bluetoothLeServiceModel=this;
              if (intent==null){
                  Log.d("Blue", "onStartCommand: false  intent null");
                  return super.onStartCommand(intent, flags, startId);
              }
              mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
              mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
              if (!initialize()) {
                  Toast.makeText(this, "UnSupport "+"Bluetooth", Toast.LENGTH_SHORT).show();
              }
              mBleReceiver = new BleReceiver();
              // 成功启动初始化后自动连接到设备。
              boolean connect = connect(mDeviceAddress);
              if (connect==false){
                  Toast.makeText(this, "UnConnect to"+mDeviceName, Toast.LENGTH_SHORT).show();

              }

              registerReceiver(mBleReceiver, makeGattUpdateIntentFilter());
          }else{
               close();
              bluetoothLeServiceModel=this;
              if (intent==null){
                  Log.d("Blue", "onStartCommand: false  intent null");
                  return super.onStartCommand(intent, flags, startId);
              }
              mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
              mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
              if (!initialize()) {
                  Toast.makeText(this, "UnSupport "+"Bluetooth", Toast.LENGTH_SHORT).show();
              }
              // 成功启动初始化后自动连接到设备。
              boolean connect = connect(mDeviceAddress);
              if (connect==false){
                  Toast.makeText(this, "UnConnect to"+mDeviceName, Toast.LENGTH_SHORT).show();

              }

          }

        return super.onStartCommand(intent, flags, startId);

    }

    private SensorManager sensorManager = null;
    private boolean mRegister = false;
    private Sensor sensor = null;
    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mRegister = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        unregisterReceiver(mBleReceiver);
        close();
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
    ArrayList<String>  mUDs=new ArrayList<>();

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private static BluetoothGattCharacteristic mWriteCharacteristic;

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
                   setCharacteristicNotification(gattCharacteristic, true);
                    mNotifyCharacteristic = getBluetoothGattCharacteristic();
                    Log.e("console", "2gatt Characteristic: " + mNotifyCharacteristic.describeContents());
                  readCharacteristic(gattCharacteristic);
                }   if (uuid.contains("6e400002")) {
                    Log.e("console", "2gatt Characteristic: write" + uuid);
                    mWriteCharacteristic = getWriteCharacteristic();

                }

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }


    }
      public static   void  writeInt(int value){
        if (mWriteCharacteristic==null){
            return;
        }
        byte[]  writeByte=new byte[1];
        writeByte[0]=(byte)value;
        mWriteCharacteristic.setValue(writeByte);
        if (bluetoothLeServiceModel!=null){
            bluetoothLeServiceModel.writeCharacteristic(mWriteCharacteristic);
        }

      }

    public static native   void  receviedData(byte[] data,int len);
    public static native   void  onConnectStateChange(int state);
    public static native   void  angleSensorChanged(float x,float y,float z);
}
