package william.arduinoproject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.usb.*;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.ToggleButton;
import com.vuforia.*;
//import java.io.FileDescriptor;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.*;

public class MainActivity extends AppCompatActivity {

    UsbAccessory megaADK;
    UsbManager usbManager;
    ParcelFileDescriptor fileDescriptor;
    FileInputStream iS;
    FileOutputStream oS;
    PendingIntent permissionIntent;
    IntentFilter usbFilter;
    Boolean rP;
    GLSurfaceView glSurfaceView;


    IntentFilter wifiP2P;
    Channel wifiChannel;
    WifiP2pManager wifiManager;

    BroadcastReceiver wifiReceiver;

    Boolean isWifiP2pEnabled = false;
    Context context;

    String et1;
    String et2;

    private List<WifiP2pDevice> peers;

    boolean extendedTracking = true;

    boolean wifiP2PEnabled;

 //   Vuforia vuforia = new Vuforia();
    private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";

    //First case checks if we do not have permission to perform an action
    //Second case checks if the action incoming is that we were detatched
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    //Theres only 1 accessory
                    UsbAccessory accessory = usbManager.getAccessoryList()[0];
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        megaADK = accessory;
                        setUp(megaADK);
                    } else {
                        Log.d("MEGA ADK ", "Permission denied" + accessory);
                    }
                    rP = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {

                UsbAccessory accessory = usbManager.getAccessoryList()[0];
                if(accessory == null) Log.d("Detached", "accessory no longer findable");

//              if (accessory != null && accessory.equals(megaADK)) {
//                    closeAccessory();
//              }
            }else if(UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)){
                UsbAccessory accessory = usbManager.getAccessoryList()[0];
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    megaADK = accessory;
                    setUp(megaADK);
                } else {
                    Log.d("MEGA ADK ", "Permission denied" + accessory);
                }
                rP = false;
            }
        }
    };



    //ALREADY IMPLEMENTED IN WIFIDIRECTBROADCASTRECEIVER


//    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver(){
//        @Override
//        public void onReceive(Context context, Intent intent){
//            String action = intent.getAction();
//            switch(action){
//                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
//                    wifiP2PEnabled = (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1)==WifiP2pManager.WIFI_P2P_STATE_ENABLED);
//                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
//                    if(!wifiP2PEnabled)break;
//                    if(wifiManager!=null){
//                        wifiManager.requestPeers(wifiChannel,peerListener);
//                    }
//                    Toast.makeText(context, "Peers changed, updating",Toast.LENGTH_SHORT).show();
//
//                    //OH NOOOOOOOOOOOOOOOOOOOOO
//                    try {
//                        oS.write(new byte[]{(byte) 1, (byte) 23, (byte) 4});
//                    }catch (IOException e){
//                    }
//                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
//                    Toast.makeText(context, "WE BE CONNECTED BOISSSSSSSSSSSS", Toast.LENGTH_SHORT).show();
//                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
//                    // Do Something
//            }
//        }
//    };




    //Sets up the on create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("hello", "jello");
        wifiP2P = new IntentFilter();

        wifiP2P.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiP2P.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiP2P.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiP2P.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifiChannel = wifiManager.initialize(this, getMainLooper(), null);

       // wifiReceiver = new WifiDirectBroadcastReceiver(wifiManager, wifiChannel, this);
//        registerReceiver(wifiReceiver,wifiP2P);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        usbFilter = new IntentFilter(ACTION_USB_PERMISSION);
        usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);

        context = getApplicationContext();
        glSurfaceView = new GLSurfaceView(context);
//        registerReceiver(usbReceiver, usbFilter);
        Log.i("Create", "Finished on create");
        if (getLastNonConfigurationInstance() != null) {
//            if(megaADK != null) {
//                megaADK = (UsbAccessory) getLastNonConfigurationInstance();
//                setUp(megaADK);
//            }else{
//                Toast.makeText(this,"megaADK wasn't defined",Toast.LENGTH_SHORT).show();
//            }
        }

