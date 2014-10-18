package com.baid.pbus.pebblebus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.*;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;


public class MyActivity extends Activity implements View.OnClickListener{

    TextView message;
    Button launch, hello;
    boolean connected, messageSupport;
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("0902a0a7-ca40-4299-8fcc-abb641ee0007");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        connected = PebbleKit.isWatchConnected(getApplicationContext());

        message = (TextView) findViewById(R.id.message);
        launch = (Button) findViewById(R.id.launch);
        launch.setOnClickListener(this);

        hello = (Button) findViewById(R.id.hello);
        hello.setOnClickListener(this);

        if (PebbleKit.areAppMessagesSupported(getApplicationContext())) {

            Log.i(getLocalClassName(), "App Message is supported!");
            messageSupport = true;
        }
        else{

            Log.i(getLocalClassName(), "App Message is NOT supported!");
            messageSupport = false;
        }



        if(connected)
            message.setText("Connected!");
        else
            message.setText("Not connected");

        //notifies us when pebble connects or disconnects
        PebbleKit.registerPebbleConnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i(getLocalClassName(), "Pebble connected!");
                alertMessage("Pebble Connected");
            }
        });

        PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i(getLocalClassName(), "Pebble disconnected!");
                alertMessage("Pebble disconnected");
            }
        });


        //keeps track of when pebble has ack or nack message
        PebbleKit.registerReceivedAckHandler(getApplicationContext(), new PebbleKit.PebbleAckReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveAck(Context context, int transactionId) {
                Log.i(getLocalClassName(), "Received ack for transaction " + transactionId);
                alertMessage("Ack!");
            }
        });

        PebbleKit.registerReceivedNackHandler(getApplicationContext(), new PebbleKit.PebbleNackReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveNack(Context context, int transactionId) {
                Log.i(getLocalClassName(), "Received nack for transaction " + transactionId);
                alertMessage("Nack!");
            }
        });

        //handle messages from Pebble
        final Handler handler = new Handler();
        PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                Log.i(getLocalClassName(), "Received value=" + data.getUnsignedInteger(0) + " for key: 0");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
          /* Update your UI here. */
                    }
                });
                PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
            }
        });

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
        else if(id == hello.getId() && messageSupport){

            PebbleDictionary data = new PebbleDictionary();
            data.addString(1, "Hello World!");
            PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, data);
        }
    }

    private void alertMessage(String m){

        AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext()).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(m);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();

    }


}
