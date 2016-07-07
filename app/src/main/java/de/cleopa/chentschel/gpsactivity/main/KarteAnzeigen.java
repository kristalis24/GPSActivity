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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

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
//    StringBuilder geoData = new StringBuilder("");
//    public static GPXDocument mDocument = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        File akte = new File(getExternalFilesDir(null),"gpx.txt");
        if (akte.exists())
        {
            akte.delete();
            Log.d(TAG, "---> FILE GELÖSCHT <---");
        }
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

        try {schreibeDateiEnde();}
        catch (IOException e){Log.d(TAG, "\nonServiceDisconnected(): DateiEnde konnte nicht geschrieben werden\n"+e);}
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
                    //mMap.setTrafficEnabled(true);
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
        try {File extAnwVerzeichnis = getExternalFilesDir(null);// Zugriff auf Anwendungsverzeichnis auf externen Speicher
            if (extAnwVerzeichnis != null)
            {File akte = new File(extAnwVerzeichnis, "gps.txt");
//                FileInputStream in = new FileInputStream(akte);
//                StringBuilder s = leseDatei(in, geoData);
                //leseDatei(akte);
                schreibeDatei(akte,time,höhe,lati,loni);
            }
        } catch (IOException e){Log.e(TAG, "Dateizugriff fehlerhaft.", e);}
    }

    private void schreibeDatei(File akte, long time, double höhe, double lati, double loni) throws IOException{
        StringBuilder geoData = new StringBuilder();

        if (akte.exists() == false) {
            geoData = new StringBuilder("");
            geoData.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            geoData.append("\n");
            geoData.append("\n<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"GPXActivity\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">");
            geoData.append("\n");
        } else {
            geoData.append(leseDatei(akte).toString());
            geoData.append("\n");
            geoData.append("\n<wpt lat=\"" + lati + "\" lon=\"" + loni + "\">\n");
            geoData.append("<ele>\"" + höhe + "\"</ele>\n");
            geoData.append("<time>\"" + getZeit(time) + "\"</time>\n");
            geoData.append("<name><![CDATA[]]></name>\n");
            geoData.append("<desc><![CDATA[]]></desc>\n");
            geoData.append("<type><![CDATA[]]></type>\n");
            geoData.append("</wpt>\n");
        }
        try (FileWriter file = new FileWriter(akte)) {file.write(geoData.toString());
        } catch (IOException e) {Log.e(TAG, "Dateizugriff fehlerhaft.", e);}
    }

    private void schreibeDateiEnde() throws IOException{
        File akte = new File(getExternalFilesDir(null), "gps.txt");
        StringBuilder geoData = new StringBuilder(leseDatei(akte));
        geoData.append("\n</gpx>");
        try (FileWriter fileWriter = new FileWriter(akte)){fileWriter.write(geoData.toString());}
        catch (IOException e){Log.e(TAG, "Dateizugriff fehlerhaft.", e);}
    }

    private StringBuilder leseDatei(File akte) throws IOException {
        StringBuilder inhalt = new StringBuilder();
        FileInputStream inStream = new FileInputStream(akte);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inStream)))
        {
            String zeile;

            while ((zeile = in.readLine()) != null)
            {
                inhalt.append(zeile);
            }
        }
        return inhalt;
    }

    public String getZeit(long time){
//        TimeZone tz = TimeZone.getTimeZone("Locale.GERMAN");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.GERMANY); // Quoted "Z" to indicate UTC, no timezone offset
//        df.setTimeZone(tz);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddZHH:mm:ssZ", Locale.GERMAN);
        return df.format(time);
    }



    static class KarteAnzeigenCallbackHandler extends Handler{
        private WeakReference<KarteAnzeigen> mActivity;

        KarteAnzeigenCallbackHandler(KarteAnzeigen acticity){
            mActivity = new WeakReference<>(acticity);
        }

        @Override
        public void handleMessage(Message msg){
            KarteAnzeigen activity = mActivity.get();
            if (activity != null){activity.handleMessage(msg);}
        }
    }
}