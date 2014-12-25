package cn.edu.fudan.ee.glasscameracontrol.DataBase;

import java.io.File;
import java.io.IOException;

/**
 * Created by hbj on 2014/12/18.
 */
public class Constants {

    // 保存所有数据的根文件夹
    public String getRootDirectoryOfData()
    {
        String rootDirectory = null;
        try {
            rootDirectory = new File("").getCanonicalPath() + "\\Data\\";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rootDirectory;
    }
}