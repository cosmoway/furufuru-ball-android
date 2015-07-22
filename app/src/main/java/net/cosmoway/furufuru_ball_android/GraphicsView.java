package net.cosmoway.furufuru_ball_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.List;
public class GraphicsView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener
        , Runnable, MyWebSocketClient.MyCallbacks {
    // 円の直径
    private final int INIT_DIAMETER = 80;
    private int mDiameter = INIT_DIAMETER;
    // 円のX,Y座標
    private float mCircleX = INIT_DIAMETER;
    private float mCircleY = INIT_DIAMETER;
    // Acceleraton
    private float[] mAcceleration;
    private float[] mLinearAcceleration;
    // 円の加速度
    private float mCircleAx = 0.0f;
    private float mCircleAy = 0.0f;
    private float mCircleAz = 0.0f;
    // 円の移動量
    private double mCircleVx = 0.0d;
    private double mCircleVy = 0.0d;
    // 描画用
    private Paint mPaint;
    // Loop
    private Thread mLoop;
    // SensorManager
    private SensorManager mManager;

    //
    private final int SENSOR_DELAY;
    // Vibration
    private Vibrator mVib;
    private MyWebSocketClient mWebSocketClient;
    //
    private final int SPEED = 50;

    // Constructor
    public GraphicsView(Context context) {
        super(context);
        // SurfaceView描画に用いるコールバックを登録する。
        getHolder().addCallback(this);
        // 描画用の準備
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        // Get the system-service.
        mManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mVib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mWebSocketClient = MyWebSocketClient.newInstance();
        mWebSocketClient.setCallbacks(this);
        mLoop = new Thread(this);
        // Initializeing of acceleraton.
        mAcceleration = new float[]{0.0f, 0.0f, 0.0f};
        mLinearAcceleration = new float[]{0.0f, 0.0f, 0.0f};
        SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;
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
        // スレッド開始
        mLoop.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
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
        mCircleAz = mAcceleration[2] / 20;
        String str = "Acceleration:"
                + "\nX:" + mCircleAx
                + "\nY:" + mCircleAy
                + "\nZ:" + mCircleAz;
        Log.d("Acceleration", str);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void run() {
        // Runnableインターフェースをimplementsしているので、runメソッドを実装する
        // これは、Threadクラスのコンストラクタに渡すために用いる。
        while (true) {
            Canvas canvas = getHolder().lockCanvas();
            String json = null;
            if (canvas != null) {
                canvas.drawColor(Color.BLUE);
                // 円を描画する
                canvas.drawCircle(mCircleX, mCircleY, mDiameter, mPaint);
                getHolder().unlockCanvasAndPost(canvas);
                // 円の座標を移動させる
                mCircleVx += mCircleAx * SENSOR_DELAY;
                mCircleVy += mCircleAy * SENSOR_DELAY;
                mCircleX += mCircleVx * SENSOR_DELAY;
                mCircleY += mCircleVy * SENSOR_DELAY;
                // 画面の領域を超えた？
                if (mCircleX < mDiameter || getWidth() - mDiameter < mCircleX) {
                    if (Math.abs(mCircleVx) <= SPEED) {
                        mVib.vibrate(50);
                        mCircleVx = -mCircleVx * 0.9;
                        mCircleAx = -mCircleAx;
                        if (mCircleX < mDiameter) mCircleX = mDiameter;
                        else mCircleX = getWidth() - mDiameter;
                    } else {
                        if (mCircleX < -(mDiameter * 2) || mCircleX > getWidth() + mDiameter * 2) {
                            json = "{\"move\":\"out\"}";
                            //break;
                        }
                    }
                }
                if (mCircleY < mDiameter || getHeight() - mDiameter < mCircleY) {
                    if (Math.abs(mCircleVy) <= SPEED) {
                        mVib.vibrate(50);
                        mCircleVy = -mCircleVy * 0.9;
                        mCircleAy = -mCircleAy;
                        if (mCircleY < mDiameter) mCircleY = mDiameter;
                        else mCircleY = getHeight() - mDiameter;
                    } else {
                        if (mCircleY < -(mDiameter * 2) || mCircleY > getHeight() + mDiameter * 2) {
                            json = "{\"move\":\"out\"}";
                            //break;
                        }
                    }
                }
                if (mWebSocketClient.isOpen() && json != null) {
                    mWebSocketClient.send(json);
                } else if (mWebSocketClient.isClosed()) {
                    mWebSocketClient.connect();
                }
            }
        }
    }

    @Override
    public void callbackMethod() {
        run();
    }
}