package admin.parimargin.com.androidble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sandeep on 6/5/2016.
 */

public class DeviceDetailsActivity extends AppCompatActivity{
    public static final String TAG = DeviceDetailsActivity.class.getName();
    BluetoothDevice device;
    BluetoothGatt bluetoothGatt;
    List<BluetoothGattCharacteristic> characteristics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_deatail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothDevice device = getIntent().getExtras().getParcelable("SELECTED_BLUETOOTH_DEVICE");
        if(device != null) {
            connectDevice(device);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(bluetoothGatt!= null) {
            bluetoothGatt.disconnect();

        }
    }

    public void connectDevice(BluetoothDevice device) {
        if(bluetoothGatt == null) {
            bluetoothGatt = device.connectGatt(this, false, gattCallBack);
        }
    }

    BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d(TAG, "STATE_CONNECTED");
                    bluetoothGatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.d(TAG, "STATE_DISCONNECTED");
                    break;
                default:
                    Log.d(TAG, "STATE_OTHER");

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            final List<BluetoothGattService> services = gatt.getServices();
            characteristics = new ArrayList<BluetoothGattCharacteristic>();
            characteristics.add(services.get(1).getCharacteristics().get(0));
            characteristics.add(services.get(1).getCharacteristics().get(1));
            characteristics.add(services.get(1).getCharacteristics().get(2));

            characteristics.add(services.get(2).getCharacteristics().get(0));
            characteristics.add(services.get(2).getCharacteristics().get(1));
            characteristics.add(services.get(2).getCharacteristics().get(2));

            characteristics.add(services.get(3).getCharacteristics().get(0));
            characteristics.add(services.get(3).getCharacteristics().get(1));
            characteristics.add(services.get(3).getCharacteristics().get(2));
            requesReadCharacteristics(gatt);
        }

        public void requesReadCharacteristics(BluetoothGatt gatt) {
            gatt.readCharacteristic(characteristics.get(characteristics.size() - 1));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0 ) {
                Log.d(TAG, "DeviceNameFetchFromDevice: " + characteristic.getValue());
                characteristics.remove(characteristics.get(characteristics.size() - 1));

                if (characteristics.size() > 0) {
                    requesReadCharacteristics(gatt);
                } else {
                    gatt.disconnect();
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
    };
}
