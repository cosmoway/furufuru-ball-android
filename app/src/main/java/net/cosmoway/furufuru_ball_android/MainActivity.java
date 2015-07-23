package net.cosmoway.furufuru_ball_android;

import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends Activity {

    private final String TAG = "Ws";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new GraphicsView(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if ("sdk".equals(Build.PRODUCT)) {
            // エミュレータの場合はIPv6を無効    ----1
            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
            java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        }

        try {
            URI uri = new URI("ws://10.0.2.2:3333");
            WebSocketClient client = new WebSocketClient(uri) {

                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.d(TAG, "onOpen");
                }

                @Override
                public void onMessage(final String message) {
                    Log.d(TAG, "onMessage");
                    Log.d(TAG, "Message:" + message);
                }

                @Override
                public void onError(Exception ex) {
                    Log.d(TAG, "onError");
                    ex.printStackTrace();
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "onClose");
                }
            };

            client.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setBearingRequired(false);  // 方位不要
        criteria.setSpeedRequired(false);    // 速度不要
        criteria.setAltitudeRequired(false); // 高度不要
        if (locationManager != null) {
            locationManager.requestSingleUpdate(criteria, new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {
                    String str = "";
                    str = str + "Latitude:" + String.valueOf(location.getLatitude()) + "\n";
                    str = str + "Longitude:" + String.valueOf(location.getLongitude());
                    Log.d("Location", str);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            }, null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}