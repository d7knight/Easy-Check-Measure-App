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
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.david.distoapp.BluetoothLeService;
import com.example.david.distoapp.FractionNum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class CreateNewCheckMeasureActivity extends Activity {
    private final String TAG = CreateNewCheckMeasureActivity.class.getSimpleName();
    public static int checkMeasureId;
    static public DatabaseHandler databaseHandler;
    ArrayAdapter<CharSequence> control_adapter, mount_adapter;
    ArrayAdapter<CharSequence> measurement_list_adapter;
    ArrayList<Integer> measurement_keys;
    EditText field_company, field_customer, field_address;
    Date currentTime = Calendar.getInstance().getTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_check_measure);

        databaseHandler = new DatabaseHandler(this);
        checkMeasureId = databaseHandler.getCheckMeasureCount() + 1;

        initializeViewObjects();
        updateArrayAdapter();
    }

    private void initializeViewObjects() {
        control_adapter = ArrayAdapter.createFromResource(this,
                R.array.control_array, android.R.layout.simple_spinner_item);
        mount_adapter = ArrayAdapter.createFromResource(this,
                R.array.mount_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        control_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mount_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        field_company = (EditText) findViewById(R.id.company);
        field_customer = (EditText) findViewById(R.id.customer);
        field_address = (EditText) findViewById(R.id.address);
        TextWatcher myTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                databaseHandler.updateCheckMeasure(checkMeasureId, field_address.getText().toString(), field_company.getText().toString(),
                        field_customer.getText().toString(), currentTime);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        field_company.addTextChangedListener(myTextWatcher);
        field_customer.addTextChangedListener(myTextWatcher);
        field_address.addTextChangedListener(myTextWatcher);

        measurement_keys = new ArrayList<Integer>();
        measurement_list_adapter = new ArrayAdapter<CharSequence>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<CharSequence>());
        ListView measurementList = (ListView) findViewById(R.id.measurement_list);
        measurementList.setAdapter(measurement_list_adapter);
        measurementList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                addOrEditMeasurement(measurement_keys.get(i));
            }
        });
        ((TextView)findViewById(R.id.checkmeasure_id_text)).setText("Check Measure #" + checkMeasureId);
    }

    public void sendCheckMeasure(View v) {
        if (field_company.getText() == null || field_customer.getText() == null || field_company.getText().toString().trim().equals("") || field_customer.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Error: Please enter text in Company and Customer fields", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, field_company.getText() + "," + field_customer.getText() + "," + field_address.getText());
            Intent i = new Intent(this, SendCheckMeasureActivity.class);
            startActivity(i);
        }
    }

    public void addOrEditMeasurement(final Integer measurement_id) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.measurement_dialog, null);
        final AlertDialog dialog = dialogBuilder.setView(dialogView).setTitle(measurement_id == null ? "Add measurement" : "Edit measurement").create();

        final EditText m_diag_width, m_diag_length, m_diag_room, m_diag_notes;
        m_diag_width = (EditText) dialogView.findViewById(R.id.m_diag_width);
        m_diag_length = (EditText) dialogView.findViewById(R.id.m_diag_length);
        m_diag_room = (EditText) dialogView.findViewById(R.id.m_diag_room);
        m_diag_notes = (EditText) dialogView.findViewById(R.id.m_diag_notes);

        final Spinner m_diag_mount, m_diag_control;
        m_diag_control = (Spinner) dialogView.findViewById(R.id.m_diag_control);
        m_diag_mount = (Spinner) dialogView.findViewById(R.id.m_diag_mount);

        m_diag_mount.setAdapter(mount_adapter);
        m_diag_control.setAdapter(control_adapter);

        if (measurement_id != null) {
            //restore old content
            ArrayList<ArrayList<String>> mlist = databaseHandler.getSingleMeasurement(checkMeasureId,
                    measurement_id);
            ArrayList<String> single = mlist.get(0);

            //String query = "SELECT " + WIDTH + "," + LENGTH + "," + ROOM + "," + CONTROL + "," + MOUNT+ "," + SPECIAL_NOTE + "," + KEY_MEASUREMENT_ID + " " +
            m_diag_width.setText(single.get(0));
            m_diag_length.setText(single.get(1));
            m_diag_room.setText(single.get(2));
            m_diag_control.setSelection(control_adapter.getPosition(single.get(3)));
            m_diag_mount.setSelection(mount_adapter.getPosition(single.get(4)));
            m_diag_notes.setText(single.get(5));

            Button delete = (Button) dialogView.findViewById(R.id.measurement_dialog_delete);
            delete.setOnClickListener(new View.OnClickListener() {
                private int counter = 1;

                @Override
                public void onClick(View v) {
                    if (counter > 0) {
                        Toast.makeText(v.getContext(), "Are you sure you would like to delete this measurement? Press DELETE again to confirm.", Toast.LENGTH_SHORT).show();
                    } else {
                        databaseHandler.deleteMeasurement(checkMeasureId, measurement_id);
                        dialog.dismiss();
                        updateArrayAdapter();
                    }
                    counter--;
                }
            });
            delete.setVisibility(View.VISIBLE);
        }

        Button confirm = (Button) dialogView.findViewById(R.id.measurement_dialog_confirm);
        Button cancel = (Button) dialogView.findViewById(R.id.measurement_dialog_cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("MAK", "Editing element: " + measurement_id);
                String width, length, room;
                width = m_diag_width.getText().toString();
                length = m_diag_length.getText().toString();
                room = m_diag_room.getText().toString();

                if (width == null || length == null || room == null || width.trim().equals("") || length.trim().equals("") || room.trim().equals("")) {
                    Toast.makeText(v.getContext(), "Error: Please enter text in Width, Length, and Room fields", Toast.LENGTH_LONG).show();
                    return;
                }
                if (measurement_id != null) {
                    databaseHandler.updateMeasurement(checkMeasureId, measurement_id, width, length, room, m_diag_control.getSelectedItem().toString(),
                            m_diag_mount.getSelectedItem().toString(), m_diag_notes.getText().toString());
                } else {
                    databaseHandler.createMeasurement(checkMeasureId, width, length, room, m_diag_control.getSelectedItem().toString(),
                            m_diag_mount.getSelectedItem().toString(), m_diag_notes.getText().toString());
                }
                dialog.dismiss();
                updateArrayAdapter();
            }
        });
        dialog.show();
    }

    public void addMeasurement(View v) {
        addOrEditMeasurement(null);
    }

    private void updateArrayAdapter() {

        measurement_list_adapter.clear();
        measurement_keys.clear();
        Log.e("MAK", "List adapter cleared. " + measurement_list_adapter.isEmpty());
        ArrayList<ArrayList<String>> measurementList = databaseHandler.getAllMeasurements(checkMeasureId);
        for (ArrayList<String> row : measurementList) {
            //String query = "SELECT " + WIDTH + "," + LENGTH + "," + ROOM + "," + CONTROL + "," + MOUNT+ "," + SPECIAL_NOTE + "," + KEY_MEASUREMENT_ID + " " +

            String room, mount, width, length, control, special_note;
            width = row.get(0).toUpperCase();
            if (width.contains("/")) {
                try {
                    String[] fraction = width.split("/");
                    String[] wholeNum = fraction[0].split(" ");
                    width = wholeNum[0] + " <sup>" + wholeNum[1] + "</sup>" + "/" + "<sub>" + fraction[1] + "</sub> ";
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, "Fraction formatting failed, but eh - whatever.");
                }
            }
            length = row.get(1).toUpperCase();
            if (length.contains("/")) {
                try {
                    String[] fraction = length.split("/");
                    String[] wholeNum = fraction[0].split(" ");
                    length = wholeNum[0] + " <sup>" + wholeNum[1] + "</sup>" + "/" + "<sub>" + fraction[1] + "</sub> ";
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, "Fraction formatting failed, but eh - whatever.");
                }
            }
            room = row.get(2).toUpperCase();
            control = row.get(3).toUpperCase();
            mount = row.get(4).toUpperCase();

            measurement_keys.add(Integer.parseInt(row.get(6)));
            String formattedString = room + ": " + width + "x" + length + " " + control + " " + mount;

            Log.e("MAK", "Updating: " + formattedString);
            measurement_list_adapter.add(Html.fromHtml(formattedString));
        }
        TextView measurementCountView = (TextView)findViewById(R.id.measurement_counter);
        if (measurement_list_adapter.getCount() == 0){
            measurementCountView.setText("No measurements added.\n\nAfter clicking Add, your measurements will show up here.");
        } else if (measurement_list_adapter.getCount() == 1) {
            measurementCountView.setText(measurement_list_adapter.getCount()+" measurement added so far:");
        }

        else{
            measurementCountView.setText(measurement_list_adapter.getCount()+" measurements added so far:");
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
        if (id == R.id.action_clear) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete check measure").setMessage("Are you sure you would like to delete all data?").setPositiveButton("Yes, delete everything", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    databaseHandler.deleteCheckMeasure(checkMeasureId);
                    field_company.setText("");
                    field_customer.setText("");
                    field_address.setText("");
                    updateArrayAdapter();
                    dialog.dismiss();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }
        return false;
    }

    /*
    EditText distoInsertion;
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
                    String newMeasurement = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                    FractionNum fnum = new FractionNum(Double.parseDouble(newMeasurement));
                    if (dialog != null && dialog.isShowing()) {
                        final TextView first, second, third, minimum;
                        // set the custom dialog components - text, image and button
                        first = (TextView) dialog.findViewById(R.id.first);
                        second = (TextView) dialog.findViewById(R.id.second);
                        third = (TextView) dialog.findViewById(R.id.third);
                        minimum = (TextView) dialog.findViewById(R.id.minimum);
                        if (first.getText().toString().equals("")) {
                            first.setText(fnum.str());
                        } else if (second.getText().toString().equals("")) {
                            second.setText(fnum.str());
                        } else {
                            if (!third.getText().toString().equals("")) {
                                Toast.makeText(getApplicationContext(), "Replaced Third Measurement with New Measurement", Toast.LENGTH_SHORT).show();
                            }
                            third.setText(fnum.str());


                            Double firstMeasurement, secondMeasurement, thirdMeasurement;
                            firstMeasurement = (new FractionNum(first.getText().toString())).getFullnumber();
                            secondMeasurement = (new FractionNum(second.getText().toString())).getFullnumber();
                            thirdMeasurement = (new FractionNum(third.getText().toString())).getFullnumber();


                            if (firstMeasurement < secondMeasurement) {
                                if (firstMeasurement < thirdMeasurement) {
                                    minimum.setText(first.getText().toString());
                                } else {
                                    minimum.setText(third.getText().toString());
                                }
                            } else {
                                if (secondMeasurement < thirdMeasurement) {
                                    minimum.setText(second.getText().toString());
                                } else {
                                    minimum.setText(third.getText().toString());
                                }

                            }
                        }
                    } else {
                        insertMeasurement(fnum.str());
                    }

                }
            }
        };

        private void insertMeasurement(String measurement) {
            if (distoInsertion.equals(dc_length)) {
                if (!distoInsertion.getText().toString().equals("")) {
                    Log.d(TAG, distoInsertion.getText().toString());

                    //newMeasurement();
                    distoInsertion = dc_width;
                    insertMeasurement(measurement);
                    return;


                }
                distoInsertion.setText(measurement);
                distoInsertion = dc_width;
                Toast.makeText(getApplicationContext(), "Inserted in length ...", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Inserted in length " + measurement);


            } else {
                if (!distoInsertion.getText().toString().equals("")) {
                    Log.d(TAG, distoInsertion.getText().toString());

                    distoInsertion = dc_length;
                    insertMeasurement(measurement);
                    return;


                }

                distoInsertion.setText(measurement);
                distoInsertion = dc_length;
                Toast.makeText(getApplicationContext(), "Inserted in width ...", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Inserted in width " + measurement);
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
                if (uuid.equals(BluetoothLeService.DISTO_SERVICE)) {
                    // Loops through available Characteristics.
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        uuid = gattCharacteristic.getUuid().toString();
                        if (uuid.equals(BluetoothLeService.DISTO_CHARACTERISTIC_DISTANCE)) {
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
                    Toast.makeText(getApplicationContext(), "Connection Established to " + mDeviceName, Toast.LENGTH_SHORT).show();
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
    */

