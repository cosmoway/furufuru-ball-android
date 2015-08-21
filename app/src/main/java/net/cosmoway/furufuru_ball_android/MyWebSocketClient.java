package net.cosmoway.furufuru_ball_android;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class MyWebSocketClient extends WebSocketClient {

    private static final String TAG = "MyWebSocketClient";

    public interface MyCallbacks {
        void moveIn();

        void join(int count);

        void gameOver();

        void start();
    }

    private MyCallbacks mCallbacks;

    public void setCallbacks(MyCallbacks myCallbacks) {
        mCallbacks = myCallbacks;
    }


    public static MyWebSocketClient newInstance() {
        URI uri = null;
        try {
            uri = new URI("ws://furufuru-ball.herokuapp.com");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return new MyWebSocketClient(uri);
    }

    public MyWebSocketClient(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG, "Connected");
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "Massage: " + message);
        JSONObject json = null;
        try {
            json = new JSONObject(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String str;
        try {
            if (json != null) {
                str = json.getString("move");
                if (str != null) {
                    if (str.equals("in")) {
                        mCallbacks.moveIn();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (json != null) {
                str = json.getString("player");
                if (str != null) {
                    if (str.equals("change")) {
                        int count = json.getInt("count");
                        mCallbacks.join(count);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (json != null) {
                str = json.getString("game");
                if (str != null) {
                    if (str.equals("over")) {
                        mCallbacks.gameOver();
                    } else if (str.equals("start")) {
                        mCallbacks.start();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG, "Connection suspended: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.w(TAG, "Connection failed:" + ex.getMessage());
    }

    public boolean isOpen() {
        return getConnection().isOpen();
    }

    public boolean isClosed() {
        return getConnection().isClosed();
    }
}