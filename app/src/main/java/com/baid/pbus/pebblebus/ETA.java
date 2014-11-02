package com.baid.pbus.pebblebus;

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

/**
 * Created by Ish on 10/18/14.
 */
class ETA extends AsyncTask<String, String, Void> {

    InputStream inputStream = null;
    String result = "";
    int routeID;
    ArrayList<Integer> busIDs;
    ArrayList<Integer> expTime;

    private OnTaskCompleted listener;

    public ETA(int rID, OnTaskCompleted l){

        routeID = rID;

        //keeps track of when asyctask is finished
        listener = l;
        //keeps track of near buses and arrival times
        busIDs = new ArrayList<Integer>();
        expTime = new ArrayList<Integer>();

    }

    public ArrayList<Integer> getBusIDs(){

        return busIDs;
    }

    public ArrayList<Integer> getExpTimes(){

        return expTime;
    }



    @Override
    protected Void doInBackground(String... params) {

        String url_select = "http://mbus.doublemap.com/map/v2/eta?stop=";
        url_select += routeID;

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

            //extracts JsonArray
            int start = result.indexOf("[");
            int end = result.indexOf("]");

            //ensures we retrieve jSonArray
            if(end != -1 && start != -1){

                if(end == result.length() - 1)
                    result.substring(start);
                else
                    result = result.substring(start, end + 1);
            }
            else{

                Log.d("Baid", routeID + " is route");
                Log.d("Baid", "JSON result: " + result);
                return;
            }



            JSONArray jArray = new JSONArray(result);



            for(int i = 0; i < jArray.length(); i++) {

                JSONObject jObject = jArray.getJSONObject(i);
                Log.d("Baid", jObject.toString());

                int id = jObject.getInt("route");
                int time = jObject.getInt("avg");
                String liveness = jObject.getString("type");

                    busIDs.add(id);
                    expTime.add(time);



            } // End Loop

        } catch (JSONException e) {
            Log.e("JSONException", "Error: " + e.toString());
        } // catch (JSONException e)

        //callback
        listener.onTask2Completed();

    } // protected void onPostExecute(Void v)

}//asynctask

