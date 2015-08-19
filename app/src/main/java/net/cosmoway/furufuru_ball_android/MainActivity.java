package net.cosmoway.furufuru_ball_android;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends Activity implements MyWebSocketClient.MyCallbacks {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private GraphicsView mGraphicsView;

    private SurfaceView mOverLaySurfaceView;
    private SurfaceHolder mOverLayHolder;
    private OverlayGraphicsView mOverlayGraphicsView;

    private Context mContext;

    private MyWebSocketClient mWebSocketClient;

    private static final String TAG = "Ws";

    /**
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebSocketClient = MyWebSocketClient.newInstance();
        mWebSocketClient.setCallbacks(this);
        mWebSocketClient.connect();
        mContext = getApplicationContext();
        setContentView(R.layout.main);
        //オーバーレイするSurfaceView

        mOverlayGraphicsView = new OverlayGraphicsView();
        mOverLaySurfaceView = (SurfaceView) findViewById(R.id.overLaySurfaceView);
        mOverLayHolder = mOverLaySurfaceView.getHolder();
        //ここで半透明にする
        mOverLayHolder.setFormat(PixelFormat.TRANSLUCENT);
        mOverLayHolder.addCallback(mOverlayGraphicsView);

        //背景になるSurfaceView
        mGraphicsView = new GraphicsView(mContext);
        mSurfaceView = (SurfaceView) findViewById(R.id.mySurfaceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(mGraphicsView);

        findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGraphicsView.onStart();
            }
        });
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
    public void moveIn() {

    }

    @Override
    public void join() {
        mGraphicsView.mJoin++;
        TextView text = (TextView) findViewById(R.id.text_join);
        text.setText("いまのおともだちは" + mGraphicsView.mJoin + "にんです。");
    }

    @Override
    public void start() {
        findViewById(R.id.button_help).setVisibility(View.INVISIBLE);
        findViewById(R.id.view_lobby).setVisibility(View.INVISIBLE);
    }

    @Override
    public void gameOver() {
        findViewById(R.id.overLaySurfaceView).setVisibility(View.VISIBLE);
        findViewById(R.id.view_result).setVisibility(View.VISIBLE);
        TextView result = (TextView) findViewById(R.id.text_result);
        if (mGraphicsView.mTime > 10000 || mGraphicsView.mTime > GraphicsView.INIT_TIME - (mGraphicsView.mJoin + 1)) {
            result.setText("あなたのきろくは0びょうでした。");
        } else {
            result.setText("あなたのきろくは" + ((double) mGraphicsView.mTime / 1000) + "びょうでした。");
        }
        mWebSocketClient.close();
        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGraphicsView.isRunning = false;
                findViewById(R.id.overLaySurfaceView).setVisibility(View.INVISIBLE);
                findViewById(R.id.view_result).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_help).setVisibility(View.VISIBLE);
                findViewById(R.id.view_lobby).setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}