package cn.edu.fudan.ee.cameraview;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector ;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;

import cn.edu.fudan.ee.cameraview.DataBase.LoadInitialParams;
import cn.edu.fudan.ee.cameraview.SocketCommunication.SocketService;

public class MainActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private GLSurfaceView glSurfaceView;
    private GLRenderer renderer;
    private Camera mCamera;
    private Camera.Parameters params;
    private Camera.Size previewSize;
    private int pixelAmounts;
    private int callbackBufferSize;
    private byte[] mCallbackBuffer;
    private SurfaceView surfaceView;
    private SurfaceHolder mHolder;
 //   private ByteBuffer frameData;

    public static CameraParamsHandler cameraParamsHandler;// Handler处理接收到的相机参数
    private SocketService mBoundService;
    private GestureDetector mGestureDetector;// 手势检测器
    private int initialZoom;// 手指触摸触摸屏时的初始相机zoom倍数
    LoadInitialParams loadInitialParams;// 加载相机初始参数类


    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Activity", "---------->>onCreate");
        super.onCreate(savedInstanceState);
        FrameLayout layout = new FrameLayout(this);
        setContentView(layout);
        surfaceView = new SurfaceView(this);
        glSurfaceView = new GLSurfaceView(this);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);

        Initialize_1();
        renderer = new GLRenderer(this,previewSize,pixelAmounts);
        Initialize_2();


