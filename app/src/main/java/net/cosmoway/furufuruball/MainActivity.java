package net.cosmoway.furufuruball;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity implements MyWebSocketClient.MyCallbacks,
        GraphicsView.Callback {

    private GraphicsView mGraphicsView;
    private PopupWindow mPopupWindow;
    private MyWebSocketClient mWebSocketClient;
    private Handler mHandler = new Handler();

    private static final String TAG = "Ws";

    /**
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectIfNeeded();
        setContentView(R.layout.main);
        //背景になるSurfaceView
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.mySurfaceView);
        mGraphicsView = new GraphicsView(this, surfaceView);
        mGraphicsView.setCallback(this);
        TextView text = (TextView) findViewById(R.id.text_join);
        text.setText("Join：" + 0);
        findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGraphicsView.onStart();
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
                InputStream is = null;
                BufferedReader br = null;
                String text = "";
                try {
                    try {
                        // assetsフォルダ内の sample.txt をオープンする
                        is = getAssets().open("help.txt");
                        br = new BufferedReader(new InputStreamReader(is));

                        // １行ずつ読み込み、改行を付加する
                        String str;
                        while ((str = br.readLine()) != null) {
                            text += str + "\n";
                        }
                    } finally {
                        if (is != null) is.close();
                        if (br != null) br.close();
                    }
                } catch (IOException e) {
                    Log.i(TAG, "error");
                }

                TextView helpText = (TextView) popupView.findViewById(R.id.text_help);
                helpText.setText(text);
                mPopupWindow.setContentView(popupView);
                //背景に透明な画像を設定
                mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                // タップ時に他のViewでキャッチされないための設定
                mPopupWindow.setOutsideTouchable(true);
                mPopupWindow.setFocusable(true);
                // 表示サイズの設定
                float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());

                mPopupWindow.setWindowLayoutMode((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
                mPopupWindow.setWidth((int) width);
                mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                mPopupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
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
        mGraphicsView.moveIn();
    }

    @Override
    public void join(final int count) {
        mGraphicsView.join(count);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                TextView text = (TextView) findViewById(R.id.text_join);
                text.setText("Join：" + count);
            }
        });
    }

    @Override
    public void start() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.button_help).setVisibility(View.INVISIBLE);
                findViewById(R.id.view_lobby).setVisibility(View.INVISIBLE);
            }
        });
        mGraphicsView.start();

    }

    @Override
    public void gameOver() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mGraphicsView.gameOver();
                findViewById(R.id.view_overlay).setVisibility(View.VISIBLE);
                findViewById(R.id.view_result).setVisibility(View.VISIBLE);
                TextView result = (TextView) findViewById(R.id.text_result);
                TextView gameSet = (TextView) findViewById(R.id.text_game_set);
                if (GraphicsView.isTimeUp(mGraphicsView.mTime, mGraphicsView.mJoinCount)) {
                    gameSet.setText(R.string.text_game_over);
                    result.setText("Time　----");
                } else {
                    gameSet.setText(R.string.text_game_set);
                    result.setText("Time　" + ((double) mGraphicsView.mTime / 1000));
                }
                findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mGraphicsView.init();
                        connectIfNeeded();
                        findViewById(R.id.view_overlay).setVisibility(View.INVISIBLE);
                        findViewById(R.id.view_result).setVisibility(View.INVISIBLE);
                        findViewById(R.id.button_help).setVisibility(View.VISIBLE);
                        findViewById(R.id.view_lobby).setVisibility(View.VISIBLE);
                    }
                });
                disconnect();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onGameStart() {
        sendJson("{\"game\":\"start\"}");
    }

    @Override
    public void onMoveOut() {
        sendJson("{\"move\":\"out\"}");
    }

    @Override
    public void onGameOver() {
        sendJson("{\"game\":\"over\"}");
    }

    private void sendJson(String json) {
        connectIfNeeded();
        if (mWebSocketClient.isOpen()) {
            mWebSocketClient.send(json);
        }
    }

    private void connectIfNeeded() {
        if (mWebSocketClient == null || mWebSocketClient.isClosed()) {
            mWebSocketClient = MyWebSocketClient.newInstance();
            mWebSocketClient.setCallbacks(MainActivity.this);
            mWebSocketClient.connect();
        }
    }

    private void disconnect() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
            mWebSocketClient = null;
        }
    }
}