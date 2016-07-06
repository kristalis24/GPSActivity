package de.cleopa.chentschel.gpsactivity.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

import de.cleopa.chentschel.gpsactivity.R;
import de.cleopa.chentschel.gpsactivity.service.GeoPositionsService;
import de.cleopa.chentschel.gpsactivity.service.GeoPositionsService.GeoPositionsServiceBinder;

public class KarteAnzeigen extends Activity{

    private static final String TAG =KarteAnzeigen.class.getSimpleName();
    private static final float DEFAULT_ZOOM_LEVEL = 17.5f;
    public static Location mMeinePosition;
    private Marker mMeinMarker;
    private MapView mMapView;
    private GoogleMap mMap;
    public static final String IN_PARAM_GEO_POSITION = "location";
    public static final int TYP_EIGENE_POSITION = 1;
    private static Handler mKarteAnzeigenCallbackHandler;
    private Polyline mVerbindungslinie;
    LatLng latLngA;
    LatLng latLng;
    long time;
    boolean newFile;
    Double breite;
    Double länge;
    double höhe;
//    public static GPXDocument mDocument = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.karte_anzeigen);
        if (mMeinePosition != null && mVerbindungslinie != null){mVerbindungslinie.remove();}
        if (mMap != null && mMeinMarker != null){mMeinMarker.remove();}

        mKarteAnzeigenCallbackHandler = new KarteAnzeigenCallbackHandler(this);
        mMapView = (MapView) findViewById(R.id.karte_anzeigen);
        mMapView.onCreate(savedInstanceState);
        initMapView();

        final Intent geoIntent = new Intent(this, GeoPositionsService.class);
        bindService(geoIntent, mGeoPositionsServiceConnection, Context.BIND_AUTO_CREATE);
    }

    protected void onDestroy(){
        mMapView.onDestroy();
        mKarteAnzeigenCallbackHandler.removeCallbacksAndMessages(null);
        unbindService(mGeoPositionsServiceConnection);
        stopService(new Intent(this, GeoPositionsService.class));
        super.onDestroy();
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume(){
        if (mMapView != null){
            // null, wenn Google Play Store nicht installiert ist
            mMapView.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onPause(){
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    private void initMapView(){
        boolean usePlayService = isGooglePlayServiceAvailable();

        if (usePlayService){
            MapsInitializer.initialize(this);

            if (mMap == null){
                mMap = mMapView.getMap();

                if (mMap != null){
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                    mMap.getUiSettings().setCompassEnabled(true);
                    mMap.setMyLocationEnabled(true);
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    mMap.setIndoorEnabled(true);
                    mMap.setTrafficEnabled(true);
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));
                }
            }
        } else {
            finish();
        }
    }

    private boolean isGooglePlayServiceAvailable(){
        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (errorCode != ConnectionResult.SUCCESS){
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, -1);
            if (errorDialog != null){
                errorDialog.show();
                return false;
            }
        }
        return true;
    }

    public void handleMessage(Message msg) {
        final Bundle bundle = msg.getData();
        final Location location = (Location) bundle.get("location");

        if (location != null) {
        breite = location.getLatitude();
        länge = location.getLongitude();
        time = location.getTime();
        höhe = location.getAltitude();

            latLng = new LatLng(breite, länge);
        }
        if (latLngA==null){latLngA=latLng;}

        final MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(latLng);
        markerOption.title(getAddressFromLatLng(latLng));
        mMeinMarker = mMap.addMarker(markerOption);
        mVerbindungslinie=mMap.addPolyline(new PolylineOptions().add(latLngA, latLng).width(5).color(Color.BLUE));
        mMeinMarker.showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        latLngA=latLng;
        
        demoExternesAnwendungsVerzeichnis(time,höhe,breite,länge);
//        demoExternesAnwendungsVerzeichnis(breite + "," + länge);
    }

    private String getAddressFromLatLng(LatLng latLng){
        Geocoder geocoder = new Geocoder(getBaseContext());
        String address = "";
        try {
            address=geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0).getAddressLine(0);
        } catch (IOException e){
            e.printStackTrace();
        }
        return address;
    }

    private ServiceConnection mGeoPositionsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            ((GeoPositionsServiceBinder) binder).setzeActivityCallbackHandler(mKarteAnzeigenCallbackHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
        }
    };

    public void demoExternesAnwendungsVerzeichnis(long time, double höhe, double lati, double loni){
//        String geoData = time+","+höhe+","+lati+","+loni;
        try {
            // Zugriff auf Anwendungsverzeichnis auf externen Speicher
            File extAnwVerzeichnis = getExternalFilesDir(null);
            if (extAnwVerzeichnis != null){
                File akte = new File(extAnwVerzeichnis, "gps.txt");
                // Falls das File noch nicht existiert wird es mit createNewFile() erzeugt.
                // Ansonsten wird createNewFile() ignoriert.
                newFile = akte.createNewFile();
                FileInputStream in = new FileInputStream(akte);
                //StringBuilder s = leseDatei(in, geoData);

                schreibeDatei(akte,time,höhe,lati,loni);
            }
        } catch (IOException e){
            Log.e(TAG, "Dateizugriff fehlerhaft.", e);
        }
    }

    private void schreibeDatei(File out, long time, double höhe, double lati, double loni) throws IOException{
        StringBuilder geoData = new StringBuilder("");
        if (!(newFile = out.createNewFile())){
            geoData.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            geoData.append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"EasyTrails\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n");
//            schreibeDatei(out, header);
        } else {

            //makeFile(long time, double höhe, double lati, double loni);
            geoData.append("<wpt lat=\"" + lati + "\" lon=\"" + loni + "\">");
            geoData.append("<ele> =\"" + höhe + "\" </ele>");
            geoData.append("<time> =\"" + time + "\" </time>");
            geoData.append("<name><![CDATA[]]></name>");
            geoData.append("<desc><![CDATA[]]></desc>");
            geoData.append("<type><![CDATA[]]></type>");
            geoData.append("</wpt>");
        }

        try (FileWriter file = new FileWriter(out)) {
            file.write(geoData.toString());
            file.append("\n");
        }catch (IOException e){
            Log.e(TAG, "Dateizugriff fehlerhaft.", e);
        }
    }

    private void schreibeDatei(File out, String header){
        try (FileWriter file = new FileWriter(out)) {
            file.write(header);
//            file.append("\n");
        }catch (IOException e){
            Log.e(TAG, "Dateizugriff fehlerhaft.", e);
        }
    }

    private StringBuilder leseDatei(FileInputStream inStream, String geoData) throws IOException{
        StringBuilder inhalt = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inStream))) {
            String zeile;
            while ((zeile = in.readLine()) != null) {
                inhalt.append(zeile);
                inhalt.append("\n");
            }
        } finally {
            inhalt.append(geoData);
        }
        return inhalt;
    }



    static class KarteAnzeigenCallbackHandler extends Handler{
        private WeakReference<KarteAnzeigen> mActivity;

        KarteAnzeigenCallbackHandler(KarteAnzeigen acticity){
            mActivity = new WeakReference<>(acticity);
        }

        @Override
        public void handleMessage(Message msg){
            KarteAnzeigen activity = mActivity.get();
            if (activity != null){
                activity.handleMessage(msg);
            }
        }
    }
}