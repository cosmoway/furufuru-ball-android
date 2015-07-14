package net.cosmoway.furufuru_ball_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GraphicsView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    // 円の直径
    private final int INIT_DIAMETER = 40;
    private int mDiameter = INIT_DIAMETER;
    // 円のX,Y座標
    private int mCircleX = INIT_DIAMETER;
    private int mCircleY = INIT_DIAMETER;
    // 円の移動量
    private int mCircleVx = (int) (Math.random() * 10) + 1;
    private int mCircleVy = mCircleVx;
    // 描画用
    private Paint mPaint;
    // Loop
    private Thread mLoop;
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
        // Get the system-service of vibrator.
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
        // スレッド開始
        mLoop.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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
                    if (Math.abs(mCircleVx) <= 5) {
                        mVib.vibrate(50);
                        mCircleVx *= -1;
                    } else {
                        if (mCircleX < mDiameter) {
                            mCircleX = getWidth() - mDiameter;
                            //break;
                        } else {
                            mCircleX = mDiameter;
                        }
                    }
                }
            }
            if (mCircleY < mDiameter || getHeight() < mCircleY + mDiameter) {
                if (Math.abs(mCircleVy) <= 5) {
                    mVib.vibrate(50);
                    mCircleVy *= -1;
                } else {
                    if (mCircleY < mDiameter) {
                        mCircleY = getHeight() - mDiameter;
                    } else {
                        mCircleY = mDiameter;
                        //break;
                    }
                }
            }
        }
    }
}