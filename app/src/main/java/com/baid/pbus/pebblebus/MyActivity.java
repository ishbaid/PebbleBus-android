package com.baid.pbus.pebblebus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;


public class MyActivity extends Activity implements View.OnClickListener{

    TextView message;
    Button launch, hello, request;
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

        request = (Button) findViewById(R.id.request);
        request.setOnClickListener(this);

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
        else if(id == request.getId()){

            getBusInfo();
        }
    }

    private void getBusInfo(){

        new parseBusInfo().execute();
        //alertMessage("Done");
    }

    class parseBusInfo extends AsyncTask<String, String, Void> {

        InputStream inputStream = null;
        String result = "";

        @Override
        protected Void doInBackground(String... params) {

            String url_select = "http://mbus.doublemap.com/map/v2/buses";

            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

            try {
                // Set up HTTP post

                // HttpClient is more then less deprecated. Need to change to URLConnection
                HttpClient httpClient = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost(url_select);
                httpPost.setEntity(new UrlEncodedFormEntity(param));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();

                // Read content & Log
                inputStream = httpEntity.getContent();
            } catch (UnsupportedEncodingException e1) {
                Log.e("UnsupportedEncodingException", e1.toString());
                e1.printStackTrace();
            } catch (ClientProtocolException e2) {
                Log.e("ClientProtocolException", e2.toString());
                e2.printStackTrace();
            } catch (IllegalStateException e3) {
                Log.e("IllegalStateException", e3.toString());
                e3.printStackTrace();
            } catch (IOException e4) {
                Log.e("IOException", e4.toString());
                e4.printStackTrace();
            }
            // Convert response to string using String Builder
            try {
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
                StringBuilder sBuilder = new StringBuilder();

                String line = null;
                while ((line = bReader.readLine()) != null) {
                    sBuilder.append(line + "\n");
                }

                inputStream.close();
                result = sBuilder.toString();

            } catch (Exception e) {
                Log.e("StringBuilding & BufferedReader", "Error converting result " + e.toString());
            }
           return null;
        } // protected Void doInBackground(String... params)

        protected void onPostExecute(Void v) {
            //parse JSON data
            try {
                JSONArray jArray = new JSONArray(result);
                for(int i = 0; i < jArray.length(); i++) {

                    JSONObject jObject = jArray.getJSONObject(i);
                    Log.d("Baid", jObject.toString());

                } // End Loop

            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)
        } // protected void onPostExecute(Void v)

    }//asynctask

    private void alertMessage(String m){

        AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext()).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(m);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();

    }


}
