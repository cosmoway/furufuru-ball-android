package net.cosmoway.furufuru_ball_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class GraphicsView extends View{

    public GraphicsView(Context context) {
        super(context);
        // Set the color of background.
        setBackgroundColor(Color.BLUE);
    }
    // Override the method as onDraw().
    // Draw graphic use instance of class as canvas.
    @Override
    protected void onDraw(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        // Draw the circle.
        paint.setAntiAlias(true);
        canvas.drawCircle(120,200,100,paint);
    }
}
