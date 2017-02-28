package william.arduinoproject;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.usb.*;
import android.util.Log;
import android.view.View;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    UsbManager usbManager;
    ParcelFileDescriptor fileDescriptor;
    FileInputStream iS;
    FileOutputStream oS;
    PendingIntent permissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";


    //First case checks if we do not have permission to perform an action
    //Second case checks if the action incoming is that we were detatched
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = usbManager.getAccessoryList()[0];
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        usbManager.openAccessory(accessory);
                    } else {
                        Log.d("MEGA ADK ", "permission denied for accessory " + accessory);
                    }
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {

                UsbAccessory accessory = usbManager.getAccessoryList()[0];
                if (accessory != null) {
                    try {
                        if (fileDescriptor != null) {
                            Log.d("Accessory Usb ", "Has been detached " + accessory);
                            fileDescriptor.close();
                        }
                    } catch (IOException e) {
                    } finally {
                        fileDescriptor = null;
                        accessory = null;
                    }
                }
            }
        }
    };

    //Sets up the on create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        IntentFilter usbFilter = new IntentFilter(ACTION_USB_PERMISSION);
        usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);

        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);


        Context context = getApplicationContext();
        context.registerReceiver(usbReceiver, usbFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (usbManager.getDeviceList().size() != 0) {
            UsbAccessory[] accessories = usbManager.getAccessoryList();
            UsbAccessory megaADK = (accessories == null ? null : accessories[0]);
            fileDescriptor = usbManager.openAccessory(megaADK);
            iS = new FileInputStream(fileDescriptor.getFileDescriptor());
            oS = new FileOutputStream(fileDescriptor.getFileDescriptor());
        }
    }

    public void doStuff(View v) {
        byte[] message = {(byte) 1};
        UsbAccessory[] accessories = usbManager.getAccessoryList();
        UsbAccessory megaADK = (accessories == null ? null : accessories[0]);
        if(megaADK!=null) {
            usbManager.requestPermission(megaADK, permissionIntent);
            if (usbManager.getDeviceList().size() != 0) {
                fileDescriptor = usbManager.openAccessory(megaADK);

                iS = new FileInputStream(fileDescriptor.getFileDescriptor());
                oS = new FileOutputStream(fileDescriptor.getFileDescriptor());
                try {
                    oS.write(message);
                } catch (Exception e) {
                }
            }
        }
    }
}