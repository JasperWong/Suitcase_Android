package com.example.jasper.suitcase;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import android.widget.TextView;
import com.jasper.ble.BLEService;
import com.jasper.ble.GATTUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FunctionActivity extends AppCompatActivity {

    private final static String TAG = FunctionActivity.class.getSimpleName();

//    private ArrayList<ClassMenuItem> mMenuList = new ArrayList<ClassMenuItem>();

    private ListView mListView;

    //    private MenuListAdapter mAdapter;
    ArrayList<InputStream> inputStreamArrayList = new ArrayList<InputStream>();

    private BLEService mBluetoothLeService = null;

    private BluetoothDevice mBluetoothDevice = null;

    public int distance = 0;

    public final int setting = 100;

    private  char IsLock ='1';

    private char IsMove='0';

    private char IsMiss='0';

    private BluetoothGattCharacteristic mCharacteristic;

    private BluetoothGatt mBluetoothGatt = null;

    private TextView textView = null;

    private Handler mHandler;

    private Runnable mCircleRevealRunnable;

    public int rssi_view=0;

    public int rssi_set=-90;

    public long []pattern={1000,2000,1000,3000};

    public String rssi_edit="";

    public EditText editText=null;

    private Vibrator vibrator=null;

    private Handler handler =new Handler();

    public byte[] send={0,0,0,0,0};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_function);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.suitcase_72px);
        textView = (TextView) findViewById(R.id.RSSI);
        final ImageView lock_view = (ImageView) findViewById(R.id.lock);
        final ImageView move_view=(ImageView)findViewById(R.id.move);

        Intent intent = getIntent();

        mBluetoothDevice = (BluetoothDevice) intent.getParcelableExtra(MainActivity.EXTRA_DEVICE_OBJ);
        Intent gattServiceIntent = new Intent(this, BLEService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattCallback);

        vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        getSystemService(VIBRATOR_SERVICE);

        lock_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsLock=='1') {
                    lock_view.setImageResource(R.drawable.unlocked);
                    IsLock = '0';
                } else {
                    lock_view.setImageResource(R.drawable.locked);
                    IsLock = '1';
                }
            }
        });

        move_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsMove=='1') {
                    move_view.setImageResource(R.drawable.play);
                    IsMove = '0';
                } else {
                    move_view.setImageResource(R.drawable.pause);
                    IsMove = '1';
                }
            }
        });

        vibrator.vibrate(pattern, -1);
        handler.post(runnable);
    }

    Runnable runnable =new Runnable() {
        public void run() {
            textView.setText("rssi: " + rssi_view);
            if(rssi_view<rssi_set){
                vibrator.vibrate(1000);
                IsMiss='1';
            }
            else {
                vibrator.cancel();
                IsMiss='0';
            }
            handler.postDelayed(runnable, 1000);
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG, "start service Connection");

            mBluetoothLeService = ((BLEService.LocalBinder) service).getService();

            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            Log.d(TAG, "mDeviceAddress = " + mBluetoothDevice.getAddress());
            boolean status = mBluetoothLeService.connect(mBluetoothDevice.getAddress());
            if (false == status) {
                Log.d(TAG, "Connection failed");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "end Service Connection");
            mBluetoothLeService = null;
        }
    };

    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, buildGattUpdateIntentFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mGattUpdateReceiver);
        mBluetoothLeService.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService.disconnect();
        mBluetoothLeService.close();
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override

        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "enter BroadcastReceiver");
            final String action = intent.getAction();
            Log.d(TAG, "action = " + action);
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "BroadcastReceiver :" + "device connected");

            }
            else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {

            }
            else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "services discovered!!!");
                //从搜索出来的services里面找出合适的service
                List<BluetoothGattService> gattServiceList = mBluetoothLeService.getSupportedGattServices();
                BluetoothGattCharacteristic characteristic = GATTUtils.lookupGattServices(gattServiceList, GATTUtils.BLE_TX);
                // Error
                characteristic.setValue("this is a test write for characteristic");
                //characteristic.setValue("test");
                mBluetoothLeService.writeCharacteristic(characteristic);
            }
            else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "receive data");

            }
            else if (BLEService.ACTION_GATT_RSSI.equals(action)) {
                Log.d(TAG, "BroadCast + RSSI");
            }
            else if (BLEService.ACTION_DATA_WRITE.equals(action)) {
                Log.d(TAG, "Write");
                List<BluetoothGattService> gattServiceList = mBluetoothLeService.getSupportedGattServices();
                BluetoothGattCharacteristic characteristic = GATTUtils.lookupGattServices(gattServiceList, GATTUtils.BLE_TX);
                BuildPackage((byte)IsLock,(byte)IsMove,(byte)IsMiss);
                characteristic.setValue(send);
                mBluetoothLeService.writeCharacteristic(characteristic);
                mBluetoothGatt.readRemoteRssi();
            }
        }
    };

    BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        // Connection state changed.
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.i(TAG, "Connected to " + gatt.getDevice().getAddress());
                Log.i(TAG, "Read remote RSSI.");
                gatt.readRemoteRssi();
                // Circle-Reveal graph once we're connected. ~2 seconds before data starts coming in.
                mHandler.postDelayed(mCircleRevealRunnable, 2200);
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from " + gatt.getDevice().getAddress());
            }
        }

        // New RSSI received.
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            // Only update graph on every 10th RSSI read (average from 10 RSSIs).
            rssi_view=rssi;
            Log.d("rssi", rssi + " ");
            mHandler.postDelayed(mStartRssiScanRunnable, 100);
    }

        final Runnable mStartRssiScanRunnable = new Runnable() {
        @Override
            public void run() {
                mBluetoothGatt.readRemoteRssi();
            }
        };
    };

    public void BuildPackage(byte Islock,byte IsMove,byte IsMiss)
    {
        send[0] = 's';
        send[1] = Islock;
        send[2] = IsMove;
        send[3] = IsMiss;
        send[4] = 'u';
    }

    private static IntentFilter buildGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BLEService.ACTION_GATT_RSSI);
        intentFilter.addAction(BLEService.ACTION_DATA_WRITE);
        return intentFilter;
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){

        int id=item.getItemId();
        switch (id){
            case R.id.setting:
                LayoutInflater inflater = getLayoutInflater();
                View edit = inflater.inflate(R.layout.edit, null);
                AlertDialog.Builder dialog=new AlertDialog.Builder(FunctionActivity.this);
                dialog.setCancelable(true);
                dialog.setView(edit);
                editText=(EditText)edit.findViewById(R.id.rssi_edit);
                dialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rssi_edit=editText.getText().toString();
                        rssi_set=Integer.parseInt(rssi_edit);
                    }
                });
                dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "cancel");
                    }
                });
                dialog.show();

        }
        return super.onOptionsItemSelected(item);
    }
}