//        frameData = ByteBuffer.allocateDirect(callbackBufferSize);

        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        layout.addView(surfaceView);
        layout.addView(glSurfaceView);
        glSurfaceView.setZOrderOnTop(true);
        layout.setKeepScreenOn(true);

        // 手势检测
        mGestureDetector = createGestureDetector(MainActivity.this);
        mGestureDetector.setOnDoubleTapListener(new DoubleTapListener());
        // Socket通信
        Intent intent = new Intent(MainActivity.this, SocketService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    public void Initialize_1()
    {
        mCamera = Camera.open();
        params = mCamera.getParameters();
        params.setPreviewSize(640,360);
        params.setPreviewFpsRange(30000,30000);
        params.setPreviewFormat(ImageFormat.NV21);
        previewSize = params.getPreviewSize();
        pixelAmounts = previewSize.height*previewSize.width;
    }

    public void Initialize_2()
    {
        cameraParamsHandler = new CameraParamsHandler(this, renderer, mCamera, params);
        loadInitialParams = LoadInitialParams.getInstance(this);
        loadInitialParams.myParams.params3 = false;
        Toast.makeText(MainActivity.this, "原始效果", Toast.LENGTH_SHORT).show();
        cameraParamsHandler.Zoom(MainActivity.this, loadInitialParams.myParams.params1);// 应用缩放效果
        cameraParamsHandler.WhiteBalance(MainActivity.this, loadInitialParams.myParams.params2);// 应用白平衡效果
        loadInitialParams.myParams.params4 = 100;// 原始画面比例为100%
        loadInitialParams.myParams.params5 = 0;// 原始画面起始横坐标为0
        loadInitialParams.myParams.params6 = 0;// 原始画面起始纵坐标为0
        mCamera.setParameters(params);
        callbackBufferSize = pixelAmounts * 3/2;
        mCallbackBuffer = new byte[callbackBufferSize];
    }


    protected void onResume() {
        Log.i("Activity", "---------->>onResume");
        super.onResume();
        glSurfaceView.onResume();
        if (mCamera == null) {
            Initialize_1();
            Initialize_2();
        }
    }


    protected void onPause() {
        Log.i("Activity", "---------->>onPause");
        super.onPause();
        glSurfaceView.onPause();
        if(mCamera!= null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCallbackBuffer = null;
            mCamera.release();
            mCamera = null;
        }
        unbindService(conn);
    }


    protected void onDestroy() {
        Log.i("Activity", "---------->>onDestroy");
        super.onDestroy();
    }


    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.setPreviewCallbackWithBuffer(this);
        mCamera.addCallbackBuffer(mCallbackBuffer);
        mCamera.startPreview();
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void onPreviewFrame(byte[] data, Camera camera) {

//        frameData.position(0);
//        frameData.put(data);
        renderer.setCapturedData(data);
        glSurfaceView.requestRender();
        mCamera.addCallbackBuffer(mCallbackBuffer);
    }

    private ServiceConnection conn = new ServiceConnection(){


        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBoundService = ((SocketService.LocalBinder)iBinder).getService();
        }


        public void onServiceDisconnected(ComponentName componentName) {
            mBoundService = null;
        }
    };

    // 手势检测器对相应手势做出处理
    // onFling()是甩，这个甩的动作是在一个MotionEvent.ACTION_UP(手指抬起)发生时执行，
    // onScroll()只要手指移动就会执行,不会执行MotionEvent.ACTION_UP。
    // onFling通常用来实现翻页效果，onScroll通常用来实现放大缩小和移动。
    private GestureDetector createGestureDetector(Context context)
    {
        GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {

            public boolean onDown(MotionEvent motionEvent) {
                // 按下
                Log.i("Gesture", "onDown");
                initialZoom = loadInitialParams.myParams.params1;
                return false;
            }


            public void onShowPress(MotionEvent motionEvent) {
                // down事件发生而move或者up还没发生前触发该事件
                Log.i("Gesture", "onShowPress");
            }

            public boolean onSingleTapUp(MotionEvent motionEvent) {
                // 手指离开触摸屏
                Log.i("Gesture", "onSingleTapUp");
                return false;
            }


            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
                // 手指在触摸屏上滑动
                Log.i("Gesture", "onScroll");
                float x = motionEvent.getX();
                float x2 = motionEvent2.getX();
                Log.i("motionEvent.getX()", ""+x);
                Log.i("motionEvent2.getX()", ""+x2);
                if(x2 >= x)
                {
                    loadInitialParams.myParams.params1 = initialZoom+(int)((60.0f-initialZoom+1.0f)/1366*(x2-x));
                    Log.i("放大倍数", ""+ loadInitialParams.myParams.params1);
                }
                else
                {
                    loadInitialParams.myParams.params1 = initialZoom-(int)((initialZoom-0.0f+1.0f)/1366*(x-x2));
                    Log.i("缩小倍数", ""+ loadInitialParams.myParams.params1);
                }
                //两种方式都可以
//                mPreview.params.setZoom(loadInitialParams.myParams.params1);
//                mPreview.mCamera.setParameters(mPreview.params);
                Message msg = new Message();
                msg.what = 1;
                msg.obj = loadInitialParams.myParams;
                cameraParamsHandler.myHandler.sendMessage(msg);

                return false;
            }


            public void onLongPress(MotionEvent motionEvent) {
                // 手指按下一段时间，并且没有松开
                Log.i("Gesture", "onLongPress");
                loadInitialParams.myParams.params3 = !loadInitialParams.myParams.params3;
                Message msg = new Message();
                msg.what = 2;
                msg.obj = loadInitialParams.myParams;
                cameraParamsHandler.myHandler.sendMessage(msg);
            }


            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
                // 手指在触摸屏上迅速移动，并松开的动作
                Log.i("Gesture", "onFling");
                return false;
            }
        });
        return gestureDetector;
    };

    // DoubleTapListener监听
    private class DoubleTapListener implements GestureDetector.OnDoubleTapListener{

        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.i("DoubleTapGesture", "onSingleTapConfirmed");
//            Toast.makeText(MainActivity.this, "onSingleTapConfirmed", Toast.LENGTH_SHORT).show();
            return true;
        }

        public boolean onDoubleTap(MotionEvent e) {
            Log.i("DoubleTapGesture", "onDoubleTap");
//            Toast.makeText(MainActivity.this, "onDoubleTap", Toast.LENGTH_SHORT).show();
            return true;
        }

        public boolean onDoubleTapEvent(MotionEvent e) {
//            Log.i("DoubleTapGesture", "onDoubleTapEvent");
//            Toast.makeText(MainActivity.this, "onDoubleTapEvent", Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    // 将事件发送到手势检测器

    public boolean onGenericMotionEvent(MotionEvent event)
    {
        if(mGestureDetector != null)
        {
            return mGestureDetector.onTouchEvent(event);
        }
        return false;
    }
}