/*
    public void testDB() {

        databaseHandler.deleteCheckMeasure(1);
        databaseHandler.deleteCheckMeasure(2);
        databaseHandler.deleteMeasurement(1, 1);
        databaseHandler.deleteMeasurement(1, 2);
        databaseHandler.deleteMeasurement(1, 3);
        databaseHandler.deleteMeasurement(1, 4);

    // Inserting Contacts
    Log.d("Insert: ", "Inserting ..");
    databaseHandler.addCheckMeasure(1, "toronto", "bickels", "michael", Calendar.getInstance().getTime());
    databaseHandler.addCheckMeasure(2, "toronto2", "bickels2", "michael2", Calendar.getInstance().getTime());
    databaseHandler.createMeasurement(1, "55 1/4", "55 1/8", "master", "left", "IB", "testing123");
    databaseHandler.createMeasurement(1, "56 1/4", "55 1/8", "master", "left", "IB", "testing123");
    databaseHandler.createMeasurement(1, "57 1/4", "55 1/8", "master", "left", "IB", "testing123");
    databaseHandler.createMeasurement(1, "58 1/4", "55 1/8", "master", "left", "IB", "testing123");

    databaseHandler.getCheckMeasureCount();

    Log.d(TAG, "Reading all cms..");
    Log.d(TAG, "cm count=" + String.valueOf(databaseHandler.getCheckMeasureCount()));
    Log.d(TAG, "cm 1 measurement count=" + String.valueOf(databaseHandler.getMeasurementCount(1)));
    Log.d(TAG, "cm 2 measurement count=" + String.valueOf(databaseHandler.getMeasurementCount(2)));
    for (ArrayList<String> l : databaseHandler.getAllCheckMeasures()) {
        String line = "";
        for (String s : l) {
            line += s;
            line += ", ";
        }
        Log.d(TAG, line);
    }

    for (ArrayList<String> l : databaseHandler.getAllMeasurements(1)) {
        String line = "";
        for (String s : l) {
            line += s;
            line += ", ";
        }
        Log.d(TAG, "for cm 1: " + line);
    }
}
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.send) {
            databaseHandler.addMeasurement(checkMeasureId, measurement_id, dc_width.getText().toString(), dc_length.getText().toString(),
                    dc_room.getText().toString(), control.getSelectedItem().toString(),
                    mount.getSelectedItem().toString(), dc_special_note.getText().toString());
            Intent i = new Intent(this, SendCheckMeasureActivity.class);
            startActivity(i);


        } else if (id == R.id.disto_settings) {
            Intent intent = new Intent(CreateNewCheckMeasureActivity.this, DeviceScanActivity.class);
            startActivityForResult(intent, 2);// Activity is started with requestCode 2
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            return true;
        } else if (id == R.id.trigger_measurement) {
            if (mConnected == false) {
                Toast.makeText(getApplicationContext(), "Please connect to your Disto Device first " +
                        "before doing this operation", Toast.LENGTH_SHORT).show();
                return false;
            }
            mBluetoothLeService.distoTakeMeasurement();
            return true;

        } else if (id == R.id.trigger_3measurements) {
            if (mConnected == false) {
                Toast.makeText(getApplicationContext(), "Please connect to your Disto Device first " +
                        "before doing this operation", Toast.LENGTH_SHORT).show();

                return false;
            }

            LayoutInflater inflater = LayoutInflater.from(CreateNewCheckMeasureActivity.this);
            final View customView = inflater.inflate(R.layout.dialog_three_measurement_mode, null);

            final TextView first, second, third, minimum;
            final Button redo, trigger;
            first = (TextView) customView.findViewById(R.id.first);
            second = (TextView) customView.findViewById(R.id.second);
            third = (TextView) customView.findViewById(R.id.third);
            minimum = (TextView) customView.findViewById(R.id.minimum);
            redo = (Button) customView.findViewById(R.id.redo);
            trigger = (Button) customView.findViewById(R.id.trigger);


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
        } else if (id == R.id.view) {
            databaseHandler.addMeasurement(checkMeasureId, measurement_id, dc_width.getText().toString(), dc_length.getText().toString(),
                    dc_room.getText().toString(), control.getSelectedItem().toString(),
                    mount.getSelectedItem().toString(), dc_special_note.getText().toString());
            Intent i = new Intent(this, ViewOldCheckMeasuresActivity.class);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 2) {
            mDeviceName = data.getStringExtra(EXTRAS_DEVICE_NAME);
            mDeviceAddress = data.getStringExtra(EXTRAS_DEVICE_ADDRESS);


        }

    }
*/
/*
    public void nextMeasurement(View v) {

        databaseHandler.addMeasurement(checkMeasureId, measurement_id, width.getText().toString(), length.getText().toString(),
                dc_room.getText().toString(), control.getSelectedItem().toString(),
                mount.getSelectedItem().toString(), special_note.getText().toString());
        measurement_id++;
        width.setText("");
        length.setText("");
        dc_room.setText("");
        special_note.setText("");
        control.setSelection(0);
        mount.setSelection(0);
        if (databaseHandler.getMeasurementCount(checkMeasureId) >= measurement_id) {
            //WIDTH+","+LENGTH+","+ROOM+","+CONTROL+","+MOUNT+","+ SPECIAL_NOTE+
            ArrayList<String> data = databaseHandler.getSingleMeasurement(checkMeasureId, measurement_id).get(0);
            width.setText(data.get(0));
            length.setText(data.get(1));
            dc_room.setText(data.get(2));
            control.setSelection(control_adapter.getPosition(data.get(3)));
            mount.setSelection(mount_adapter.getPosition(data.get(4)));
            special_note.setText(data.get(5));
        }

    }

    public void prevMeasurement(View v) {
        if (measurement_id != 1) {

            databaseHandler.addMeasurement(checkMeasureId, measurement_id, width.getText().toString(), length.getText().toString(),
                    dc_room.getText().toString(), control.getSelectedItem().toString(),
                    mount.getSelectedItem().toString(), special_note.getText().toString());
            measurement_id--;
            ArrayList<String> data = databaseHandler.getSingleMeasurement(checkMeasureId, measurement_id).get(0);
            width.setText(data.get(0));
            length.setText(data.get(1));
            dc_room.setText(data.get(2));
            control.setSelection(control_adapter.getPosition(data.get(3)));
            mount.setSelection(mount_adapter.getPosition(data.get(4)));
            special_note.setText(data.get(5));
        }
    }

    public void newMeasurement() {
        databaseHandler.addMeasurement(checkMeasureId, measurement_id, width.getText().toString(), length.getText().toString(),
                dc_room.getText().toString(), control.getSelectedItem().toString(),
                mount.getSelectedItem().toString(), special_note.getText().toString());
        width.setText("");
        length.setText("");
        dc_room.setText("");
        special_note.setText("");
        control.setSelection(0);
        mount.setSelection(0);
        int totalMeasurements = databaseHandler.getMeasurementCount(checkMeasureId);
        measurement_id = totalMeasurements + 1;
    }
    */
}
