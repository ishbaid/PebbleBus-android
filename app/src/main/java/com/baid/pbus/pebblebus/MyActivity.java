package com.baid.pbus.pebblebus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


public class MyActivity extends Activity implements View.OnClickListener, OnTaskCompleted{

    TextView message;
    Button launch, hello, request;
    EditText results;

    boolean connected, messageSupport;
    static boolean locationKnown;
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("0902a0a7-ca40-4299-8fcc-abb641ee0007");


    static RetrieveStops rs;
    HashMap<Integer, String> routeMap;
    HashMap<Integer, Integer> indicies;
    static ETA eta;

    ArrayList<PebbleDictionary> queue;
    int curIndex = 0;
    int QUEUE_SIZE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        connected = PebbleKit.isWatchConnected(getApplicationContext());

        message = (TextView) findViewById(R.id.message);
        launch = (Button) findViewById(R.id.launch);
        launch.setOnClickListener(this);


        request = (Button) findViewById(R.id.request);
        request.setOnClickListener(this);

        results = (EditText) findViewById(R.id.results);


        if (PebbleKit.areAppMessagesSupported(getApplicationContext())) {

            Log.i(getLocalClassName(), "App Message is supported!");
            messageSupport = true;
        }
        else{

            Log.i(getLocalClassName(), "App Message is NOT supported!");
            messageSupport = false;
        }


        //notifies us when pebble connects or disconnects
        PebbleKit.registerPebbleConnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i(getLocalClassName(), "Pebble connected!");
                //alertMessage("Pebble Connected");
            }
        });

        PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i(getLocalClassName(), "Pebble disconnected!");
                //alertMessage("Pebble disconnected");
            }
        });


        //keeps track of when pebble has ack or nack message
        PebbleKit.registerReceivedAckHandler(getApplicationContext(), new PebbleKit.PebbleAckReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveAck(Context context, int transactionId) {
                Log.i(getLocalClassName(), "Received ack for transaction " + transactionId);

                sendData();
            }
        });

        PebbleKit.registerReceivedNackHandler(getApplicationContext(), new PebbleKit.PebbleNackReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveNack(Context context, int transactionId) {
                Log.i(getLocalClassName(), "Received nack for transaction " + transactionId);

                AlertDialog alertDialog = new AlertDialog.Builder(MyActivity.this).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Location not available");
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();
            }
        });

        //handle messages from Pebble
        final Handler handler = new Handler();
        PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                Log.i(getLocalClassName(), "Received value=" + data.getUnsignedInteger(0) + " for key, 0");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
          /* Update your UI here. */
                    }
                });
                PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
            }
        });

        initializeRouteMap();

    }

    private void initializeRouteMap(){

        //STORE locally
        routeMap = new HashMap<Integer, String>();
        routeMap.put(0,  "Comm.S.");
        routeMap.put(1,  "Comm.N.");
        routeMap.put(2,  "Northwood");
        routeMap.put(68, "Bursley-Baits");//weekend
        routeMap.put(69, "Bursley-Baits");//night
        routeMap.put(72, "Int.E.C.");
        routeMap.put(73, "Int.NIB");
        routeMap.put(75, "Mitchell-Glazier");
        routeMap.put(78, "KMS Shuttle");
        routeMap.put(87, "Oxford");
        routeMap.put(92, "Diag-Diag");
        routeMap.put(102, "Com.N.(Ni)");
        routeMap.put(107, "Oxford");
        routeMap.put(192, "Northwood");
        routeMap.put(193, "North Campus");
        routeMap.put(197, "Night Owl");
        routeMap.put(198, "Bursley-Baits");
        routeMap.put(199, "Northwood");
        routeMap.put(200, "Oxford");

        indicies = new HashMap<Integer, Integer>();
        indicies.put(0, 0);
        indicies.put(1,  1);
        indicies.put(2,  2);
        indicies.put(68, 3);//weekend
        indicies.put(69, 4);//night
        indicies.put(72, 5);
        indicies.put(73, 6);
        indicies.put(75, 7);
        indicies.put(78, 8);
        indicies.put(87, 9);
        indicies.put(92, 10);
        indicies.put(102, 11);
        indicies.put(107, 12);
        indicies.put(192, 13);
        indicies.put(193, 14);
        indicies.put(197, 15);
        indicies.put(198, 16);
        indicies.put(199, 17);
        indicies.put(200, 18);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if(id == launch.getId()){

            if(connected)
                PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
        }

        else if(id == request.getId()){

            getNearestStop();

        }
    }

    private void getNearestStop(){

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(location == null){

            AlertDialog alertDialog = new AlertDialog.Builder(MyActivity.this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Location not available");
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();

            locationKnown = false;
            return;
        }
        else{

            locationKnown = true;
        }

        //Change later
        //CC-LITTLE
        double longitude = -83.73494;//location.getLongitude();
        double latitude = 42.277683;//location.getLatitude();

        rs = new RetrieveStops(latitude, longitude, this);
        rs.execute();

    }

    private void alertMessage(String m){

        AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext()).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(m);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();

    }


    @Override
    public void onTask1Completed() {

        //if we don't know location, we cannot get an ETA
        if(!locationKnown)
            return;
        int id = rs.getId();

        eta = new ETA(id, this);
        eta.execute();

        message.setText(rs.getName());
        Log.d("Baid", "Done!");
    }

    //sends data to Pebble
    @Override
    public void onTask2Completed() {

       ArrayList<Integer> busIDs = eta.getBusIDs();
        ArrayList<Integer> expTime = eta.getExpTimes();

        assert (busIDs.size() == expTime.size());

        Log.d("Baid", "Buses: " + busIDs.size());

        QUEUE_SIZE  = 4;

        //Name
        PebbleDictionary name = new PebbleDictionary();
        name.addString(0, rs.getName());

        //Size
        PebbleDictionary size = new PebbleDictionary();
        size.addInt32(0, busIDs.size());

        PebbleDictionary schedule = new PebbleDictionary();

        PebbleDictionary bus = new PebbleDictionary();
//
        for(int i = 0; i < busIDs.size(); i ++){

            int avg = expTime.get(i);
            Integer rID = busIDs.get(i);
            if(routeMap == null)
                Log.d("Baid", "rm is null");

            if(rID != null && routeMap.containsKey(rID)) {

                String rName = routeMap.get(rID);
                results.setText(results.getText() + rName + ": " + avg + " minutes\n");


               // schedule.addInt32(i, avg);
                Log.d("Baid", "Avg is " + avg);

                int index = indicies.get(rID);
                //bus.addInt32(i, index);
                //Log.d("Baid", "Index is " + index);

            }
            else{

                Log.d("Baid", "Not selected: " + avg);
            }

        }

        schedule.addInt32(0, 1);
        schedule.addInt32(1, 2);
        schedule.addInt32(2, 3);

        bus.addInt32(0, 10);
        bus.addInt32(1, 15);
        bus.addInt32(2, 18);
        bus.addInt32(3, 1);
        bus.addInt32(4, 4);

        //Creates queue
        queue = new ArrayList<PebbleDictionary>();
        queue.add(name);
        queue.add(size);
        queue.add(schedule);
        queue.add(bus);



        sendData();


    }

    private void sendData(){

        Log.d("Baid", "Sending data");
        if(connected && messageSupport && curIndex < QUEUE_SIZE){

            PebbleDictionary toSend = queue.get(curIndex);
            PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, toSend);

            curIndex ++;
        }


    }
}
