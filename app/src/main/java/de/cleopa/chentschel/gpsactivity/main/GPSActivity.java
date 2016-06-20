package de.cleopa.chentschel.gpsactivity.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import de.cleopa.chentschel.gpsactivity.R;
import de.cleopa.chentschel.gpsactivity.service.GeoPositionsService;

public class GPSActivity extends Activity{//implements GpxParser.GpxParserListener, GpxParserHandler.GpxParserProgressListener{

//    private ProgressDialog mProgressDialog = null;
//    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpsactivity);
        setTitle(R.string.title_activity_gps);
//        showSettingsAlert();
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

//    /**
//     * Function to show settings alert dialog
//     * */
//    public void showSettingsAlert(){
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
//
//        // Setting Dialog Title
//        alertDialog.setTitle("GPS is settings");
//
//        // Setting Dialog Message
//        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
//
//        // Setting Icon to Dialog
//        //alertDialog.setIcon(R.drawable.delete);
//
//        // On pressing Settings button
//        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog,int which) {
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                mContext.startActivity(intent);
//            }
//        });
//
//        // Showing Alert Message
//        alertDialog.show();
//    }
}
