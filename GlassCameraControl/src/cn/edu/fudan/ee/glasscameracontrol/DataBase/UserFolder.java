package cn.edu.fudan.ee.glasscameracontrol.DataBase;

import java.io.File;

/**
 * Created by hbj on 2014/11/30.
 * 1.新建用户时在项目目录下为其分配一个文件夹来存储其数据及图片
 * 2.因为文件夹是按"用户名字_用户生日"来命名的，所以更新用户数据时，如果名字或生日出现改动，则需要重命名文件夹
 * 3.删除用户时需删除其文件夹及文件夹下的文件
 */
public class UserFolder {

    private String rootDirectoryOfData= new Constants().getRootDirectoryOfData();

    // 新建用户时为其分配一个文件夹来存储其数据及图片
    public void createUserFolder(String folderName)
    {
        try
        {
            File folder = new File(rootDirectoryOfData + folderName);
            if(!folder.exists()&&!folder.isDirectory())
            {
                folder.mkdir();
                System.out.println("新建用户的文件夹不存在，已创建");
            }
            else
            {
                System.out.println("新建用户的文件夹已存在");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // 因为文件夹是按"用户名字_用户生日"来命名的，所以更新用户数据时，如果名字或生日出现改动，则需要重命名文件夹
    public void updateUserFolder(String oriFolderName, String newFolderName)
    {
        try
        {
            File folder = new File(rootDirectoryOfData + oriFolderName);
            if(!folder.exists()&&!folder.isDirectory())
            {
                System.out.println("所要更新用户的文件夹不存在");
            }
            else
            {
                folder.renameTo(new File(rootDirectoryOfData + newFolderName));
                System.out.println("所要更新用户的文件夹存在，已更新");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // 删除用户时需删除其文件夹及文件夹下的文件
    public void deleteUserFolder(String folderName)
    {
        try
        {
            File folder = new File(rootDirectoryOfData + folderName);
            if(!folder.exists()&&!folder.isDirectory())
            {
                System.out.println("所要删除的用户的文件夹不存在");
            }
            else
            {
                File[] files = folder.listFiles();// 先删除文件夹中的文件
                for(File file:files)
                {
                    file.delete();

                }
                folder.delete();// 再删除文件夹
            }
            System.out.println("存在所要删除的用户的文件夹，已删除");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
