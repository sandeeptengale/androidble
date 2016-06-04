package admin.parimargin.com.androidble;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by sandeep on 6/5/2016.
 */

public class DeviceDetailsActivity extends AppCompatActivity{
    public static final String TAG = DeviceDetailsActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_deatail);

        BluetoothDevice device = getIntent().getExtras().getParcelable("SELECTED_BLUETOOTH_DEVICE");
        Log.d(TAG, "DeviceDeails: " + device.getName() + " " + device.getAddress());
    }
}
