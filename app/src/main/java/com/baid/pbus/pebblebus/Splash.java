package com.baid.pbus.pebblebus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

/**
 * Created by Ish on 10/19/14.
 */
public class Splash extends Activity {

    RelativeLayout background;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        background = (RelativeLayout) findViewById(R.id.splash_background);
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Splash.this, BusSchedule.class);
                startActivity(intent);
            }
        });
    }
}
