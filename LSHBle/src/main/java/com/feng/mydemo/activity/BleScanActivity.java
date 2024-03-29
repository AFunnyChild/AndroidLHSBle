package com.feng.mydemo.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.feng.mydemo.Model.BluetoothLeServiceModel;
import com.feng.mydemo.R;
import com.feng.mydemo.presenter.GPSPresenter;
import com.feng.mydemo.view.DeviceControlActivity;

import java.util.ArrayList;

/**
 * @author 刘松汉
 * @time 2016/12/20  14:59
 * @desc ${TODD}
 */
@SuppressLint("NewApi")
public class BleScanActivity extends Dialog implements View.OnClickListener, AdapterView.OnItemClickListener, GPSPresenter.GPS_Interface {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    // 10秒后停止查找搜索.
    private static final long SCAN_PERIOD = 300000;//请求码
    private static  final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION  = 100;
    private ListView mListView;
    private TextView mBtnScan;
    private ProgressBar mPbScan;
   public Context mContext;
    private GPSPresenter gps_presenter;

    public BleScanActivity(@NonNull Context context) {
        super(context);
        this.mContext=context;
    }
   public void showBleWindow(){
        show();
       WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
       DisplayMetrics dm = new DisplayMetrics();
       wm.getDefaultDisplay().getMetrics(dm);

       Window window = getWindow();
       WindowManager.LayoutParams params = window.getAttributes();

      getWindow().setBackgroundDrawable(null);


       params .width = dm.widthPixels*1/2;
       params .height =dm.heightPixels*8/9;
       params.gravity = Gravity.CENTER;
       window.setAttributes(params);

   }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  getActionBar().setTitle(R.string.title_devices);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_scan_ble);
        mListView = findViewById(R.id.ls_scan);
        mBtnScan= findViewById(R.id.btn_scan);
        mPbScan = findViewById(R.id.pb_scan);
        mBtnScan.setOnClickListener(this);
        mListView.setOnItemClickListener(this);
        findViewById(R.id.ic_close).setOnClickListener(this);
        mHandler = new Handler();
        if (!checkGPSPermission(mContext)){
           // openGPSDialog((Activity) mContext);
          //  return  false;
             CountDownTimer countDownTimer = new CountDownTimer(1500, 100) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    showConfirmDialog(mContext, "搜索蓝牙需要启动定位,您的手机没有开启定位，请开启后再试", confirm -> {
                        if (confirm) {
                            //跳转到手机打开GPS页面
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //设置完成后返回原来的界面
                            Activity activity = (Activity) mContext;
                            activity.startActivityForResult(intent,0);
                          //  dismiss();
                        } else {
                            Log.e("BleScan", "user manually refuse ACCESSIBILITY_SERVICE");
                        }
                    });
                }
            };
            countDownTimer.start();
        }
