package net.cosmoway.furufuru_ball_android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {
    /** Called when the activity is first created. */

    private SensorManager manager;
    private Sensor sensor;
    private SensorSurfaceView surfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surfaceView = new SensorSurfaceView(this);
        setContentView(surfaceView);

        // センサーマネージャの取得
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // センサーの取得
        List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0)
            sensor = sensors.get(0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // センサーの処理の開始
        if (sensor != null) {
            manager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // センサーの処理の停止
        manager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        surfaceView.onValueChanged(event.values);
    }

    class SensorSurfaceView extends SurfaceView implements
            SurfaceHolder.Callback {

        private Bitmap bitmap;
        private float x, y;

        public SensorSurfaceView(Context context) {
            super(context);
            getHolder().addCallback(this);
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.icon);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }

        public void onValueChanged(float[] values) {
            x -= values[0];
            y += values[1];

            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(bitmap, x, y, null);
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }
}