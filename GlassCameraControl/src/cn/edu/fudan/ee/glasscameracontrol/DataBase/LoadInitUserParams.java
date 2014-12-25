package cn.edu.fudan.ee.glasscameracontrol.DataBase;

import java.io.*;

/**
 * 加载初始用户参数
 * 单例模式
 * Created by hbj on 2014/11/4.
 */
public class LoadInitUserParams {
    private static LoadInitUserParams loadInitUserParams = null;

    private LoadInitUserParams()
    {

    }

    public static synchronized LoadInitUserParams getInstance()
    {
        if(loadInitUserParams == null)
        {
            loadInitUserParams = new LoadInitUserParams();
        }
        return loadInitUserParams;
    }

    FileInputStream fi;
    ObjectInputStream oi;
    FileOutputStream fo;
    ObjectOutputStream os;
    String folderPath;// 保存用户初始参数的文件夹路径(在本工程路径下)
    String fileName;// 保存用户初始参数的文件名
    String filePath;// 保存用户初始参数的文件路径
    public InitUserParams myParams = getInitialUserParams();

    public InitUserParams getInitialUserParams()
    {
        if(myParams == null)
        {
            myParams = createOrLoadParamsFromFile();
        }
        return myParams;
    }

    public void saveParams(InitUserParams initUserParams)
    {
        saveParamsToFile(initUserParams);
    }

    // 判断是否存在保存用户初始参数的.dat文件
    // 如果没有此文件，即没有初始用户参数，要创建；
    // 如果已经存在此文件，则从此文件读取保存的用户初始参数
    public InitUserParams createOrLoadParamsFromFile()
    {
        InitUserParams initUserParams = null;
        // 获取保存相机参数的文件
        folderPath = new Constants().getRootDirectoryOfData();
        fileName = "savedInitUserParams.dat";
        filePath = folderPath + fileName;
        File folder = new File(folderPath);
        if(!folder.exists()&&!folder.isDirectory())
        {
            folder.mkdir();// 若不存在保存用户初始参数的文件所在的文件夹，则创建（同时也说明了不存在保存用户初始参数的文件）
            System.out.println("文件夹不存在，已创建");
        }
        else
        {
            System.out.println("文件夹已存在");
        }
        File file = new File(filePath);
        if(!file.exists())
        {
            initUserParams = new InitUserParams();// 第一次使用此服务端，用户初始参数要初次实例化
            try {
                file.createNewFile();// 创建保存用户初始参数的文件
                System.out.println("第一次运行此服务端，创建保存用户初始参数的.dat文件");
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            saveParamsToFile(initUserParams);// 保存用户初始参数到文件
        }
        else// 打开服务端时自动加载参数，用于在之后初始化控件后设置控件的参数值
        {
            initUserParams = loadParamsFromFile();
            System.out.println("存在保存用户初始参数的.dat文件");
        }
        return initUserParams;
    }

    // 从文件加载参数
    public InitUserParams loadParamsFromFile()
    {
        InitUserParams initUserParams = null;
        try {
            fi = new FileInputStream(filePath);
            oi = new ObjectInputStream(fi);
            initUserParams = (InitUserParams) oi.readObject();
            oi.close();
            fi.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return initUserParams;
    }

    // 保存参数到文件
    public void saveParamsToFile(InitUserParams initUserParams)
    {
        try {
            fo = new FileOutputStream(filePath);
            os = new ObjectOutputStream(fo);
            os.writeObject(initUserParams);
            os.close();
            fo.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
