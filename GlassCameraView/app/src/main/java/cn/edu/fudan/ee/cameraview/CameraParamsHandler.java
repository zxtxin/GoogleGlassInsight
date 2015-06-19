package cn.edu.fudan.ee.cameraview;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import cn.edu.fudan.ee.cameraview.DataBase.LoadInitialParams;
import cn.edu.fudan.ee.cameraview.SocketCommunication.SocketService;
import cn.edu.fudan.ee.glasscamera.CameraParams;

/**
 * Created by hbj on 2014/12/4.
 */
public class CameraParamsHandler {
    public Context mContext;
    public GLRenderer mRenderer;
    public Camera mCamera;
    public Camera.Parameters params;
    public Handler myHandler;
    CameraParams receiveParams;// 用于在handler中保存接收的相机参数
    LoadInitialParams loadInitialParams;// 加载相机初始参数类

    public CameraParamsHandler(final Context context, final GLRenderer renderer, Camera camera, Camera.Parameters parameters) {
        this.mContext = context;
        this.mRenderer = renderer;
        this.mCamera = camera;
        this.params = parameters;

        loadInitialParams = LoadInitialParams.getInstance(context);

        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // 处理接收的消息
                super.handleMessage(msg);
                receiveParams = (CameraParams)msg.obj;
                Log.i("receive Parameters", "received");

                switch (msg.what) {
                    case 0:// Message是由socket读取PC传送的数据时发出的
                        // 相机参数修改
                        if(receiveParams.params3 != loadInitialParams.myParams.params3)
                        {
                            SwitchBetweenOriAndHist(context, receiveParams.params3);// 切换原始效果或直方图均衡效果
                        }
                        if(receiveParams.params1 != loadInitialParams.myParams.params1)
                        {
                            Zoom(context, receiveParams.params1);// 缩放
                        }
                        if(receiveParams.params2 != loadInitialParams.myParams.params2)
                        {
                            WhiteBalance(context, receiveParams.params2);// 白平衡
                        }
                        if((receiveParams.params4 != loadInitialParams.myParams.params4) ||
                                (receiveParams.params5 != loadInitialParams.myParams.params5) ||
                                (receiveParams.params6 != loadInitialParams.myParams.params6))
                        {
                            setViewPortSize(context, receiveParams.params5, receiveParams.params6, receiveParams.params4);// 设置视口
                        }
                        break;
                    case 1:// Message是由缩放发出的
                        Zoom(context, receiveParams.params1);// 缩放
                        break;
                    case 2:// Message是由切换RGBFilter与HistEqFilter时发出的
                        SwitchBetweenOriAndHist(context, receiveParams.params3);// 切换原始效果或直方图均衡效果
                        break;
                    default:
                        break;
                }

                loadInitialParams.myParams = receiveParams;

                // 相机参数修改生效
                mCamera.setParameters(params);
                Log.i("Change camera parameters","changed");


                // 每次接收到相机参数，立即保存相机参数到glass的内存中
                try {
                    loadInitialParams.saveParamsToFile(receiveParams);
                }
                catch(Throwable e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    // 缩放
    public void Zoom(Context context, int choose_zoom)
    {
        Log.i("former Zoom","" + params.getZoom());
        params.setZoom(choose_zoom);
        Log.i("later Zoom","" + params.getZoom());
    }

    // 白平衡
    // The range of the choose is limited to [0, 10]
    public void WhiteBalance(Context context, int choose_whitebalance)
    {
        String[] effect_WhiteBalance = new String[]{params.WHITE_BALANCE_AUTO, params.WHITE_BALANCE_DAYLIGHT, params.WHITE_BALANCE_CLOUDY_DAYLIGHT,
                "tungsten", params.WHITE_BALANCE_FLUORESCENT, params.WHITE_BALANCE_INCANDESCENT, "horizon", "sunset",params.WHITE_BALANCE_SHADE,
                params.WHITE_BALANCE_TWILIGHT, params.WHITE_BALANCE_WARM_FLUORESCENT};
        String[] effect_WhiteBalance_description = new String[]{"自动模式", "日光模式", "阴天模式", "钨丝灯模式",
                "荧光灯模式", "白炽灯模式", "地平线模式", "日落模式",
                "阴影模式", "黄昏模式", "暖色荧光灯模式"};
        Log.i("former WhiteBalance","" + params.getWhiteBalance());
        params.setWhiteBalance(effect_WhiteBalance[choose_whitebalance]);
        Toast.makeText(context, effect_WhiteBalance_description[choose_whitebalance], Toast.LENGTH_SHORT).show();
        Log.i("later WhiteBalance","" + params.getWhiteBalance());
    }

    // 切换原始效果或直方图均衡效果
    public void SwitchBetweenOriAndHist(Context context, boolean choose_filter)
    {
        String[] effect_histogram_description = new String[]{"原始效果", "直方图均衡效果"};
        while(!mRenderer.getDrawFrameStatus());// 当mRenderer的onDrawFrame还未完成一帧后，则一直等待，一帧完成后才能setFilter
        mRenderer.setFilter(choose_filter);
        Toast.makeText(context, effect_histogram_description[choose_filter ? 1 : 0], Toast.LENGTH_SHORT).show();
        Log.i("statusOfParams3", "" + choose_filter);
    }

    // 设置视口
    public void setViewPortSize(Context context, int viewX, int viewY, int choose_percentageOfSize)
    {
        while(!mRenderer.getDrawFrameStatus());// 当mRenderer的onDrawFrame还未完成一帧后，则一直等待，一帧完成后才能setFilter
        mRenderer.setViewPortSize(viewX, viewY, choose_percentageOfSize);
        Log.i("statusOfParams4", "" + choose_percentageOfSize);
    }
}