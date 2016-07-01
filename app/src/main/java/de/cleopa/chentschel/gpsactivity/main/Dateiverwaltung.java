package de.cleopa.chentschel.gpsactivity.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import de.cleopa.chentschel.gpsactivity.R;

public class Dateiverwaltung extends Activity {

    public static final String TAG = Dateiverwaltung.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dateiverwaltung);
    }
//
//    public boolean onCreateOptionsMenu(Menu menu){
//        getMenuInflater().inflate(R.menu.activity_dateiverwaltung, menu);
//        return true;
//    }

//    public void demoAnwVerzeichnis(){
//        try {
//            // Datei löschen, falls vorhanden
//            deleteFile("beispiel.txt");
//
//            // Datei schreiben mit openFileOutput
//            FileOutputStream out = openFileOutput("beispiel.txt", MODE_PRIVATE);
//            schreibeDatei(out);
//
//            // Datei lesen mit openFileInput
//            FileInputStream inStream = openFileInput("beispiel.txt");
//            Log.i(TAG, "Dateiinhalt: " + leseDatei(inStream));
//
//        } catch (IOException e){
//            Log.e(TAG, "Dateizugriff fehlerhaft.", e);
//        }
//    }
//
//    public void demoUnterVerzeichnis(){
//        try{
//            // Verzeichnis anlegen: getDir
//            // Anwendungsverzeichnis sollte PRIVATE sein.
//            File pubVerz = getDir("Personal", MODE_PRIVATE);
//            Log.i(TAG, "Öffne lokales Unterverzeichnis: " + pubVerz.getPath());
//
//            File akte = new File(pubVerz, "akte.txt");
//            FileOutputStream out = new FileOutputStream(akte);
//            schreibeDatei(out);
//
//            // Datei aus Verzeichnis lesen
//            pubVerz = getDir("Personal", MODE_PRIVATE);
//            akte = new File(pubVerz, "akte.txt");
//            FileInputStream inStream = new FileInputStream(akte);
//            leseDatei(inStream);
//
//        }catch (IOException e){
//            Log.e(TAG, "Dateizugriff fehlerhaft.", e);
//        }
//    }

    private void schreibeDatei(FileOutputStream out, String geoData) throws IOException{
        OutputStreamWriter wrt = new OutputStreamWriter(out);
        try{
            wrt.write(geoData + "\n");
        }finally {
            wrt.close();
        }
    }

    private String leseDatei(FileInputStream inStream) throws IOException{
        BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
        StringBuilder inhalt = new StringBuilder();
        try {
            String zeile;
            while ((zeile = in.readLine()) != null){
                inhalt.append(zeile);
            }
        }finally {
            in.close();
        }
        return inhalt.toString();
    }

    private void protokolliereUmgebung(){
        Log.i(TAG, "SD Kartenmodus: " + Environment.getExternalStorageState());
        Log.i(TAG, "SD Verzeichnis: " + Environment.getExternalStorageDirectory());
    }

    public void demoExternesAnwendungsVerzeichnis(String geoData){
        try {
            // Zugriff auf Anwendungsverzeichnis auf externen Speicher
            File extAnwVerzeichnis = getExternalFilesDir(null);
            if (extAnwVerzeichnis != null){
                Log.i(TAG, "Externes Anwendungsverzeichnis: " + extAnwVerzeichnis.getPath());
                File akte = new File(extAnwVerzeichnis, "gps.txt");
                FileOutputStream out = new FileOutputStream(akte);
                schreibeDatei(out, geoData);
            }
        } catch (IOException e){
            Log.e(TAG, "Dateizugriff fehlerhaft.", e);
        }
    }
}