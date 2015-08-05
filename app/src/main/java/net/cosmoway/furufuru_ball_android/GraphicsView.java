package net.cosmoway.furufuru_ball_android;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class GraphicsView extends SurfaceView implements SurfaceHolder.Callback,
        SensorEventListener, Runnable, MyWebSocketClient.MyCallbacks {
    //Canvas
    private Canvas mCanvas;
    private int mWidth;
    private int mHeight;
    // 円の半径
    //private final int INIT_DIAMETER = 80;
    private int mDiameter;
    // 円のX,Y座標
    private float mCircleX = mDiameter;
    private float mCircleY = mDiameter;
    // Acceleration 加速
    private float[] mAcceleration;
    private float[] mLinearAcceleration;
    // 円の加速度
    private float mCircleAx = 0.0f;
    private float mCircleAy = 0.0f;
    // 円の移動量
    private double mCircleVx = 0.0d;
    private double mCircleVy = 0.0d;
    private static final  double REBOUND=0.9;
    // 描画用
    private Paint mPaint;
    // Loop
    private Thread mLoop;
    // SensorManager
    private SensorManager mManager;
    // Delay of sensor
    private final int SENSOR_DELAY;
    // Vibration 振動
    private Vibrator mVib;
    private MyWebSocketClient mWebSocketClient;
    // Speed(scalar)
    private static final int SPEED = 100;
    // Flag
    private boolean is = false;
    // Timer
    private long mTime;
    private long mCurrentTime;
    private long mSTime;
    private long mStartTime;
    private long mStopTime;
    // Handler
    private Handler mHandler;


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
        Display display = window.getDefaultDisplay();
        Point size = new Point();
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
        if (android.os.Build.VERSION.SDK_INT < 14) {
            mWidth = display.getWidth();
            mHeight = display.getHeight();
        } else {
            display.getSize(size);
            mWidth = size.x;
            mHeight = size.y;
        }
        mHandler = new Handler();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // SurfaceView生成時に呼び出されるメソッド。
        // 今はとりあえず背景を青にするだけ。
        mCanvas = holder.lockCanvas();
        mCanvas.drawColor(Color.BLUE);
        holder.unlockCanvasAndPost(mCanvas);
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
        mDiameter = mWidth / 10;
        mCircleX = -mDiameter * 3;
        mCircleY = 0;
        mCircleVx = 30;
        mCurrentTime = 0;
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

        mCircleAx = -(mAcceleration[0] / 10 + mLinearAcceleration[0] / 2);
        mCircleAy = mAcceleration[1] / 10 + mLinearAcceleration[1] / 2;
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
        if (mCircleX > mWidth + mDiameter * 3) {
            mCircleX = (float) (mWidth + mDiameter * 3);
            mCircleVx = -30;
        } else if (mCircleX < -mDiameter * 3) {
            mCircleX = (float) (-mDiameter * 2 + mCircleVx);
            mCircleVx = 30;
        }
        if (mCircleY > mHeight + mDiameter * 2) {
            mCircleY = (float) (mHeight + mDiameter * 3);
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
        mManager.unregisterListener(this);
        is = false;
        Log.d("GV", "GameOver");
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                // タイトル設定
                alert.setTitle("ふるふるボール");
                // ページ作成.(適当に)
                if (mTime > 10000) {
                    alert.setMessage("Time Over");
                } else {
                    alert.setMessage("あなたの記録は" + ((double) mTime / 1000) + "秒でした。");
                }
                alert.setPositiveButton("閉じる", null);
                alert.show();
            }
        });
        mWebSocketClient.close();
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
                mCanvas = getHolder().lockCanvas();
                if (mCanvas != null) {
                    mCanvas.drawColor(Color.BLUE);
                    // 円を描画する
                    mCanvas.drawCircle(mCircleX, mCircleY, mDiameter, mPaint);
                    getHolder().unlockCanvasAndPost(mCanvas);
                    mCurrentTime = System.currentTimeMillis() - mStartTime;
                    mTime = mCurrentTime + mSTime;
                    // 円の座標を移動させる
                    mCircleVx += mCircleAx * SENSOR_DELAY;
                    mCircleVy += mCircleAy * SENSOR_DELAY;
                    mCircleX += mCircleVx * SENSOR_DELAY;
                    mCircleY += mCircleVy * SENSOR_DELAY;

                    // 画面の領域を超えた？
                    onCollision();
                    if (mTime > 10000) {
                        mPaint.setColor(Color.GRAY);
                        //10秒経過したら灰色となりタイムオーバー
                        mManager.unregisterListener(this);
                        mCircleVx = 0;
                        mCircleAx = 0;
                        mCircleAy = 0.98f;
                        //重力に任せて下に落ちる
                        if (mCircleY == mHeight - mDiameter * 3) {
                            mCircleVy = 0;
                            mCircleY = mHeight - mDiameter;
                            sendJson("{\"game\":\"over\"}");
                            break;
                        }
                    }
                }
            }
        }
    }
    private void onCollision() {
        if (mCircleX < mDiameter || mWidth - mDiameter < mCircleX) {
            if (Math.abs(mCircleVx) <= SPEED) {
                mVib.vibrate(Math.abs((long) mCircleVx));
                //ぶつかって跳ね返る
                mCircleVx = -mCircleVx * REBOUND;
                mCircleAx = -mCircleAx;
                if (mCircleX < mDiameter) mCircleX = mDiameter;
                else mCircleX = mWidth - mDiameter;
            } else {
                if (mCircleX < -mDiameter * 3 || mCircleX > mWidth + mDiameter * 3) {
                    //壁を抜けて相手（自分）にボールが渡る
                    moveOut();
                    sendJson("{\"move\":\"out\"}");
                }
            }
        }
        if (mCircleY < mDiameter || mCircleY > mHeight - mDiameter * 3) {
            if (Math.abs(mCircleVy) <= SPEED) {
                //ぶつかって跳ね返る
                mVib.vibrate(Math.abs((long) mCircleVy));
                mCircleVy = -mCircleVy * REBOUND;
                mCircleAy = -mCircleAy;
                if (mCircleY < mDiameter) mCircleY = mDiameter;
                else mCircleY = mHeight - mDiameter * 3;
            } else {
                if (mCircleY < -mDiameter * 3 || mCircleY > mHeight + mDiameter * 3) {
                    //壁を抜けて相手（自分）にボールが渡る
                    moveOut();
                    sendJson("{\"move\":\"out\"}");
                }
            }
        }
    }

    private void moveOut() {
        is = false;
        mCircleVx = 0;
        mCircleVy = 0;
        mStopTime = mCurrentTime;
        mSTime += mStopTime;
        Log.d("Time", String.valueOf(mSTime));
    }

    private void sendJson(String json ) {
        if (mWebSocketClient.isClosed()) {
            mWebSocketClient.connect();
        }
        if (mWebSocketClient.isOpen()) {
            mWebSocketClient.send(json);
        }
    }
}
