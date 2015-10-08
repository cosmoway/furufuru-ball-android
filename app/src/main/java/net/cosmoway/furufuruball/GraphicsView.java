package net.cosmoway.furufuruball;

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
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class GraphicsView implements SurfaceHolder.Callback, SensorEventListener, Runnable {

    public interface Callback {
        void onGameStart();

        void onMoveOut();

        void onGameOver();
    }

    /**
     * 反発係数
     */
    private static final double COR = 0.9;

    /**
     * Delay of sensor
     */
    private static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;

    /**
     * Speed(scalar)
     */
    private static final int SPEED = 100;

    private Callback mCallback;
    private int mWidth;
    private int mHeight;
    // 円の半径
    private int mDiameter;
    // 円のX,Y座標
    private float mCircleX;
    private float mCircleY;
    // Acceleration 加速
    private float[] mAcceleration;
    private float[] mLinearAcceleration;
    // 円の加速度
    private float mCircleAx;
    private float mCircleAy;
    // 円の移動量
    private double mCircleVx;
    private double mCircleVy;
    // 描画用
    private Paint mCirclePaint;
    // SensorManager
    private SensorManager mManager;
    // Vibration 振動
    private Vibrator mVibrator;
    // Flag
    private boolean isMoveIn = false;
    // Timer
    protected long mTime;
    private long mCurrentTime;
    private long mSTime;
    private long mStartTime;
    private long mStopTime;

    private SurfaceHolder mHolder;

    private boolean isRunning;

    protected int mJoinCount;

    public static boolean isTimeUp(long timeMillis, int join) {
        return timeMillis >= Math.max(21 - join, 10) * 1000;
    }

    // Constructor
    public GraphicsView(Context context, SurfaceView surfaceView) {
        // SurfaceView描画に用いるコールバックを登録する。
        surfaceView.getHolder().addCallback(this);
        // ボール描画用の準備
        mCirclePaint = new Paint();
        // Get the system-service.
        mManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        init();
    }

    public void init() {
        Canvas canvas = mHolder.lockCanvas();
        canvas.drawColor(Color.rgb(240, 240, 235));
        mHolder.unlockCanvasAndPost(canvas);

        mCirclePaint.setColor(Color.rgb(57, 57, 57));
        mAcceleration = new float[]{0.0f, 0.0f, 0.0f};
        mLinearAcceleration = new float[]{0.0f, 0.0f, 0.0f};
        mJoinCount = 0;
        isMoveIn = false;
        isRunning = false;
        // 円の加速度
        mCircleAx = 0.0f;
        mCircleAy = 0.0f;
        // 円の移動量
        mCircleVx = 0.0d;
        mCircleVy = 0.0d;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void onStart() {
        if (mCallback != null) {
            mCallback.onGameStart();
        }
        isRunning = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mManager.unregisterListener(this);
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

    public void join(int count) {mJoinCount = count;}

    public void start() {
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
        isRunning = true;
        mDiameter = mWidth / 10;
        mCircleX = -mDiameter * 3;
        mCircleY = 0;
        mCircleVx = 30;

        mTime = 0;
        mStopTime = 0;
        mCurrentTime = 0;
        mSTime = 0;
        new Thread(this).start();
    }

    public void moveIn() {
        isMoveIn = true;
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
    }

    public void gameOver() {
        mManager.unregisterListener(this);
        isMoveIn = false;
        Log.d("GV", "GameOver");
    }

    @Override
    public void run() {
        // Runnableインターフェースをimplementsしているので、runメソッドを実装する
        // これは、Threadクラスのコンストラクタに渡すために用いる。
        while (isRunning) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isMoveIn) {
                mCurrentTime = System.currentTimeMillis() - mStartTime;
                mTime = mCurrentTime + mSTime;
                // 円の座標を移動させる
                mCircleVx += mCircleAx * SENSOR_DELAY;
                mCircleVy += mCircleAy * SENSOR_DELAY;
                mCircleX += mCircleVx * SENSOR_DELAY;
                mCircleY += mCircleVy * SENSOR_DELAY;

                // 画面の領域を超えた？
                onCollision();
                if (GraphicsView.isTimeUp(mTime, mJoinCount)) {
                    mCirclePaint.setColor(Color.rgb(252, 238, 33));
                    //10秒経過したら灰色となりタイムオーバー
                    mManager.unregisterListener(this);
                    mCircleVx = 0;
                    mCircleAx = 0;
                    mCircleAy = 0.98f;
                    //重力に任せて下に落ちる
                    if (mCircleY == mHeight - mDiameter) {
                        isMoveIn = false;
                        mCircleVy = 0;
                        mCircleAy = 0;
                        mCircleY = mHeight - mDiameter;
                        if (mCallback != null) {
                            mCallback.onGameOver();
                        }
                    }
                }
                Canvas canvas = mHolder.lockCanvas();
                canvas.drawColor(Color.rgb(240, 240, 235));
                // 円を描画する
                canvas.drawCircle(mCircleX, mCircleY, mDiameter, mCirclePaint);
                mHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void onCollision() {
        if (mCircleX < mDiameter || mWidth - mDiameter < mCircleX) {
            if (Math.abs(mCircleVx) <= SPEED) {
                mVibrator.vibrate(Math.abs((long) mCircleVx));
                //ぶつかって跳ね返る
                mCircleVx = -mCircleVx * COR;
                mCircleAx = -mCircleAx;
                if (mCircleX < mDiameter) mCircleX = mDiameter;
                else mCircleX = mWidth - mDiameter;
            } else {
                if (mCircleX < -mDiameter * 3 || mCircleX > mWidth + mDiameter * 3) {
                    //壁を抜けて相手（自分）にボールが渡る
                    moveOut();
                }
            }
        }
        if (mCircleY < mDiameter || mHeight - mDiameter < mCircleY) {
            if (Math.abs(mCircleVy) <= SPEED) {
                //ぶつかって跳ね返る
                mVibrator.vibrate(Math.abs((long) mCircleVy));
                mCircleVy = -mCircleVy * COR;
                mCircleAy = -mCircleAy;
                if (mCircleY < mDiameter) mCircleY = mDiameter;
                else mCircleY = mHeight - mDiameter;
            } else {
                if (mCircleY < -mDiameter * 3 || mCircleY > mHeight + mDiameter * 3) {
                    //壁を抜けて相手（自分）にボールが渡る
                    moveOut();
                }
            }
        }
    }

    private void moveOut() {
        if (mCallback != null) {
            mCallback.onMoveOut();
        }
        isMoveIn = false;
        mCircleVx = 0;
        mCircleVy = 0;
        mStopTime = mCurrentTime;
        mSTime += mStopTime;
        Log.d("Time", String.valueOf(mSTime));
    }
}