//        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
//        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
//            finish();
//        }
        if(Build.VERSION.SDK_INT>=23){
            setPermission();
        }
        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(mContext, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();

            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            //若没打开则打开蓝牙
            mBluetoothAdapter.enable();

        }
        // 初始化蓝牙列表adapter
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mListView.setAdapter(mLeDeviceListAdapter);
        // setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    /**
     * 判断GPS是否开启
     */
    private boolean checkGPSPermission(Context  context) {
        //当Android版本大于等于6.0时才会加载布局，注册广播接收器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gps_presenter = new GPSPresenter(context, this);
            boolean gpsIsOpen = gps_presenter.gpsIsOpen(context);
            return  gpsIsOpen;
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//            //判断GPS是否开启，没有开启，则开启
//            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        }
         return true;
    }
    /**
     * 打开GPS对话框
     */
    private void openGPSDialog(final Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("请打开GPS连接")
                .setMessage("为了提高定位的准确度，更好的为您服务，请打开GPS")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //跳转到手机打开GPS页面
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //设置完成后返回原来的界面
                        context.startActivityForResult(intent,0);
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    @Override
    public void gpsSwitchState(boolean gpsOpen) {
     if (gpsOpen){
         // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
         final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
         mBluetoothAdapter = bluetoothManager.getAdapter();
         // 检查设备上是否支持蓝牙
         if (mBluetoothAdapter == null) {
             Toast.makeText(mContext, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();

             return;
         }
         if (!mBluetoothAdapter.isEnabled()) {
             //若没打开则打开蓝牙
             mBluetoothAdapter.enable();

         }
         // 初始化蓝牙列表adapter
         mLeDeviceListAdapter = new LeDeviceListAdapter();
         mListView.setAdapter(mLeDeviceListAdapter);
         // setListAdapter(mLeDeviceListAdapter);
         scanLeDevice(true);
     }
    }

    private interface OnConfirmResult {
        void confirmResult(boolean confirm);
    }
    String  mMessage="";
    private Dialog dialog;
    private void showConfirmDialog(Context context, String message, final OnConfirmResult result) {
        //
        if (dialog ==null||(mMessage.equals(message)==false)||(mContext==null)||(mContext!=context)) {
            if (dialog!=null){
                dialog.dismiss();
                dialog=null;
            }
            mMessage=message;
            mContext= context;
            dialog = new AlertDialog.Builder(mContext).setCancelable(false).setTitle("")
                    .setMessage(message)
                    .setPositiveButton("现在去开启",
                            (dialog, which) -> {
                                result.confirmResult(true);
                                dialog.dismiss();
                                dialog=null;
                            }).create();

//            if(!Settings.canDrawOverlays(mContext)) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//                mContext.startActivity(intent);
//                return;
//            } else {
//                //绘ui代码, 这里说明6.0系统已经有权限了
//            }
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){//6.0
//                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
//            }else {
//                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
//
//            }
//            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
//                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
//            }else{
//                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
//            }


        }

//.setNegativeButton("暂不开启",
//                (dialog, which) -> {
//                    result.confirmResult(false);
//                    dialog.dismiss();
//                    dialog=null;
//                })


        if (dialog.isShowing()==false){

            dialog.show();
        }

    }
    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
////        getMenuInflater().inflate(R.menu.main, menu);
////        if (!mScanning) {
////            menu.findItem(R.id.menu_stop).setVisible(false);
////            menu.findItem(R.id.menu_scan).setVisible(true);
////            menu.findItem(R.id.menu_refresh).setActionView(null);
////        } else {
////            menu.findItem(R.id.menu_stop).setVisible(true);
////            menu.findItem(R.id.menu_scan).setVisible(false);
////            menu.findItem(R.id.menu_refresh).setActionView(
////                    R.layout.actionbar_indeterminate_progress);
////        }
//       return true;
//    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId()==R.id.menu_scan){
//            mLeDeviceListAdapter.clear();
//            scanLeDevice(true);
//        }
//        if (item.getItemId()==R.id.menu_stop){
//            scanLeDevice(false);
//        }
        return true;
    }




//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//        //点击蓝牙列表的摸个设备会触发此方法
//        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
//        if (device == null) return;
//        final Intent intent = new Intent(this, DeviceControlActivity.class);
//        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
//        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
//        if (mScanning) {
//            mBluetoothAdapter.stopLeScan(mLeScanCallback);
//            mScanning = false;
//        }
//        startActivity(intent);
//    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // 扫面蓝牙,并在扫面后设置扫描为false.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.btn_scan){

           // mBtnScan.setText("SCAN");
        //    mPbScan.setVisibility(View.INVISIBLE);
            mLeDeviceListAdapter.clear();
            scanLeDevice(true);
//            if (!mScanning) {
//                mBtnScan.setText("SCAN");
//                mPbScan.setVisibility(View.INVISIBLE);
//                mLeDeviceListAdapter.clear();
//                scanLeDevice(true);
//            } else {
//                scanLeDevice(false);
//                mBtnScan.setText("STOP");
//                mPbScan.setVisibility(View.VISIBLE);
//            }
        }

//        if (item.getItemId()==R.id.menu_scan){
//            mLeDeviceListAdapter.clear();
//            scanLeDevice(true);
//        }
        if (v.getId()==R.id.ic_close){
            scanLeDevice(false);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
            mLeDeviceListAdapter.clear();
          this.cancel();
          this.dismiss();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;


        Intent startIntent = new Intent(getContext(), BluetoothLeServiceModel.class);
        startIntent.putExtra(BluetoothLeServiceModel.EXTRAS_DEVICE_NAME, device.getName());
        startIntent.putExtra(BluetoothLeServiceModel.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        boolean isChairBlue=true;
        if (device.getName().contains("H2000")||device.getName().contains("BLE")||device.getName().contains("HNNK_")){
            isChairBlue= false;
        }
        startIntent.putExtra(BluetoothLeServiceModel.DEVICE_IS_CHAIR, isChairBlue);
         getContext().startService(startIntent);
         BleScanActivity.this.dismiss();
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
       // startActivity(intent);
    }

    // 蓝牙设备列表适配器
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = BleScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0){
                String fitterName=deviceName.replace("H2000","脑机生命环").replace("HNNK_","脑机生命环").replace("BLE","脑机生命环");
                viewHolder.deviceName.setText(fitterName);
            }
            else{
                viewHolder.deviceName.setText(R.string.unknown_device);
            }

            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    // 设备扫描回调
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            new Handler(mContext.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    // 在这里执行你要想的操作 比如直接在这里更新ui或者调用回调在 在回调中更新ui
                     String deviceName = device.getName();
                    if (deviceName != null && deviceName.length() > 0){
                     //   String fitterName=deviceName.replace("H2000","脑机生命环").replace("HNNK_","脑机生命环");
                          if (deviceName.contains("BLE")||deviceName.contains("H2000")||deviceName.contains("HNNK_")||deviceName.contains("串口")){
                              mLeDeviceListAdapter.addDevice(device);
                              mLeDeviceListAdapter.notifyDataSetChanged();
                          }

                    }

                }
            });


        }
    };

    //android6.0以上启用
    private void setPermission() {
        //判断是否有权限
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            //判断是否需要 向用户解释，为什么要申请该权限
            if(ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                    Manifest.permission.READ_CONTACTS)) {
              //  Toast.makeText(mContext, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //判断位置是否打开
    public static final boolean isGpsEnable(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }
    //封装蓝牙设备的名字和地址
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

   //     scanLeDevice(false);

        //mLeDeviceListAdapter.clear();

    }
}