package admin.parimargin.com.androidble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class ListDeviceActivity extends AppCompatActivity {
    public static final String TAG = ListDeviceActivity.class.getName();
    private int REQUEST_BT_ENABLE = 1;
    private static final long SCAN_PERIOD = 10000;

    private android.os.Handler handler;
    private Button searchDevice;
    private ListView deviceList;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanSettings scanSettings;
    private List<ScanFilter> scanFilters;
    private DeviceListAdapter deviceListAdapter;

    private Button scanDevices;
    private ListView deviceInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth LE is not supported", Toast.LENGTH_SHORT);
        }

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        handler = new android.os.Handler();

        initUI();
    }

    public void initUI() {
        scanDevices = (Button) findViewById(R.id.btn_search_device);
        deviceInfoList = (ListView) findViewById(R.id.list_devices);
        deviceListAdapter = new DeviceListAdapter();
        deviceInfoList.setAdapter(deviceListAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BT_ENABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT);
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();


        if(bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BT_ENABLE);
        } else {
            if(Build.VERSION.SDK_INT >= 21){
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                scanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                scanFilters = new ArrayList<ScanFilter>();
            }
            scanLeDevices(true);
        }

        scanDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothAdapter.isEnabled()) {
                    scanLeDevices(true);
                }
            }
        });

    }

    private void scanLeDevices(boolean enable) {
        if (enable) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        bluetoothAdapter.stopLeScan(startLeScan);
                    } else {
                        bluetoothLeScanner.stopScan(scanCallback);
                    }
                }
            }, SCAN_PERIOD);
            if(Build.VERSION.SDK_INT < 21) {
                bluetoothAdapter.startLeScan(startLeScan);
            } else {
                bluetoothLeScanner.startScan(scanCallback);
            }
        } else {
            if(Build.VERSION.SDK_INT < 21) {
                bluetoothAdapter.stopLeScan(startLeScan);
            } else {
                bluetoothLeScanner.stopScan(scanCallback);
            }
        }
    }

    // callback for Bluetooth le scan
    private BluetoothAdapter.LeScanCallback startLeScan =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "DeviceNameLowerAPI: " + device.getName());
                        }
                    });
                }
            };

    //callback for higher API BLuetooth le scan
    private ScanCallback scanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice device = result.getDevice();
                    deviceListAdapter.addDevice(device);
                    deviceListAdapter.notifyDataSetChanged();
                    Log.d(TAG, "DeviceNameHigherAPI: " + device);
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                    for(ScanResult scanResult: results) {
                        Log.d(TAG, "DeviceNames: " + results.toString());
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    Log.d(TAG, "ScanError: " + errorCode);
                }
            };

    public class DeviceListAdapter extends BaseAdapter {
        private List<BluetoothDevice> devices;
        private LayoutInflater inflater;

        public DeviceListAdapter() {
            super();
            devices = new ArrayList<BluetoothDevice>();
            inflater = ListDeviceActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice bluetoothDevice) {
            if(!devices.contains(bluetoothDevice)) {
                devices.add(bluetoothDevice);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return devices.get(position);
        }

        public void clear() {
            devices.clear();
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.list_device_details, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) convertView.findViewById(R.id.text_device_name);
                viewHolder.deviceAddress = (TextView) convertView.findViewById(R.id.text_mac_address);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            BluetoothDevice device = devices.get(position);
            if(device.getName() !=null && device.getName().length() > 0) {
                viewHolder.deviceName.setText(device.getName());
            } else {
                viewHolder.deviceName.setText("unknown device");
            }
            viewHolder.deviceAddress.setText(device.getAddress());
            return convertView;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}
