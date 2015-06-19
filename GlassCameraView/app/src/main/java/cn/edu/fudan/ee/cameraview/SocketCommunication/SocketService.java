package cn.edu.fudan.ee.cameraview.SocketCommunication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URISyntaxException;

import cn.edu.fudan.ee.cameraview.MainActivity;
import cn.edu.fudan.ee.glasscamera.CameraParams;

/**
 * Created by zxtxin on 2014/9/17.
 */

public class SocketService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private Socket socket;


    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    public class LocalBinder extends Binder
    {
        public SocketService getService()
        {
          return SocketService.this;
        }
    }



    // 当进行通信的时候，turnOnCommunication为true，一直与PC进行发送、接收数据的过程
    // 当PC端界面关闭时，turnOnCommunication为false，退出发送、接收循环，重新循环等待serverSocket.accept()
    public static boolean turnOnCommunication;


    public void onCreate()
    {
        Log.i("Service", "---------->>onCreate");
        super.onCreate();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    socket = IO.socket("http://192.168.23.1:8000");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                socket.on("send_data",new Emitter.Listener(){
                    @Override
                    public void call(Object... args) {
                        JSONObject jsonReceived = null;
                        try {
                            jsonReceived = new JSONObject(new JSONTokener(args[0].toString()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.e("JSON",jsonReceived.toString());
                        CameraParams obj = new CameraParams();
                        try {
                            obj.params1 = jsonReceived.getInt("1");
                            obj.params2 = jsonReceived.getInt("2");
                            obj.params3 = jsonReceived.getInt("3")==1?true:false;
                            obj.params4 = jsonReceived.getInt("4");
                            obj.params5 = jsonReceived.getInt("5");
                            obj.params6 = jsonReceived.getInt("6");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Message msg = new Message();
                        msg.what = 0;
                        msg.obj = obj;
                        MainActivity.cameraParamsHandler.myHandler.sendMessage(msg);
                        Log.i("Send received message from server  to handler", "sent");
                    }
                }).on(Socket.EVENT_DISCONNECT,new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        //        Toast.makeText(getApplicationContext(),getString(R.string.disconnected),Toast.LENGTH_SHORT);
                        socket.connect();
                    }
                }).on(Socket.EVENT_CONNECT,new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        //  Toast.makeText(getApplicationContext(),getString(R.string.connected),Toast.LENGTH_SHORT);

                    }
                });

                socket.connect();
                Log.i("Connection",""+socket.connected());

            }
        }).start();
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.i("Service", "---------->>onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy()
    {
        Log.i("Service", "---------->>onDestroy");
        super.onDestroy();
    }
}
