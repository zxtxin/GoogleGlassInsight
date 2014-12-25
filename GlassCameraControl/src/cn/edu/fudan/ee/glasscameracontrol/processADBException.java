package cn.edu.fudan.ee.glasscameracontrol;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by hbj on 2014/12/24.
 * 处理'adb kill-server' failed -- run manually if necessary.的情况
 */
public class processADBException {
    Process p1 = null;
    Process p2 = null;
    Process p3 = null;
    InputStream is = null;
    BufferedReader reader = null;
    String status = null;

    public processADBException() {
        try
        {
            p1 = Runtime.getRuntime().exec("adb devices");
            is = p1.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            status = reader.readLine();
            p1.waitFor();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        while(status == null)
        {
            System.out.println("solving the case : 'adb kill-server' failed -- run manually if necessary.");
            try
            {
                p2 = Runtime.getRuntime().exec("adb kill-server");
                p3 = Runtime.getRuntime().exec("adb start-server");
                p1 = Runtime.getRuntime().exec("adb devices");
                is = p1.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                status = reader.readLine();
                p1.waitFor();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        System.out.println("already solved the case : 'adb kill-server' failed -- run manually if necessary.");
        try
        {
            is.close();
            reader.close();
            p1.destroy();
            if(p2 != null)
            {
                p2.destroy();
            }
            if(p3 != null)
            {
                p3.destroy();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
