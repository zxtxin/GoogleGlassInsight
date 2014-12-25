package cn.edu.fudan.ee.cameraview.SocketCommunication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import cn.edu.fudan.ee.cameraview.MainActivity;
import cn.edu.fudan.ee.glasscamera.CameraParams;

/**
 * Created by zxtxin on 2014/9/17.
 */

public class SocketService extends Service {
    private final IBinder mBinder = new LocalBinder();

    @Override
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

    private ServerSocket serverSocket = null;
    public static Socket socket = null;
    private final int SERVER_PORT = 22222;
    public static ObjectInputStream objIn = null;// 用于socket通信
    public static ObjectOutputStream objOut = null;// 用于socket通信
    CameraParams cameraParams = null;
    // 当进行通信的时候，turnOnCommunication为true，一直与PC进行发送、接收数据的过程
    // 当PC端界面关闭时，turnOnCommunication为false，退出发送、接收循环，重新循环等待serverSocket.accept()
    public static boolean turnOnCommunication;

    @Override
    public void onCreate()
    {
        Log.i("Service", "---------->>onCreate");
        super.onCreate();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    serverSocket = new ServerSocket(SERVER_PORT);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                while(true)
                {
                    try
                    {
                        socket = serverSocket.accept();
                        Log.d("SocketServer", "Accepted");

                        objIn = new ObjectInputStream(socket.getInputStream());
                        Log.i("objIn","initialed");
                        objOut = new ObjectOutputStream(socket.getOutputStream());
                        Log.i("objOut","initialed");

                        turnOnCommunication = true;

                        while (turnOnCommunication)
                        {
                            try
                            {
                                // Input
                                Object obj = objIn.readObject();
                                Log.i("is received obj null?", (obj==null)+"");
                                if(obj != null) {
                                    cameraParams = (CameraParams)obj;
                                    Log.i("readObject","OK ");
                                    Log.i("receive myParams.params1 from PC", "params1 : "+cameraParams.params1);
                                    Log.i("receive myParams.params2 from PC", "params2 : "+cameraParams.params2);
                                    Message msg = new Message();
                                    msg.what = 0;
                                    msg.obj = obj;
                                    MainActivity.cameraParamsHandler.myHandler.sendMessage(msg);
                                    Log.i("Send received message from server  to handler", "sent");

                                    // Output在CameraParamsHandler.java的handler中实现

                                } else {
                                    turnOnCommunication = false;
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                                turnOnCommunication = false;
                                Log.i("Wrong","objIn");
                                break;
                            }
                            catch (ClassNotFoundException e)
                            {
                                e.printStackTrace();
                                turnOnCommunication = false;
                                break;
                            }
                        }
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                        turnOnCommunication = false;
                        Log.i("Wrong","socket");
                    }
                }
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
        // 在退出应用之前发送空的数据告诉PC
        try {
            if(objOut != null) {
                objOut.writeObject(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
