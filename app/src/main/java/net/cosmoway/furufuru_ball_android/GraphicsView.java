package net.cosmoway.furufuru_ball_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class GraphicsView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener
        , Runnable, MyWebSocketClient.MyCallbacks {
    private Display mDisplay;
    private Point mSize;
    // 円の半径
    //private final int INIT_DIAMETER = 80;
    private int mDiameter;
    // 円のX,Y座標
    private float mCircleX = mDiameter;
    private float mCircleY = mDiameter;
    // Acceleraton
    private float[] mAcceleration;
    private float[] mLinearAcceleration;
    // 円の加速度
    private float mCircleAx = 0.0f;
    private float mCircleAy = 0.0f;
    // 円の移動量
    private double mCircleVx = 0.0d;
    private double mCircleVy = 0.0d;
    // 描画用
    private Paint mPaint;
    // Loop
    private Thread mLoop;
    // SensorManager
    private SensorManager mManager;
    // Delay of sensor
    private final int SENSOR_DELAY;
    // Vibration
    private Vibrator mVib;
    private MyWebSocketClient mWebSocketClient;
    // Speed(scalar)
    private final int SPEED = 50;
    // Flag
    private boolean is = false;
    // Timer
    private long mTime;
    private long mCTime;
    private long mSTime;
    private long mStartTime;
    private long mStopTime;
    // Json
    private String mJson;


    // Constructor
    public GraphicsView(Context context) {
        super(context);
        // SurfaceView描画に用いるコールバックを登録する。
        getHolder().addCallback(this);
        // 描画用の準備
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        // Get the system-service.
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = window.getDefaultDisplay();
        mSize = new Point();
        mManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mVib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mWebSocketClient = MyWebSocketClient.newInstance();
        mWebSocketClient.setCallbacks(this);
        mLoop = new Thread(this);
        // Initializeing of acceleraton.
        mAcceleration = new float[]{0.0f, 0.0f, 0.0f};
        mLinearAcceleration = new float[]{0.0f, 0.0f, 0.0f};
        SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;
        mTime = 0;
        mStopTime = 0;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // SurfaceView生成時に呼び出されるメソッド。
        // 今はとりあえず背景を青にするだけ。
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.BLUE);
        holder.unlockCanvasAndPost(canvas);
        mWebSocketClient.connect();
        // Regist the service of sensor.
        ArrayList<List<Sensor>> sensors = new ArrayList<>();
        sensors.add(mManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION));
        sensors.add(mManager.getSensorList(Sensor.TYPE_ACCELEROMETER));
        for (List<Sensor> sensor : sensors) {
            if (sensor.size() > 0) {
                Sensor s = sensor.get(0);
                mManager.registerListener(this, s, SENSOR_DELAY);
            }
        }
        if (android.os.Build.VERSION.SDK_INT < 14) {
            mDiameter = mDisplay.getWidth() / 12;
        } else {
            mDisplay.getSize(mSize);
            mDiameter = mSize.x / 12;
        }
        mCircleX = -mDiameter * 3;
        mCircleY = 0;
        mCircleVx = 30;
        mCTime = 0;
        mSTime = 0;
        mLoop.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mManager.unregisterListener(this);
        mWebSocketClient.close();
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAcceleration = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            mLinearAcceleration = event.values.clone();
        }

        mCircleAx = -(mAcceleration[0] / 20 + mLinearAcceleration[0] / 4);
        mCircleAy = mAcceleration[1] / 20 + mLinearAcceleration[1] / 4;
        String str = "Acceleration:"
                + "\nX:" + mCircleAx
                + "\nY:" + mCircleAy;
        Log.d("Acceleration", str);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void moveIn() {
        if (mCircleX > getWidth() + mDiameter * 3) {
            mCircleX = (float) (getWidth() + mDiameter * 3);
            mCircleVx = -30;
        } else if (mCircleX < -mDiameter * 3) {
            mCircleX = (float) (-mDiameter * 2 + mCircleVx);
            mCircleVx = 30;
        }
        if (mCircleY > getHeight() + mDiameter * 2) {
            mCircleY = (float) (getHeight() + mDiameter * 3);
            mCircleVy = -30;
        } else if (mCircleY < -mDiameter * 3) {
            mCircleY = (float) (-mDiameter * 3);
            mCircleVy = 30;
        }
        mStartTime = System.currentTimeMillis();
        is = true;
    }

    @Override
    public void gameOver() {
        is = false;
        System.out.println(mTime);
    }

    @Override
    public void run() {
        // Runnableインターフェースをimplementsしているので、runメソッドを実装する
        // これは、Threadクラスのコンストラクタに渡すために用いる。

        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (is) {
                Canvas canvas = getHolder().lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(Color.BLUE);
                    // 円を描画する
                    canvas.drawCircle(mCircleX, mCircleY, mDiameter, mPaint);
                    getHolder().unlockCanvasAndPost(canvas);
                    mCTime = System.currentTimeMillis() - mStartTime;
                    mTime = mCTime + mSTime;
                    // 円の座標を移動させる
                    mCircleVx += mCircleAx * SENSOR_DELAY;
                    mCircleVy += mCircleAy * SENSOR_DELAY;
                    mCircleX += mCircleVx * SENSOR_DELAY;
                    mCircleY += mCircleVy * SENSOR_DELAY;

                    // 画面の領域を超えた？
                    if (mCircleX < mDiameter || getWidth() - mDiameter < mCircleX) {
                        if (Math.abs(mCircleVx) <= SPEED) {
                            mVib.vibrate(Math.abs((long) mCircleVx));
                            mCircleVx = -mCircleVx * 0.9;
                            mCircleAx = -mCircleAx;
                            if (mCircleX < mDiameter) mCircleX = mDiameter;
                            else mCircleX = getWidth() - mDiameter;
                        } else {
                            if (mCircleX < -mDiameter * 3 || mCircleX > getWidth() + mDiameter * 3) {
                                moveOut();
                                sendJson();
                            }
                        }
                    }
                    if (mCircleY < mDiameter || getHeight() - mDiameter < mCircleY) {
                        if (Math.abs(mCircleVy) <= SPEED) {
                            mVib.vibrate(Math.abs((long) mCircleVy));
                            mCircleVy = -mCircleVy * 0.9;
                            mCircleAy = -mCircleAy;
                            if (mCircleY < mDiameter) mCircleY = mDiameter;
                            else mCircleY = getHeight() - mDiameter;
                        } else {
                            if (mCircleY < -mDiameter * 3 || mCircleY > getHeight() + mDiameter * 3) {
                                moveOut();
                                sendJson();
                            }
                        }
                    }
                    if (mTime > 10000) {
                        mPaint.setColor(Color.GRAY);
                        mManager.unregisterListener(this);
                        mCircleVx = 0;
                        mCircleAx = 0;
                        mCircleAy = 0.49f;
                        if (mCircleY == getHeight() - mDiameter) {
                            mCircleVy = 0;
                            mJson = "{\"game\":\"over\"}";
                            sendJson();
                            break;
                        }
                    }
                }
            }
        }
    }

    private void moveOut() {
        is = false;
        mCircleVx = 0;
        mCircleVy = 0;
        mStopTime = mCTime;
        mSTime += mStopTime;
        Log.d("Time", String.valueOf(mSTime));
        mJson = "{\"move\":\"out\"}";
    }

    private void sendJson() {
        if (mWebSocketClient.isClosed()) {
            mWebSocketClient.connect();
        }
        if (mWebSocketClient.isOpen()) {
            mWebSocketClient.send(mJson);
        }
    }
}
