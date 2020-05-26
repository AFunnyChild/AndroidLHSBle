package com.feng.mydemo.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import java.util.ArrayList;

/**
 * @author 刘松汉
 * @time 2016/12/19  9:42
 * @desc ${备用工具类}
 */
public class BleUtils {
    private Context mContext;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
    private BluetoothGatt mBluetoothLeService;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    public BleUtils(BluetoothGatt bluetoothLeService, Context context) {
        mBluetoothLeService = bluetoothLeService;
        mContext = context;
    }
}