//        final EditText editText = (EditText) findViewById(R.id.editText);
//
//        editText.setOnEditorActionListener(new TextView.OnEditorActionListener(){
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent){
//                boolean handled = false;
//                if (i == EditorInfo.IME_ACTION_DONE){
//                    String et1 = editText.getText().toString();
//                    if(et1.equals("Help")){
//                        textView.setText("Can set to these commands:" +
//                                "Set Motor Speed" +
//                                "Motor Speed" +
//                                "Forward time" +
//                                "Forward distance" +
//                                "Stop");
//                    }else if(et1.equals("Set Motor Speed")){
//                        textView.setText("Say speed on the attributes area (0 to 100)");
//                        //set the motor speed to something
//                    }else if(et1.equals("Motor Speed")){
//
//                    }
//                    editText.setText(et1);
//                }
//                return handled;
//            }
//
//        });
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(usbReceiver,usbFilter);
        //Redifining iS and oS ewould be pointless
        if (iS != null && oS != null){
            Toast.makeText(context,"is&&os null",Toast.LENGTH_SHORT).show();
            return;
        }
        //There sohuld only be 1 USB accessory which would be the Mega
        if(usbManager==null){
            Toast.makeText(context,"usbManager is null",Toast.LENGTH_SHORT).show();
            return;
        }
        UsbAccessory accessory = usbManager.getAccessoryList()[0];
        if (accessory != null) {
            if (usbManager.hasPermission(accessory)) {
                setUp(accessory);
            } else {
                synchronized (usbReceiver) {
                    if (!rP) {
                        usbManager.requestPermission(accessory, permissionIntent);
                        rP = true;
                    }
                }
            }
        } else {
            Log.d("Android Accessory", "Accessory is null");
        }
  //      wifiReceiver = new WiFiDirectBroadcastReceiver(wifiManager,wifiChannel,this);
        registerReceiver(wifiReceiver,wifiP2P);
    }



    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(usbReceiver);
        unregisterReceiver(wifiReceiver);
        closeAccessory();
    }


    @Override
    public void onDestroy() {
        unregisterReceiver(usbReceiver);
        super.onDestroy();
    }


    private void closeAccessory() {
        try {
            fileDescriptor.close();
        } catch (IOException e) {
        }
        fileDescriptor = null;
        megaADK = null;
    }

    public void connect(){
        if(peers!=null&&peers.size()!=0){
            WifiP2pDevice device = peers.get(0);
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            wifiManager.connect(wifiChannel, config, new ActionListener() {
                @Override
                public void onSuccess() {
                    //BRoadcast receiver should say connected
                }

                @Override
                public void onFailure(int reason){
                    Toast.makeText(MainActivity.this, "Connect failed",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    boolean successful;


    public void peerDiscovery(View v){
        wifiManager.discoverPeers(wifiChannel, new WifiP2pManager.ActionListener(){
            @Override
            public void onSuccess(){
                Toast.makeText(context,"hello this is successful",Toast.LENGTH_SHORT).show();
                //this is pointless but i have to init this
                successful = true;
            }

            @Override
            public void onFailure(int reasonCode){
                //this is also pointless, shucks to shuck
            }
        });
    }

    public void requestPeers(View v){
        if(peers.size()==0){
            Toast.makeText(context, "NO PEErS FOUND FEELSBADMAN", Toast.LENGTH_SHORT).show();
            return;
        }
        WifiP2pDevice device = peers.get(0);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        wifiManager.connect(wifiChannel,config,new ActionListener(){
            @Override
            public void onSuccess() {
                Toast.makeText(context, "CONNECteD BABY", Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailure(int reason){
                //hecking bamboozled
            }
        });
    }



    private PeerListListener peerListener = new PeerListListener(){
        @Override
        public void onPeersAvailable(WifiP2pDeviceList list){
            List<WifiP2pDevice> newPeers = (List) list.getDeviceList();
            if(!newPeers.equals(peers)){
                peers.clear();
                peers.addAll(newPeers);
            }

            if(peers.size() == 0){
                Toast.makeText(context, "No peers found", Toast.LENGTH_SHORT).show();
            }
        }
    };


//    public void debugArea(View v) {
//        TextView T = (TextView) findViewById(R.id.TextView);
//        T.setTextSize(15);
//        if (iS == null) {
//            T.setText("iS and oS have never been inialized");
//        } else {
////            TextView T = (TextView) findViewById(R.id.TextView);
////            T.setTextSize(15);
//            if (usbManager != null) {
//
//                String accessories1 = " ";
//                if (usbManager.getAccessoryList() == null) {
//                    //This is likely to get called
//                } else {
////                    for (int i = 0; i < usbManager.getAccessoryList().length; i++) {
////                        accessories1 = accessories1 + " " + String.valueOf(usbManager.getAccessoryList()[i]);
////                    }
////                    T.setText(accessories1);
//                }
//            } else {
//            }
//        }
//    }




//    DEPRECATED DEBUG THINGY
//    public void debugArea(View v){
//        TextView T = (TextView) findViewById(R.id.TextView);
//        T.setTextSize(15);
//        byte[] buffer = {(byte)0, (byte)1};
//        if(oS!=null){
//            try{
//                Toast.makeText(this,"Attempting to send msg", Toast.LENGTH_LONG).show();
//                T.setText("debugButton attempting to talk");
//                oS.write(buffer);
//            }catch(IOException e){
//            }
//        }
//    }

    public void clearDisplay(View v) {
        byte[] buffer = {(byte)3, (byte)4};
        if(oS!= null){
            try {
                Toast.makeText(this,"Clear Display",Toast.LENGTH_SHORT).show();
                oS.write(buffer);
            }catch (Exception e){
            }
        }
    }

    public void Forward(View v) {
        byte[] buffer = {(byte)164, (byte)5};
        if(oS!= null){
            try {
                oS.write(buffer);
            }catch (Exception e){
            }
        }
    }

    public void Backward(View v) {
        byte[] buffer = {(byte)164, (byte)6};
        if(oS!= null){
            try {
                oS.write(buffer);
            }catch (Exception e){
            }
        }
    }

    public void Stop(View v) {
        byte[] buffer = {(byte) 164, (byte) 7};
        if(oS != null){
            try{
                oS.write(buffer);
            }catch(Exception e){

            }
        }
    }

    public void sendAndReceive(View v){
        byte[] buffer = {(byte)7,(byte) 420, (byte)32, (byte) 2};
        byte[] sForBytes = {(byte)0,(byte)0,(byte)0,(byte)0};
        if(oS != null && iS != null){
            try{
                Toast.makeText(this, "Attempting to Send", Toast.LENGTH_SHORT).show();
                oS.write(buffer);
//                byte[] inputs = new byte[4];
//                for(int i =0; i < 4; i++){
//                    inputs[i]=(byte)iS.read();
//                }
//                byte[] tester = {(byte)5,(byte)1,(byte)4,(byte)3};
//                if(tester == inputs) Toast.makeText(this,"Hello from Arduino",Toast.LENGTH_LONG).show();
//     //           Toast.makeText(this,String.valueOf(iS.read()),Toast.LENGTH_LONG).show();
//                else Toast.makeText(this,inputs[0] + " " + inputs[1],Toast.LENGTH_LONG).show();
//                String inputs = " ";
//                for(int i =0; i < iS.available(); i++){
//                    inputs = inputs + String.valueOf(iS.read()) + " ";
//                }
        //        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //        ByteArrayInputStream bais = new ByteArrayInputStream();
         //       byte[] bytes =
           //     if(iS.read() == 1) Toast.makeText(this,"Second byte received",Toast.LENGTH_LONG).show();

                int numRead = iS.read(sForBytes);
                if(numRead>0){
                    Toast.makeText(this,sForBytes[0] + "   " + sForBytes[1] + "   " + sForBytes[2] + "   "
                    + sForBytes[3], Toast.LENGTH_SHORT).show();
                }


            }catch (IOException e){
                Toast.makeText(this, "Can't write/Receive", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //SENDING COMMANDS DIRECTLY TO MACHINERONIS
    public void moveForwards(View v){
        byte[] buffer = {(byte)4,(byte)2};
    }

    public void sendCircle(View v) {
        byte[] buffer = {(byte)1,(byte)0};

        // byte[] message = {(byte) 1};
        TextView T = (TextView) findViewById(R.id.TextView);
        T.setTextSize(15);

        if (oS != null) {
            try {
                T.setText("Sending message to arduino");
                oS.write(buffer);
            } catch (IOException e) {
                T.setText("Wasn't able to send text to arduino");
                Log.e("Android Accessory", "write failed", e);
            }
        } else {
            T.setText("moutput is null");
        }

//        if(buttonLED.isChecked())
//            buffer[0]=(byte)0; // button says on, light is off
//        else
//            buffer[0]=(byte)1; // button says off, light is on
//
//
//        if (mOutputStream != null) {
//            try {
//                mOutputStream.write(buffer);
//            } catch (IOException e) {
//                Log.e(TAG, "write failed", e);
//            }
//        }

        //WHat is up my dudes i am writing a program right now

//        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
//        Context context = getApplicationContext();
//        context.registerReceiver(usbReceiver, usbFilter);
//        Log.i("Yo", "Registered Receiver");
//        if (megaADK == null) {
//
//            megaADK = usbManager.getAccessoryList()[0];
//
//            if (megaADK == null) {
//                TextView T = (TextView) findViewById(R.id.TextView);
//                T.setTextSize(15);
//                T.setText("NO MEGAADK FOUND");
//            }
//        }
//        usbManager.requestPermission(megaADK, permissionIntent);
//
//        byte[] message = {(byte) 1};
////        UsbAccessory[] accessories = usbManager.getAccessoryList();
////        UsbAccessory megaADK = (accessories == null ? null : accessories[0]);
//        if (megaADK != null) {
//            if (usbManager.getAccessoryList().length != 0) {
//                fileDescriptor = usbManager.openAccessory(megaADK);
//
//                iS = new FileInputStream(fileDescriptor.getFileDescriptor());
//                oS = new FileOutputStream(fileDescriptor.getFileDescriptor());
//                try {
//                    oS.write(message);
//                } catch (Exception e) {
//                }
//            }
//        }
    }

    public void beginUsb(View v){
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        if(usbManager == null) usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if(usbManager.getAccessoryList()==null){
            Toast.makeText(context, "Accessory null from BeginUSB", Toast.LENGTH_SHORT).show();
            return;
        }
        if(megaADK != null) {
            setUp(megaADK);
        }else{
            megaADK = usbManager.getAccessoryList()[0];
            setUp(megaADK);
        }
    }

    public void setUp(UsbAccessory accessory) {
        fileDescriptor = usbManager.openAccessory(accessory);
        if (fileDescriptor != null) {
            megaADK = accessory;
            iS = new FileInputStream(fileDescriptor.getFileDescriptor());
            oS = new FileOutputStream(fileDescriptor.getFileDescriptor());
        }
    }



    private class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean> {
        private int mProgressValue = -1;
        private Activity mActivity;
        private Object mShutdownLock = new Object();
        protected Boolean doInBackground(Void... params){
            synchronized (mShutdownLock){
                Vuforia.setInitParameters(mActivity,
                        0,
                        "ASmxFXn/////AAAAGULt39uVqk5Apdm/e2Oz3PgxOgTnKuKirQ2RNnHtSMdoJ" +
                                "/f4oikqW0F6fHIwUp2EBmXxcFj976SqvXYsBR0oSITs92OjWfHjhj" +
                                "r55Xxcr73LGe0T7NC7iAWoK+AgEp/3YFed5TDdJPuLnpj9vVKZhxs" +
                                "SbhUlCVOLEmoPVrlQ9hN4XI/FfzAIJjNKRyifq+686U0kS9BEB/WD" +
                                "bMZyzxbb69nZXemrAJiDaSItce5cQRIG9LCId3uj7kqum5XyvGZnC" +
                                "vJWnpJ1fXqnrcGdZplXKbI/4GQIfuRWNltPF+uV7DohhKaKgUMvpO" +
                                "8O9PfS2WFeXSzUMgzZ+yWSaAZ+D48D4uqnoY0cyU+zyMI2+dMog3g7");
                do
                {
                    mProgressValue = Vuforia.init();
                    publishProgress(mProgressValue);
                }while (!isCancelled() && mProgressValue >= 0 && mProgressValue < 100);
                return (mProgressValue > 0);
            }
        }
    }

//
//    public boolean doLoadTrackersData()
//    {
//        TrackerManager tManager = TrackerManager.getInstance();
//        ObjectTracker objectTracker = (ObjectTracker) tManager
//                .getTracker(ObjectTracker.getClassType());
//        if (objectTracker == null)
//            return false;
//
//        if (mCurrentDataset == null)
//            mCurrentDataset = objectTracker.createDataSet();
//
//        if (mCurrentDataset == null)
//            return false;
//
//        if (!mCurrentDataset.load(" ######## ObjectRecognition/rigidBodyTarget.xml  ####### EXAMPLE INSERT NEW ONE IN HERE",
//                STORAGE_TYPE.STORAGE_APPRESOURCE))
//            return false;
//
//        if (!objectTracker.activateDataSet(mCurrentDataset))
//            return false;
//
//        int numTrackables = mCurrentDataset.getNumTrackables();
//        for (int count = 0; count < numTrackables; count++)
//        {
//            Trackable trackable = mCurrentDataset.getTrackable(count);
//            if(isExtendedTrackingActive())
//            {
//                trackable.startExtendedTracking();
//            }
//
//            String name = "Current Dataset : " + trackable.getName();
//            trackable.setUserData(name);
//            Log.d("Object Recognition", "UserData:Set the following user data "
//                    + (String) trackable.getUserData());
//        }
//
//        return true;
//    }

//    public boolean isExtendedTrackingActive(){
//        return extendedTracking;
//    }

//    public void setUp(UsbDevice device) {
//        UsbDeviceConnection usb = usbManager.openDevice(device);
//        //  fileDescriptor = usbManager.openAccessory(device);
//        if (fileDescriptor != null) {
//            iS = new FileInputStream(fileDescriptor.getFileDescriptor());
//            oS = new FileOutputStream(fileDescriptor.getFileDescriptor());
//        }
//    }

    public void setIsWifiP2pEnabled(Boolean value)
    {
        isWifiP2pEnabled = value;
    }

    public boolean getIsWifiP2pEnabled(){
        return isWifiP2pEnabled;
    }
    public void resetData(){

    }


}