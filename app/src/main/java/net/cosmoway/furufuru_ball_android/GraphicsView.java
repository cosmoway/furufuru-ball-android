package net.cosmoway.furufuru_ball_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GraphicsView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    // 円のX,Y座標
    private int mCircleX = 0;
    private int mCircleY = 0;
    // 円の移動量
    private int mCircleVx = 5;
    private int mCircleVy = 5;
    // 描画用
    private Paint mPaint;
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
        mVib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // スレッド開始
        Thread loop = new Thread(this);
        loop.start();
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
                canvas.drawCircle(mCircleX, mCircleY, 40, mPaint);
                getHolder().unlockCanvasAndPost(canvas);
                // 円の座標を移動させる
                mCircleX += mCircleVx;
                mCircleY += mCircleVy;
                // 画面の領域を超えた？
                if (mCircleX < 0 || getWidth() < mCircleX) {
                    mVib.vibrate(50);
                    mCircleVx *= -1;
                }
                if (mCircleY < 0 || getHeight() < mCircleY) {
                    mVib.vibrate(50);
                    mCircleVy *= -1;
                }
            }
        }
    }
}