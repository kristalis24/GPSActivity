package de.cleopa.chentschel.gpsactivity.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import de.cleopa.chentschel.gpsactivity.main.GpsData;
import de.cleopa.chentschel.gpsactivity.main.KarteAnzeigen;


public class GeoPositionsService extends Service implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener {

    private GpsData mGpsData;
    private final IBinder mGpsBinder = new GeoPositionsServiceBinder();
    private Handler mKarteAnzeigenCallbackHandler;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;

    private static final long UPDATE_INTERVAL = 15000;
    private static final long SCHNELLSTES_INTERVAL = 5000;

    /**
     * onLocationChanged(): Update der GPS-Koordinaten alle 5sek. beim SCHNELLSTES_INTERVAL
     *                      bzw. alle 15sek bei UPDATE_INTERVAL
     *
     * @param location = GPS-Koordinaten (double Breitengrad, double Längengrad)
     */

    @Override
    public void onLocationChanged(Location location) {
        if (location != null){
            mGpsData = new GpsData(location);
        }

        if (mKarteAnzeigenCallbackHandler != null){
            final Bundle bundle = new Bundle();
            bundle.putParcelable(KarteAnzeigen.IN_PARAM_GEO_POSITION, location);

            final Message msg = new Message();
            msg.setData(bundle);
//            msg.what = KarteAnzeigen.TYP_EIGENE_POSITION;

            mKarteAnzeigenCallbackHandler.sendMessage(msg);
        }
    }

    @Override
    public void onCreate() {
        boolean usePlayService = isGooglePlayServiceAvailable();
        if (usePlayService) {
            starteGeoProvider();
        }

        if (mLocationClient != null){
            mLocationClient.connect();
        }
    }

    @Override
    public void onDestroy() {
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mGpsBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mKarteAnzeigenCallbackHandler = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onConnected(Bundle arg0) {
        Location location = mLocationClient.getLastLocation();
        mGpsData = new GpsData(location);
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
        Toast.makeText(this, (mGpsData.toString()), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisconnected(){

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private boolean isGooglePlayServiceAvailable() {
        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return errorCode == ConnectionResult.SUCCESS;
    }

    private void starteGeoProvider(){
        mLocationClient = new LocationClient(this, this, this);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(SCHNELLSTES_INTERVAL);
    }

    public class GeoPositionsServiceBinder extends Binder{

//        public GpsData getGpsData(){return mGpsData;}

        public void setzeActivityCallbackHandler(final Handler callback){
            mKarteAnzeigenCallbackHandler = callback;
        }

//        public void restarteGeoProvider(){starteGeoProvider();}
    }
}
