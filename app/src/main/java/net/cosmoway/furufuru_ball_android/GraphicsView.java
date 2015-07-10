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

import java.util.List;

public class GraphicsView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener
        , Runnable {
    // 円の直径
    private final int INIT_DIAMETER = 40;
    private int mDiameter = INIT_DIAMETER;
    // 円のX,Y座標
    private float mCircleX = INIT_DIAMETER;
    private float mCircleY = INIT_DIAMETER;
    // Acceleraton
    private float[] mAcceleration;
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
        mLoop = new Thread(this);
        // Initializeing of acceleraton.
        mAcceleration = new float[]{0.0f, 0.0f, 0.0f};
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
        // Regist the service of sensor.
        List<Sensor> sensors = mManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            Sensor s = sensors.get(0);
            mManager.registerListener(this, s, SENSOR_DELAY);
        }
        // スレッド開始
        mLoop.start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAcceleration = event.values.clone();
            mCircleAx = -mAcceleration[0] / 100;
            mCircleAy = mAcceleration[1] / 100;
            mCircleAz = mAcceleration[2] / 100;
            String str = "Acceleration:"
                    + "\nX:" + mCircleAx * 100
                    + "\nY:" + mCircleAy * 100
                    + "\nZ:" + mCircleAz * 100;
            Log.d("Acceleration", str);
        }
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
                    mVib.vibrate(50);
                    mCircleVx = -mCircleVx;
                    mCircleAx = -mCircleAx;
                    if (mCircleX < mDiameter) mCircleX = mDiameter;
                    else mCircleX = getWidth() - mDiameter;
                }
                if (mCircleY < mDiameter || getHeight() - mDiameter < mCircleY) {
                    mVib.vibrate(50);
                    mCircleVy = -mCircleVy;
                    mCircleAy = -mCircleAy;
                    if (mCircleY < mDiameter) mCircleY = mDiameter;
                    else mCircleY = getHeight() - mDiameter;
                }
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Listenerの登録解除
        mManager.unregisterListener(this);
    }
}