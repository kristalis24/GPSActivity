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
import java.util.ArrayList;
import java.util.List;

import de.cleopa.chentschel.gpsactivity.R;
import de.cleopa.chentschel.gpsactivity.service.GeoPositionsService;
import de.cleopa.chentschel.gpsactivity.service.GeoPositionsService.GeoPositionsServiceBinder;

public class KarteAnzeigenSaved extends Activity {

    private static final String TAG = KarteAnzeigenSaved.class.getSimpleName();
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
    boolean newFile = true;
    StringBuilder s = null;
    ArrayList<LatLng> list = new ArrayList<LatLng>();
    double latitude;
    double longitude;

//    public static GPXDocument mDocument = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.karte_anzeigen_saved);
        if (mMeinePosition != null && mVerbindungslinie != null) {
            mVerbindungslinie.remove();
        }
        if (mMap != null && mMeinMarker != null) {
            mMeinMarker.remove();
        }

        mKarteAnzeigenCallbackHandler = new KarteAnzeigenCallbackHandler(this);
        mMapView = (MapView) findViewById(R.id.karte_anzeigen_saved);
        mMapView.onCreate(savedInstanceState);
        initMapView();

        final Intent geoIntent = new Intent(this, GeoPositionsService.class);
        bindService(geoIntent, mGeoPositionsServiceConnection, Context.BIND_AUTO_CREATE);
    }

    protected void onDestroy() {
        mMapView.onDestroy();
        mKarteAnzeigenCallbackHandler.removeCallbacksAndMessages(null);
        unbindService(mGeoPositionsServiceConnection);
        stopService(new Intent(this, GeoPositionsService.class));
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
//        stopService(new Intent(this, GeoPositionsService.class));
        if (mMapView != null) {
            // null, wenn Google Play Store nicht installiert ist
            mMapView.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    private void initMapView() {
        boolean usePlayService = isGooglePlayServiceAvailable();

        if (usePlayService) {
            MapsInitializer.initialize(this);

            if (mMap == null) {
                mMap = mMapView.getMap();

                if (mMap != null) {
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

    private boolean isGooglePlayServiceAvailable() {
        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, -1);
            if (errorDialog != null) {
                errorDialog.show();
                return false;
            }
        }
        return true;
    }

    public void handleMessage(Message msg) throws IOException {
        final Bundle bundle = msg.getData();
        final Location location = (Location) bundle.get("location");

//        File extAnwVerzeichnis = getExternalFilesDir(null);
        final String file = new File(getExternalFilesDir(null), "gps.txt").toString();

        FileInputStream openFileInput = new FileInputStream(file);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(openFileInput))) {

            String zeile;
            while ((zeile = in.readLine()) != null) {
                String inhalt[] = zeile.split(",");
                time = Long.parseLong(inhalt[0]);
//                inhalt[0]=null;
                latitude = Double.parseDouble(inhalt[1]);
                longitude = Double.parseDouble(inhalt[2]);

                if (location != null) {
                    latLng = new LatLng(latitude, longitude);
//                    list.add(new LatLng(latitude, longitude));
                }

                if (latLngA == null) {
                    latLngA = latLng;
                }

                final MarkerOptions markerOption = new MarkerOptions();
                markerOption.position(latLng);
                markerOption.title(getAddressFromLatLng(latLng));
                mMeinMarker = mMap.addMarker(markerOption);
                mVerbindungslinie = mMap.addPolyline(new PolylineOptions().add(latLngA, latLng).width(5).color(Color.BLUE));
                mMeinMarker.showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                latLngA = latLng;

            }
        }// finally { inStream.close(); }
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


    static class KarteAnzeigenCallbackHandler extends Handler{

        private WeakReference<KarteAnzeigenSaved> mActivity;

        KarteAnzeigenCallbackHandler(KarteAnzeigenSaved acticity){
            mActivity = new WeakReference<>(acticity);
        }

        @Override
        public void handleMessage(Message msg){
            KarteAnzeigenSaved activity = mActivity.get();

            if (activity != null){
                try {activity.handleMessage(msg);
                } catch (IOException e){
                    Log.e(TAG, "Dateizugriff fehlerhaft.", e);
                }
            }
        }
    }
}