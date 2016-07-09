package de.cleopa.chentschel.gpsactivity.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import de.cleopa.chentschel.gpsactivity.R;

public class AndroidGPSTrackingActivity extends Activity {

    String gpsLocation;
//    Button btnShowLocation;
    TextView textView;

    // GPSTracker class
    GPSTracker gps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(textView);
        textView = (TextView) findViewById(R.id.txtview);

        // show location button click event
//            @Override
//            public void onClick(View arg0) {
                 // create class object
            gps = new GPSTracker(AndroidGPSTrackingActivity.this);

            // check if GPS enabled
            if (gps.canGetLocation()) {
                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                gpsLocation = "Your Location is - \nLat: " + latitude + "\nLong: " + longitude;
//                Toast.makeText(getApplicationContext(), gpsLocation, Toast.LENGTH_LONG).show();
                textView.setText(gpsLocation);
            } else {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gps.showSettingsAlert();
            }
        }
    }
//}
//        });
//}
