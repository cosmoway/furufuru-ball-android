package net.cosmoway.furufuru_ball_android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GPSService extends Service implements LocationListener {
    private LocationManager mLocman;
    /** Called when the activity is first created. */
    @Override
    public void onCreate() {
        mLocman = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if (mLocman != null){
            mLocman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,this);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        if (mLocman != null){
            mLocman.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location){
        Log.v("----------", "----------");
        Log.v("Latitude", String.valueOf(location.getLatitude()));
        Log.v("Longitude", String.valueOf(location.getLongitude()));
        Log.v("Accuracy", String.valueOf(location.getAccuracy()));
        Log.v("Altitude", String.valueOf(location.getAltitude()));
        Log.v("Time", String.valueOf(location.getTime()));
        Log.v("Speed", String.valueOf(location.getSpeed()));
        Log.v("Bearing", String.valueOf(location.getBearing()));
    }

    @Override
    public void onProviderDisabled(String provider){

    }

    @Override
    public void onProviderEnabled(String provider){
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
        switch(status){
            case LocationProvider.AVAILABLE:
                Log.v("Status","AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.v("Status","OUT_OF_SERVICE");
                break;
            case  LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.v("Status","TEMPORARILY_UNAVAILABLE");
                break;

        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
