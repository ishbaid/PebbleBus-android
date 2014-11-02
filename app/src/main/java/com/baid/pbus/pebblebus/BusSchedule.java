package com.baid.pbus.pebblebus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ish on 10/23/14.
 */
public class BusSchedule extends Activity implements View.OnClickListener, OnTaskCompleted{


    TextView title;
    Spinner busStops;
    ListView arrivalTimes;
    Button closestStop;

    HashMap<Integer, String> routeMap;
    RetrieveBusStops rbs;
    ETA eta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_schedule);

        title = (TextView) findViewById(R.id.title);
        busStops = (Spinner) findViewById(R.id.bus_stops);
        arrivalTimes = (ListView) findViewById(R.id.arrival_times);

        closestStop = (Button) findViewById(R.id.closest_stop);
        closestStop.setOnClickListener(this);





        //TODO: STORE locally
        //keeps track of bus IDs associated with each bus
        routeMap = new HashMap<Integer, String>();


        routeMap.put(0,  "Commuter South");
        routeMap.put(1,  "Commuter North");
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
        routeMap.put(166, "Bursley-Baits");
        routeMap.put(167, "Bursley-Baits");
        routeMap.put(170, "Commuter North");
        routeMap.put(171, "Commuter North");
        routeMap.put(172, "Commuter South");
        routeMap.put(173, "Commuter South");
        routeMap.put(179, "Bio-Research Shuttle");
        routeMap.put(180, "MedExpress");
        routeMap.put(181, "MedExpress");
        routeMap.put(182, "North-East Shuttle");
        routeMap.put(183, "Wall Street - NIB");
        routeMap.put(184, "Wall Street Express");
        routeMap.put(185, "Wall Street Express");
        routeMap.put(186, "Northwood Express");
        routeMap.put(187, "Diag to Diag");
        routeMap.put(188, "Northwood");
        routeMap.put(189, "Oxford Shuttle");
        routeMap.put(190, "North Campus");
        routeMap.put(192, "Northwood");
        routeMap.put(193, "North Campus");
        routeMap.put(198, "Bursley-Baits");
        routeMap.put(199, "Northwood");
        routeMap.put(200, "Oxford");

    }

    @Override
    protected void onResume() {
        super.onResume();
        populateStops();
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if(id == closestStop.getId()){

            Intent intent  = new Intent(BusSchedule.this, ClosestStop.class);
            startActivity(intent);
        }
    }

    private void populateStops(){

        rbs = new RetrieveBusStops(this);
        rbs.execute();



    }





    @Override
    public void onTask1Completed() {

        ArrayList<String> stopNames = rbs.getStopNames();
        final HashMap<String, Integer> stopMap = rbs.getStopMap();
        ArrayAdapter <String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, stopNames);
        spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );


        busStops.setAdapter(spinnerArrayAdapter);
        busStops.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //show arrival times for a stop
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String bName = (String) busStops.getItemAtPosition(position);

                //set title
                title.setText(bName);

                int bid = stopMap.get(bName);
                eta = new ETA(bid, BusSchedule.this);
                eta.execute();


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //set defalt spinner value
        setSpinner();

    }

    //sets default value for spinner
    private void setSpinner(){

        //CC Little is the most popular stop, so it makes sense to have it's times displayed immediately
        String myString = "Central Campus Transit Center: CC Little"; //the value you want the position for

        ArrayAdapter myAdap = (ArrayAdapter) busStops.getAdapter(); //cast to an ArrayAdapter

        int spinnerPosition = myAdap.getPosition(myString);

        //set the default according to value
        busStops.setSelection(spinnerPosition);
    }

    //ETA is done
    @Override
    public void onTask2Completed() {

        ArrayList<Integer> bIDs = eta.getBusIDs();
        ArrayList<Integer> expTime = eta.getExpTimes();

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (int i = 0; i < bIDs.size(); i ++) {

            Map<String, String> datum = new HashMap<String, String>(2);

            Integer bid = bIDs.get(i);
            String bName = routeMap.get(bid);
            Integer bTime = expTime.get(i);

            //put bus name and time into hashmap
            datum.put("bus", bName);
            datum.put("time", bTime.toString());
            data.add(datum);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[] {"bus", "time"},
                new int[] {android.R.id.text1,
                        android.R.id.text2});
        arrivalTimes.setAdapter(adapter);

        //set bus schedule
        /*ArrayList<String> incomingBusNames = new ArrayList<String>();
        for(int i = 0; i < bIDs.size(); i ++){

            int bid = bIDs.get(i);
            int bTime = expTime.get(i);
            String bName = routeMap.get(bid);
            if(bName != null){

                incomingBusNames.add(bName + " | " + bTime);
            }
            else{

                incomingBusNames.add("Unknown | " + bTime);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, incomingBusNames);

        arrivalTimes.setAdapter(adapter);

        */


    }
}
