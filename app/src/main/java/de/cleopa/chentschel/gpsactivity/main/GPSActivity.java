package de.cleopa.chentschel.gpsactivity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import de.cleopa.chentschel.gpsactivity.R;
import de.cleopa.chentschel.gpsactivity.service.GeoPositionsService;

public class GPSActivity extends AppCompatActivity{//implements GpxParser.GpxParserListener, GpxParserHandler.GpxParserProgressListener{

//    private ProgressDialog mProgressDialog = null;
//    private static final String TAG =KarteAnzeigen.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpsactivity);
        setTitle(R.string.title_activity_gps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.gpsactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Wir prüfen, ob Menü-Element mit der ID "action_daten_aktualisieren"
        // ausgewählt wurde und geben eine Meldung aus
        switch (item.getItemId()) {
            case R.id.men_Beenden:
                finish();
                return true;
            case R.id.men_deleteFile:
                File file = getExternalFilesDir(null);
                file = new File(file, "gps.txt");
                if (file.delete()){
                    Toast.makeText(getBaseContext(), "Die alte Strecke wurde gelöscht!", Toast.LENGTH_LONG).show();
                }else{Toast.makeText(getBaseContext(), "Es existiert keine Strecke zum löschen!", Toast.LENGTH_LONG).show();}
            default:
                return super.onOptionsItemSelected(item);
        }
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
