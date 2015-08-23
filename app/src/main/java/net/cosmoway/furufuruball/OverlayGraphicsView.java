package net.cosmoway.furufuruball;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

public class OverlayGraphicsView implements SurfaceHolder.Callback {

    @Override
    public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {
        Canvas mCanvas = holder.lockCanvas();
        Paint mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setAlpha(64);
        mCanvas.drawPaint(mPaint);
        holder.unlockCanvasAndPost(mCanvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
}
