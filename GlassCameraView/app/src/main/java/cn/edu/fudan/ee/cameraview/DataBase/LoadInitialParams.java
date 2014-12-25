package cn.edu.fudan.ee.cameraview.DataBase;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cn.edu.fudan.ee.glasscamera.CameraParams;

/**
 * 单例模式
 * 加载相机初始参数类
 * Created by hbj on 2014/11/4.
 */
public class LoadInitialParams {
    private static LoadInitialParams loadInitialParams = null;
    private static Context mContext;

    private LoadInitialParams(Context context)
    {
        mContext = context;
    }

    public static synchronized LoadInitialParams getInstance(Context context)
    {
        mContext = context;
        if(loadInitialParams == null)
        {
            loadInitialParams = new LoadInitialParams(context);
        }
        return loadInitialParams;
    }

    FileInputStream fi;
    ObjectInputStream oi;
    FileOutputStream fo;
    ObjectOutputStream os;
    String fileName = "savedInitialParams.ser";// 保存相机参数的文件
    public CameraParams myParams = getInitialParams();

    public CameraParams getInitialParams()
    {
        if(myParams == null)
        {
            myParams = createOrLoadParamsFromFile();
        }
        return myParams;
    }

    // 启动相机时，加载保存相机参数的文件
    public CameraParams createOrLoadParamsFromFile()
    {
//        new File(mContext.getFilesDir(), fileName).delete();
        CameraParams cameraParams = null;
        File file = new File(mContext.getFilesDir(), fileName);
        if(!file.exists())
        {
            cameraParams = new CameraParams();// 第一次使用此Glass应用，相机要初次实例化
            try {
                file.createNewFile();// 创建保存相机参数的文件
                Log.i("loadParamsFromFile", "第一次运行此Glass应用，创建保存相机参数的.ser文件");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            saveParamsToFile(cameraParams);// 保存相机参数到文件
        }
        else// 打开Glass应用时自动加载参数，设置照相机的参数值
        {
            Log.i("loadParamsFromFile", "已存在保存相机参数的.ser文件");
            try {
                fi = mContext.openFileInput(fileName);
                oi = new ObjectInputStream(fi);
                cameraParams = (CameraParams)oi.readObject();
                oi.close();
                fi.close();
            }
            catch(ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            Log.i("Already finished loading Params?", "Finished");
            Log.i("Initial params1", ""+cameraParams.params1);
            Log.i("Initial params2", ""+cameraParams.params2);
        }
        return cameraParams;
    }

    // 保存参数到文件
    public void saveParamsToFile(CameraParams cameraParams)
    {
        try {
            fo = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fo);
            os.writeObject(cameraParams);
            os.close();
            fo.close();
        }
        catch(IOException e1)
        {
            e1.printStackTrace();
        }
        Log.i("Saved?", "success");
        Log.i("Saved params1", ""+cameraParams.params1);
        Log.i("Saved params2", ""+cameraParams.params2);
    }
}