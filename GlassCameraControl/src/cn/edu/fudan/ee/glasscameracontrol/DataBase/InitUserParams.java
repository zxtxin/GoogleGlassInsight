package cn.edu.fudan.ee.glasscameracontrol.DataBase;

import java.io.Serializable;

/**
 * Created by hbj on 2014/12/15.
 */
public class InitUserParams implements Serializable {
    public int indexOfUser = -1;
    public String userName;
    public String userBirthdate;
    public String userSex;
    public String complementaryInfo;
}