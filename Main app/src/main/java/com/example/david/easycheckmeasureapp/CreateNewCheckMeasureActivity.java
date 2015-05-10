package com.example.david.easycheckmeasureapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.david.distoapp.BluetoothLeService;
import com.example.david.distoapp.DeviceScanActivity;
import com.example.david.distoapp.FractionNum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class CreateNewCheckMeasureActivity extends Activity {
    private final String TAG = CreateNewCheckMeasureActivity.class.getSimpleName();
    public static int cm_id;
    public int measurement_id;
    static public DatabaseHandler db;
    Spinner control, mount;
    ArrayAdapter<CharSequence> control_adapter,mount_adapter;
    EditText company,customer,address,room,width,length,special_note;
    EditText distoInsertion;
    Date currentTime=Calendar.getInstance().getTime();
    AlertDialog dialog;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";



    private String mDeviceAddress;
    private String mDeviceName;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // Code to manage Service lifecycle.
    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                displayNotification(com.example.david.distoapp.R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                displayNotification(com.example.david.distoapp.R.string.disconnected);
                invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                initializeGattServices(mBluetoothLeService.getSupportedGattServices());

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String newMeasurement=intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                FractionNum fnum=new FractionNum(Double.parseDouble(newMeasurement));
                if(dialog!=null&&dialog.isShowing()){
                    final TextView first,second,third,minimum;
                    // set the custom dialog components - text, image and button
                    first=(TextView)dialog.findViewById(R.id.first);
                    second=(TextView)dialog.findViewById(R.id.second);
                    third=(TextView)dialog.findViewById(R.id.third);
                    minimum=(TextView)dialog.findViewById(R.id.minimum);
                 if(first.getText().toString().equals("")){
                     first.setText(fnum.str());
                 }
                 else if(second.getText().toString().equals("")){
                     second.setText(fnum.str());
                 }
                 else{
                     if(!third.getText().toString().equals("")){
                         Toast.makeText(getApplicationContext(),"Replaced Third Measurement with New Measurement", Toast.LENGTH_SHORT).show();
                     }
                     third.setText(fnum.str());


                     Double firstMeasurement,secondMeasurement,thirdMeasurement;
                     firstMeasurement=(new FractionNum(first.getText().toString())).getFullnumber();
                     secondMeasurement=(new FractionNum(second.getText().toString())).getFullnumber();
                     thirdMeasurement=(new FractionNum(third.getText().toString())).getFullnumber();



                     if(firstMeasurement<secondMeasurement){
                         if(firstMeasurement<thirdMeasurement){
                             minimum.setText(first.getText().toString());
                         }
                         else{
                             minimum.setText(third.getText().toString());
                         }
                     }
                     else{
                         if(secondMeasurement<thirdMeasurement){
                             minimum.setText(second.getText().toString());
                         }
                         else{
                             minimum.setText(third.getText().toString());
                         }

                     }
                 }
                }
                else {
                    insertMeasurement(fnum.str());
                }

            }
        }
    };

    private void insertMeasurement(String measurement) {
        if(distoInsertion.equals(length)){
            if(!distoInsertion.getText().toString().equals("")){
                Log.d(TAG,distoInsertion.getText().toString());

                newMeasurement();
                distoInsertion=width;
                insertMeasurement(measurement);
                return;


            }
            distoInsertion.setText(measurement);
            distoInsertion=width;
            Toast.makeText(getApplicationContext(), "Inserted in length ...",Toast.LENGTH_SHORT ).show();
            Log.d(TAG,"Inserted in length "+measurement);


        }
        else {
            if(!distoInsertion.getText().toString().equals("")){
                Log.d(TAG,distoInsertion.getText().toString());

                distoInsertion=length;
                insertMeasurement(measurement);
                return;


            }

            distoInsertion.setText(measurement);
            distoInsertion=length;
            Toast.makeText(getApplicationContext(), "Inserted in width ...",Toast.LENGTH_SHORT ).show();
            Log.d(TAG,"Inserted in width "+measurement);
        }
    }

    private void initializeGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;


        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();


            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            if(uuid.equals(BluetoothLeService.DISTO_SERVICE)) {
                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    if(uuid.equals(BluetoothLeService.DISTO_CHARACTERISTIC_DISTANCE)){
                        mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                    }

                }
            }

        }

    }

    private void displayNotification(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"Connection Established to "+mDeviceName,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_create_new_check_measure);
        Log.d(TAG, "On create called");


        db = new DatabaseHandler(this);
        cm_id=db.getCheckMeasureCount()+1;
        measurement_id=1;


        initializeViewObjects();



    }




    private void initializeViewObjects() {
        control=(Spinner)findViewById(R.id.spinner_control);
        mount=(Spinner)findViewById(R.id.spinner_mount);

        control_adapter = ArrayAdapter.createFromResource(this,
                R.array.control_array, android.R.layout.simple_spinner_item);
        mount_adapter = ArrayAdapter.createFromResource(this,
                R.array.mount_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        control_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mount_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mount.setAdapter(mount_adapter);
        control.setAdapter(control_adapter);
        company=(EditText)findViewById(R.id.company);
        customer=(EditText)findViewById(R.id.customer);
        address=(EditText)findViewById(R.id.address);
        room=(EditText)findViewById(R.id.room);
        width=(EditText)findViewById(R.id.width);
        length=(EditText)findViewById(R.id.length);
        special_note=(EditText) findViewById(R.id.special_note);
        TextWatcher myTextWatcher=new TextWatcher(){
            public void afterTextChanged(Editable s) {
                db.addCheckMeasure(cm_id, address.getText().toString(), company.getText().toString(),
                        customer.getText().toString(), currentTime);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        };
        company.addTextChangedListener(myTextWatcher);
        customer.addTextChangedListener(myTextWatcher);
        address.addTextChangedListener(myTextWatcher);

         control.setSelection(0);
         mount.setSelection(0);
        distoInsertion=width;
    }



    public void testDB(){

        db.deleteCheckMeasure(1);
        db.deleteCheckMeasure(2);
        db.deleteMeasurement(1,1);
        db.deleteMeasurement(1,2);
        db.deleteMeasurement(1,3);
        db.deleteMeasurement(1,4);


        /**
         * CRUD Operations
         * */
        // Inserting Contacts
        Log.d("Insert: ", "Inserting ..");
        db.addCheckMeasure(1, "toronto", "bickels", "michael", Calendar.getInstance().getTime());
        db.addCheckMeasure(2, "toronto2", "bickels2", "michael2", Calendar.getInstance().getTime());
        db.addMeasurement(1, 1, "55 1/4", "55 1/8", "master", "left", "IB", "testing123");
        db.addMeasurement(1, 2, "56 1/4", "55 1/8", "master", "left", "IB", "testing123");
        db.addMeasurement(1, 3, "57 1/4", "55 1/8", "master", "left", "IB", "testing123");
        db.addMeasurement(1, 4, "58 1/4", "55 1/8", "master", "left", "IB", "testing123");

        db.getCheckMeasureCount();

        Log.d(TAG, "Reading all cms..");
        Log.d(TAG, "cm count="+String.valueOf( db.getCheckMeasureCount()));
        Log.d(TAG, "cm 1 measurement count="+String.valueOf( db.getMeasurementCount(1)));
        Log.d(TAG, "cm 2 measurement count="+String.valueOf( db.getMeasurementCount(2)));
        for (ArrayList<String> l : db.getAllCheckMeasures()) {
            String line = "";
            for (String s : l) {
                line += s;
                line += ", ";
            }
            Log.d(TAG, line);
        }

        for (ArrayList<String> l : db.getAllMeasurements(1)) {
            String line = "";
            for (String s : l) {
                line += s;
                line += ", ";
            }
            Log.d(TAG,"for cm 1: "+ line);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_new_check_measure, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.send){
            db.addMeasurement(cm_id, measurement_id, width.getText().toString(), length.getText().toString(),
                    room.getText().toString(), control.getSelectedItem().toString(),
                    mount.getSelectedItem().toString(), special_note.getText().toString());
            Intent i = new Intent(this,SendCheckMeasureActivity.class);
            startActivity(i);


        }
        else if(id == R.id.disto_settings){
            Intent intent = new Intent(CreateNewCheckMeasureActivity.this,DeviceScanActivity.class);
            startActivityForResult(intent, 2);// Activity is started with requestCode 2
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            return true;
        }
        else if(id == R.id.trigger_measurement){
            if(mConnected==false){
                Toast.makeText(getApplicationContext(),"Please connect to your Disto Device first " +
                        "before doing this operation", Toast.LENGTH_SHORT).show();
                return false;
            }
            mBluetoothLeService.distoTakeMeasurement();
            return true;

        }
        else if(id == R.id.trigger_3measurements) {
            if (mConnected == false) {
                Toast.makeText(getApplicationContext(), "Please connect to your Disto Device first " +
                        "before doing this operation", Toast.LENGTH_SHORT).show();

                return false;
            }

            LayoutInflater inflater = LayoutInflater.from(CreateNewCheckMeasureActivity.this);
            final View customView = inflater.inflate(R.layout.dialog_three_measurement_mode, null);

            final TextView first,second,third,minimum;
            final Button redo, trigger;
            first = (TextView) customView.findViewById(R.id.first);
            second = (TextView) customView.findViewById(R.id.second);
            third = (TextView) customView.findViewById(R.id.third);
            minimum = (TextView) customView.findViewById(R.id.minimum);
            redo=(Button)customView.findViewById(R.id.redo);
            trigger=(Button)customView.findViewById(R.id.trigger);


            trigger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBluetoothLeService.distoTakeMeasurement();

                }
            });

            redo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    first.setText("");
                    second.setText("");
                    third.setText("");
                    minimum.setText("");
                    customView.invalidate();

                }
            });
            dialog = new AlertDialog.Builder(CreateNewCheckMeasureActivity.this)
                    .setTitle("Take Three Measurements")
                    .setView(customView)
                    .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        insertMeasurement(minimum.getText().toString());
                        }
                    }).create();
            dialog.show();






            return true;
        }
        else if(id == R.id.view){
            db.addMeasurement(cm_id, measurement_id, width.getText().toString(), length.getText().toString(),
                    room.getText().toString(), control.getSelectedItem().toString(),
                    mount.getSelectedItem().toString(), special_note.getText().toString());
            Intent i = new Intent(this,ViewOldCheckMeasuresActivity.class);
            startActivity(i);
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // check if the request code is same as what is passed  here it is 2
        if(requestCode==2)
        {
            mDeviceName = data.getStringExtra(EXTRAS_DEVICE_NAME);
            mDeviceAddress = data.getStringExtra(EXTRAS_DEVICE_ADDRESS);


        }

    }

    public void nextMeasurement(View v){

     db.addMeasurement(cm_id, measurement_id, width.getText().toString(), length.getText().toString(),
             room.getText().toString(), control.getSelectedItem().toString(),
             mount.getSelectedItem().toString(), special_note.getText().toString());
        measurement_id++;
     width.setText("");
     length.setText("");
     room.setText("");
     special_note.setText("");
     control.setSelection(0);
     mount.setSelection(0);
     if(db.getMeasurementCount(cm_id)>=measurement_id){
         //WIDTH+","+LENGTH+","+ROOM+","+CONTROL+","+MOUNT+","+ SPECIAL_NOTE+
         ArrayList<String> data=db.getSingleMeasurement(cm_id,measurement_id).get(0);
         width.setText(data.get(0));
         length.setText(data.get(1));
         room.setText(data.get(2));
         control.setSelection(control_adapter.getPosition(data.get(3)));
         mount.setSelection(mount_adapter.getPosition(data.get(4)));
         special_note.setText(data.get(5));
     }

    }

    public void prevMeasurement(View v){
     if(measurement_id!=1){

         db.addMeasurement(cm_id, measurement_id, width.getText().toString(), length.getText().toString(),
                 room.getText().toString(), control.getSelectedItem().toString(),
                 mount.getSelectedItem().toString(), special_note.getText().toString());
         measurement_id--;
         ArrayList<String> data=db.getSingleMeasurement(cm_id,measurement_id).get(0);
         width.setText(data.get(0));
         length.setText(data.get(1));
         room.setText(data.get(2));
         control.setSelection(control_adapter.getPosition(data.get(3)));
         mount.setSelection(mount_adapter.getPosition(data.get(4)));
         special_note.setText(data.get(5));
     }
    }
    public void newMeasurement(){
        db.addMeasurement(cm_id, measurement_id, width.getText().toString(), length.getText().toString(),
                room.getText().toString(), control.getSelectedItem().toString(),
                mount.getSelectedItem().toString(), special_note.getText().toString());
        width.setText("");
        length.setText("");
        room.setText("");
        special_note.setText("");
        control.setSelection(0);
        mount.setSelection(0);
        int totalMeasurements=db.getMeasurementCount(cm_id);
        measurement_id=totalMeasurements+1;
    }
}
