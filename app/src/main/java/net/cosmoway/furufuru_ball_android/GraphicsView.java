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
    private int mCircleX = INIT_DIAMETER;
    private int mCircleY = INIT_DIAMETER;
    // 円の移動量
    private int mCircleVx = 5;
    private int mCircleVy = 5;
    // 描画用
    private Paint mPaint;
    // Loop
    private Thread mLoop;
    // SensorManager
    private SensorManager mManager;
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
            mManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
        }
        // スレッド開始
        mLoop.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Listenerの登録解除
        mManager.unregisterListener(this);
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
                mCircleX += mCircleVx;
                mCircleY += mCircleVy;
                // 画面の領域を超えた？
                if (mCircleX < mDiameter || getWidth() < mCircleX + mDiameter) {
                    mVib.vibrate(50);
                    mCircleVx *= -1;
                }
                if (mCircleY < mDiameter || getHeight() < mCircleY + mDiameter) {
                    mVib.vibrate(50);
                    mCircleVy *= -1;
                }
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            String str = "Values:"
                    + "\nX:" + event.values[SensorManager.DATA_X]
                    + "\nY:" + event.values[SensorManager.DATA_Y]
                    + "\nZ:" + event.values[SensorManager.DATA_Z];
            Log.d("Values",str);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}