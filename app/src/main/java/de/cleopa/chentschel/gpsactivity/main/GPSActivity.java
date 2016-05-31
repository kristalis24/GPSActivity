package de.cleopa.chentschel.gpsactivity.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import de.cleopa.chentschel.gpsactivity.R;
import de.cleopa.chentschel.gpsactivity.service.GeoPositionsService;

public class GPSActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpsactivity);
        setTitle(R.string.title_activity_gps);
        startService(new Intent(this, GeoPositionsService.class));
    }

    @Override
    protected void onDestroy(){
        stopService(new Intent(this, GeoPositionsService.class));
        super.onDestroy();
    }

    public void onClickKarteAnzeigen(final View sfNormal){
        final Intent intent = new Intent(this, KarteAnzeigen.class);
        startActivity(intent);
    }
}
