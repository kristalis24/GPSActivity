package de.cleopa.chentschel.gpsactivity.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
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

import java.lang.ref.WeakReference;

import de.cleopa.chentschel.gpsactivity.R;
import de.cleopa.chentschel.gpsactivity.service.GeoPositionsService;
import de.cleopa.chentschel.gpsactivity.service.GeoPositionsService.GeoPositionsServiceBinder;

public class KarteAnzeigen extends Activity{

    private static final float DEFAULT_ZOOM_LEVEL = 17.5f;
    private static final String TAG =KarteAnzeigen.class.getSimpleName();
    public static Location mMeinePosition;
    private Marker mMeinMarker;
    private MapView mMapView;
    private GoogleMap mMap;
    public static final String IN_PARAM_GEO_POSITION = "location";
//    public static final int TYP_EIGENE_POSITION = 1;
    public static boolean mPositionNachverfolgen;
    private static Handler mKarteAnzeigenCallbackHandler;
    private Polyline mVerbindungslinie;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.karte_anzeigen);

        if (mMeinePosition != null && mVerbindungslinie != null){
                mVerbindungslinie.remove();
        }

        if (mMap != null && mMeinMarker != null){
                    mMeinMarker.remove();
        }


        mKarteAnzeigenCallbackHandler = new KarteAnzeigenCallbackHandler(this);

        mMapView = (MapView) findViewById(R.id.karte_anzeigen);
        mMapView.onCreate(savedInstanceState);

        initMapView();

        final Intent geoIntent = new Intent(this, GeoPositionsService.class);
        bindService(geoIntent, mGeoPositionsServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
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
        if (bundle != null) {
            final Location location = (Location) bundle.get(KarteAnzeigen.IN_PARAM_GEO_POSITION);

            if (location == null) {
                Log.d(TAG, "-----> location ist NULL <---");
            } else {
                Log.d(TAG, "-----> location ist nicht NULL <---");
            }

            final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            Log.d(TAG, "handleMessage latlng: " + latLng + "bundle:" + bundle.toString());

//            final int typ = msg.what;

            final MarkerOptions markerOption = new MarkerOptions();
            markerOption.position(latLng);

//
//            mMeinePosition = location;

            markerOption.title(getString(R.string.position_ich));  // TODO: Hier Adresse anzeigen
            mMeinMarker = mMap.addMarker(markerOption);
            mMeinMarker.showInfoWindow();

            if (!mPositionNachverfolgen) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }

//            LatLng latLngA;
            LatLng latLngA = new LatLng(location.getLatitude(), location.getLongitude());

            if (!latLng.equals(latLngA)) {

                mVerbindungslinie = mMap.addPolyline(new PolylineOptions().add(latLng, latLngA).width(5).color(Color.BLUE));

                Log.d(TAG, "\nnew latlng: " + latLng);
                Log.d(TAG, "\n\nmVerbindungslinie: " + mVerbindungslinie.getPoints().toString() + "    mVlatlng: " + latLng + "    mVlatLngA: " + latLngA);

                markerOption.position(latLngA);
//                markerOption.title("Punkt 2");
                Marker mMeinMarker2 = mMap.addMarker(markerOption);
                mMeinMarker2.showInfoWindow();
            }
        }
    }

    private ServiceConnection mGeoPositionsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            ((GeoPositionsServiceBinder) binder).setzeActivityCallbackHandler(mKarteAnzeigenCallbackHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {    }
    };

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