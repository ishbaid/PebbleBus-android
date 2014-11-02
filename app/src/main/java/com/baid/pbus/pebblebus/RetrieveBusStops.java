package com.baid.pbus.pebblebus;

/**
 * Created by Ish on 10/23/14.
 */

import android.os.AsyncTask;
import android.util.Log;

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
import java.util.HashMap;

/**
 * Created by Ish on 10/18/14.
 *
 * This class retrieves ALL bus stops
 */
public class RetrieveBusStops extends AsyncTask<String, String, Void> {

    InputStream inputStream = null;
    String result = "";

    ArrayList<String> stopNames;
    HashMap<String, Integer> stopMap;

    OnTaskCompleted listener;

    public RetrieveBusStops(OnTaskCompleted l){

        listener = l;
        stopNames = new ArrayList<String>();
        stopMap = new HashMap<String, Integer>();

    }

    public ArrayList<String> getStopNames(){

        return stopNames;
    }

    //maps stop Ids to stop names
    public HashMap <String, Integer> getStopMap(){

        return stopMap;
    }

    @Override
    protected Void doInBackground(String... params) {

        String url_select = "http://mbus.doublemap.com/map/v2/stops";

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

            Log.d("Baid", "Json result is " + result);

            JSONArray jArray = new JSONArray(result);

            for(int i = 0; i < jArray.length(); i++) {

                JSONObject jObject = jArray.getJSONObject(i);
                String bName = jObject.getString("name");
                int id = jObject.getInt("id");

                stopNames.add(bName);
                stopMap.put(bName, id);


            } // End Loop

            Log.d("Baid", "Number of stops: " + stopNames.size());


        } catch (JSONException e) {
            Log.e("JSONException", "Error: " + e.toString());
        } // catch (JSONException e)


        //callback
        listener.onTask1Completed();

    } // protected void onPostExecute(Void v)

}//asynctask



