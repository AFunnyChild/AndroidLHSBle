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
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.feng.mydemo.bean.BleConstant;
import com.feng.mydemo.bean.SampleGattAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * @author 刘松汉
 * @time 2016/12/19  9:45
 * @desc ${处理不同的数据请求}
 */
public class BluetoothLeServiceModel extends Service {

    private final static String TAG = BluetoothLeServiceModel.class.getSimpleName();

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
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
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
            Toast.makeText(BluetoothLeServiceModel.this, "读取到数据", Toast.LENGTH_SHORT).show();
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
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
          //  Toast.makeText(this, "heartRate:" + heartRate, Toast.LENGTH_SHORT).show();
        } else {
            // 对于所有配置文件，写入格式为十六进制的数据。
            final byte[] data = characteristic.getValue();
           // String s = bytes2HexString(data);
            String s=new String(data);
            intent.putExtra(EXTRA_DATA,  s);
      //   Toast.makeText(this, "heartRate:" + s, Toast.LENGTH_SHORT).show();

        }
        sendBroadcast(intent);
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
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
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
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
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
            Log.w(TAG, "BluetoothAdapter not initialized");
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
            Log.w(TAG, "BluetoothAdapter not initialized");
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


}
