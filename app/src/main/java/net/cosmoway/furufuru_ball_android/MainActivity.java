package net.cosmoway.furufuru_ball_android;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new GraphicsView(this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Thread.interrupted();
    }
}