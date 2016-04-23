package com.example.jasper.suitcase;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import com.jasper.ble.BLEScanHelper;
import com.jasper.ble.BluetoothRfcommClient;
import com.jasper.ble.DeviceListAdapter;

import java.util.ArrayList;
/**
 * Main activity.
 *
 * @author JasperWong
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_DEVICE_OBJ = "EXTRA_DEVICE_OBJ";

    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

    public static BluetoothRfcommClient mRfcommClient = null;

    private BLEScanHelper mScanHelper;

    private ListView mListView;

    private DeviceListAdapter mAdapter;

//    private BluetoothDevice mBluetoothDevice=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.suitcase_72px);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "本机没有找到蓝牙硬件或驱动！", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 如果本地蓝牙没有开启，则开启
        if (!mBluetoothAdapter.isEnabled()) {
            // 我们通过startActivityForResult()方法发起的Intent将会在onActivityResult()回调方法中获取用户的选择，比如用户单击了Yes开启，
            // 那么将会收到RESULT_OK的结果，
            // 如果RESULT_CANCELED则代表用户不愿意开启蓝牙
            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mIntent, 1);
            // 用enable()方法来开启，无需询问用户(实惠无声息的开启蓝牙设备),这时就需要用到android.permission.BLUETOOTH_ADMIN权限。
            // mBluetoothAdapter.enable();
            // mBluetoothAdapter.disable();//关闭蓝牙
        }

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mScanHelper.stopScan();
//                mDeviceList.clear();
//                mScanHelper.startScan();
//
//            }
//        });

//        mScanHelper.startScan();
//        ActionBar actionBar = getActionBar();
//        actionBar.setDisplayUseLogoEnabled(false);
//        actionBar.setDisplayShowHomeEnabled(false);
//        actionBar.setDisplayShowTitleEnabled(false);
//        actionBar.setCustomView(R.layout.action_bar_title);
//        actionBar.setDisplayShowCustomEnabled(true);

        mListView = (ListView) findViewById(R.id.lv_paired);
        mAdapter = new DeviceListAdapter(this);
        mAdapter.setData(mDeviceList);
        mListView.setAdapter(mAdapter);
        mAdapter.setListener(onConnectListener);

        //
        mScanHelper = new BLEScanHelper(this);
        mScanHelper.initialize();
        mScanHelper.setOnScanDeviceListener(mOnScanDeviceListener);
    }

    DeviceListAdapter.OnConnectButtonClickListener onConnectListener = new DeviceListAdapter.OnConnectButtonClickListener()
    {
        @Override
        public void onConnectButtonClick(BluetoothDevice device)
        {
            Intent intent = new Intent(MainActivity.this, FunctionActivity.class);
            intent.putExtra(EXTRA_DEVICE_OBJ,device);
            startActivity( intent );
        }
    };


    BLEScanHelper.onScanDeviceListener mOnScanDeviceListener = new BLEScanHelper.onScanDeviceListener()
    {

        @Override
        public void onScanDevice(final BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.add(device);
                }
            });
        }
    };

    @Override
    protected void onResume() {
        mDeviceList.clear();

        mScanHelper.startScan();

        super.onResume();
    }


    @Override
    public void onPause()
    {
        mScanHelper.stopScan();

        super.onPause();
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id=item.getItemId();
        switch (id){
            case R.id.menu_refresh:
                mScanHelper.stopScan();
                mDeviceList.clear();
                mScanHelper.startScan();
        }

        return super.onOptionsItemSelected(item);
    }
}
