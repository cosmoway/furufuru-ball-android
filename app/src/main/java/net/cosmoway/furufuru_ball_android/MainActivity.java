package net.cosmoway.furufuru_ball_android;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

public class MainActivity extends Activity implements MyWebSocketClient.MyCallbacks {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private GraphicsView mGraphicsView;

    private SurfaceView mOverLaySurfaceView;
    private SurfaceHolder mOverLayHolder;
    private OverlayGraphicsView mOverlayGraphicsView;

    private PopupWindow mPopupWindow;

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
        //mWebSocketClient.connect();
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
        TextView text = (TextView) findViewById(R.id.text_join);
        text.setText("Join：" + 0);
        findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGraphicsView.onStart();
                findViewById(R.id.button_help).setVisibility(View.INVISIBLE);
                findViewById(R.id.view_lobby).setVisibility(View.INVISIBLE);
            }
        });
        findViewById(R.id.button_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow = new PopupWindow(MainActivity.this);

                //レイアウト設定
                View popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);
                ImageButton btn = (ImageButton) popupView.findViewById(R.id.button_close);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mPopupWindow.isShowing()) {
                            mPopupWindow.dismiss();
                        }

                    }
                });

                try {
                    mPopupWindow.setContentView(popupView);
                    /*
                    //背景設定
                    mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.styleable.PopupWindow_popupBackground));
                    */

                    // タップ時に他のViewでキャッチされないための設定
                    mPopupWindow.setOutsideTouchable(true);
                    mPopupWindow.setFocusable(true);

                    // 表示サイズの設定
                    float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());

                    mPopupWindow.setWindowLayoutMode((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
                    mPopupWindow.setWidth((int) width);
                    mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                    mPopupWindow.showAtLocation(v, Gravity.CENTER,  0, 0);
                } catch (Exception e) {

                }
            }
        });
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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
        TextView text = (TextView) findViewById(R.id.text_join);
        text.setText("Join：" + mWebSocketClient.mCount);
    }

    @Override
    public void start() {

    }

    @Override
    public void gameOver() {
        findViewById(R.id.overLaySurfaceView).setVisibility(View.VISIBLE);
        findViewById(R.id.view_result).setVisibility(View.VISIBLE);
        TextView result = (TextView) findViewById(R.id.text_result);
        if (mGraphicsView.mTime > 10000 /*|| mGraphicsView.mTime > GraphicsView.INIT_TIME - (mGraphicsView.mJoin + 1)*/) {
            result.setText("Time　----");
        } else {
            result.setText("Time　" + ((double) mGraphicsView.mTime / 1000));
        }
        //mWebSocketClient.close();
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