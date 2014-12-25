package cn.edu.fudan.ee.glasscameracontrol;

import cn.edu.fudan.ee.glasscamera.CameraParams;
import cn.edu.fudan.ee.glasscameracontrol.DataBase.Constants;
import cn.edu.fudan.ee.glasscameracontrol.DataBase.LoadInitCameraParams;
import cn.edu.fudan.ee.glasscameracontrol.DataBase.LoadInitUserParams;
import cn.edu.fudan.ee.glasscameracontrol.DataBase.UserFolder;
import cn.edu.fudan.ee.glasscameracontrol.GUIWidget.SelectUser;
import cn.edu.fudan.ee.glasscameracontrol.GUIWidget.UI;
import cn.edu.fudan.ee.glasscameracontrol.projectGlassScreentoPC.MainFrame;
import com.android.ddmlib.IDevice;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.regex.Pattern;

public class GlassCameraControl implements ChangeListener, ActionListener {
    int PC_LOCAL_PORT = 22222;
    int ANDROID_PORT = 22222;
    public static ADB mADB;
    IDevice[] mDevices;
    public static IDevice mDevice;
    static GlassCameraControl glass;
    private Socket socket;
    private Thread socketThread;// 开启线程进行socket通信的发送接收数据
    private Timer timer;// 定时3分钟发送数据到Glass，保持激活socket，防止TimeOutException
    private ObjectOutputStream transToGlass = null;// 用于socket通信发送数据
    private ObjectInputStream receiveFromGlass = null;// 用于socket通信接收数据
    public static UI myUI;// 创建界面
    public LoadInitCameraParams loadInitCameraParams = LoadInitCameraParams.getInstance();// 加载初始相机参数类
    /* 加载用户初始参数类
     * loadInitUserParams.myParams.indexOfUser表示用户的id，
     * 当界面未加载用户时，值为-1；
     * 当加载某一用户后，值为该用户在excel中的行号；
     * 当新建用户完成后，值为excel表格的最后一行。
     * 此索引用途：对加载进的用户进行更新；新建用户；删除用户
     */
    public LoadInitUserParams loadInitUserParams = LoadInitUserParams.getInstance();
    private MainFrame glassFrame;// 把Glass画面传送到PC
    public static boolean alreadyProjectToPC = false;// 判断是否已开启传送画面
    static String[] mArgs;
    private UserFolder userFolder = new UserFolder();// 用来为每个新建用户建立保存其数据及图片的文件夹，或者更新该文件夹，或者删除该文件夹
    boolean flag_device = false;// 判断是否已经连接设备
    boolean flag_socket = false;// 用来判断socket是不是断开了，如果socket通信断开了，就让用户重新从检测设备开始进行socket建立
    boolean flag_initConn = false;// 判断是不是刚与Glass建立起通信，如果是则发送PC端的初始参数到Glass对Glass进行初始化

    public GlassCameraControl() {

        // 处理'adb kill-server' failed -- run manually if necessary.的情况
        new processADBException();

        mADB = new ADB();
        mADB.initialize();

        // 引入界面
        myUI = new UI();

        // jPanel1中的控件监听
        myUI.loadAnUserParams.addActionListener(this);
        myUI.createNewUser.addActionListener(this);
        myUI.deleteUser.addActionListener(this);
        myUI.savedImages.addActionListener(this);

        // jPanel2中的控件监听
        // 开启或关闭直方图均衡
        myUI.histogram.addActionListener(this);
        // 检测设备：google glass
        myUI.detectDevice.addActionListener(this);
        // 建立socket通信
        myUI.setUpSocket.addActionListener(this);
        // 传送参数到Glass
        myUI.update.addActionListener(this);
        // 投影Glass画面到PC
        myUI.projectGlassScreenToPC.addActionListener(this);
        // 更新已有用户参数或保存新建用户的参数
        myUI.save.addActionListener(this);

        // 主窗体关闭
        myUI.jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                // 若通信过程中PC界面关闭，则发送空的数据给Glass，Glass识别出内容为空时，则认为socket断开，不再向PC发送数据
                if(flag_device && flag_socket)
                {
                    try {
                        transToGlass.writeObject(null);
                        transToGlass.reset();// writeObject后，一定要reset()

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        mArgs = args;
        glass = new GlassCameraControl();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // jPanel1中的控件的响应

        // jPanel2中的控件的响应
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // jPanel1中的控件的响应

        // 加载用户
        if(e.getSource() == myUI.loadAnUserParams)
        {
            System.out.println("加载用户参数");
            // 从excel读取用户数据显示在jTable中
            Object[] tableTitle = myUI.loadUserParams.readTitleFromExcel();
            final Object[][] tableData = myUI.loadUserParams.readAllUsersDataFromExcel();
            if(tableData.length == 0)// 若表中无数据，则弹出对话框提示
            {
                dialog(myUI.jFrame, "尚不存在用户!", "提示");
                myUI.displayUserInfoIntoJFrame("", "", "男", "", 0, 0, false);// 清空
                myUI.enableOrDisableUserInput(false);// 禁止用户输入
            }
            else
            {
                myUI.jFrame.setEnabled(false);// 屏蔽主窗体
                final SelectUser selectUser = new SelectUser(myUI, tableTitle, tableData);
                selectUser.jTable.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        super.mouseClicked(e);
                        if (e.getClickCount() == 2)// 双击操作
                        {
                            int selectedRow = selectUser.jTable.convertRowIndexToModel(selectUser.jTable.getSelectedRow());
                            System.out.println("所加载的用户的索引：" + selectedRow);

                            // 用户在JTable中的索引为selectedRow，则在excel中的索引为selectedRow + 1（因为第一行是标题）
                            // 这句话一定要放在displayUserInfoIntoJFrame之前，否则就没随其他参数一起被保存
                            loadInitUserParams.myParams.indexOfUser = selectedRow + 1;

                            // 将一个用户的所有信息加载到界面相应控件中，
                            // 最后一个参数true/false表示加载的同时是否保存参数到文件savedInitialCameraParams和savedInitialUserParams，
                            // 如果设置为true，则当用户加载完后会进行保存，如果点击关闭软件，则下次打开软件时会自动加载这次加载的用户
                            myUI.displayUserInfoIntoJFrame(tableData[selectedRow][0].toString(),
                                    tableData[selectedRow][1].toString(),
                                    tableData[selectedRow][2].toString(),
                                    tableData[selectedRow][3].toString(),
                                    Integer.parseInt(tableData[selectedRow][4].toString()),
                                    Integer.parseInt(tableData[selectedRow][5].toString()),
                                    true);

                            myUI.jFrame.setEnabled(true);// 使能主窗体
                            selectUser.jFrame.dispose();// 关闭窗体

                            myUI.save.setText("更新用户参数");
                            myUI.save.setEnabled(true);
                            myUI.deleteUser.setEnabled(true);// 已经加载一个用户，使能删除操作
                            myUI.savedImages.setEnabled(true);
                            myUI.enableOrDisableUserInput(true);// 允许用户输入

                            // 加载完后，若存在设备连接，则发送给Glass告知实时更新；若不存在设备连接，则只进行加载操作
                            sendDataToGlass();
                        }
                    }
                });
            }
        }

        // 新建用户
        if(e.getSource() == myUI.createNewUser)
        {
            System.out.println("新建用户");
            // 清空界面控件
            myUI.displayUserInfoIntoJFrame("", "", "男", "", 0, 0, false);// 这里设置false，表示不保存，是为了处理当界面已存在用户时，
                                                                          // 当执行新建用户操作时，如果并没有保存新建用户，
                                                                          // 而是退出软件，则下次打开时仍可以加载这次本来有的用户
            myUI.save.setText("保存新建用户的参数");
            myUI.save.setEnabled(true);
            myUI.deleteUser.setEnabled(false);// 新建用户，则屏蔽删除操作，只有当保存新建用户后才使能删除操作
            myUI.savedImages.setEnabled(false);
            myUI.enableOrDisableUserInput(true);
        }

        // 删除用户
        if(e.getSource() == myUI.deleteUser)
        {
            int n = JOptionPane.showConfirmDialog(myUI.jFrame, "确认删除?", "删除用户", JOptionPane.YES_NO_OPTION);
            if(n == JOptionPane.YES_OPTION)
            {
                System.out.println("删除用户");
                boolean flag = myUI.loadUserParams.deleteAnUserInExcel(loadInitUserParams.myParams.indexOfUser);
                if(flag)// 删除是否成功的标志位，只有成功才能进行下面的操作；以防删除出现异常时仍执行下面的操作
                {
                    // 删除用户时需删除其文件夹及文件夹下的文件
                    userFolder.deleteUserFolder(myUI.userName.getText().trim() + "_" + myUI.userBirthdate.getText().trim());

                    loadInitUserParams.myParams.indexOfUser = -1;// 删除后，重新置为-1，表示当前未显示用户
                    // 删除完后，清空控件显示
                    myUI.displayUserInfoIntoJFrame("", "", "男", "", 0, 0, true);
                    myUI.save.setText("保存新建用户的参数,或者更新已有用户的参数");
                    myUI.save.setEnabled(false);
                    myUI.deleteUser.setEnabled(false);// 删除完后，屏蔽删除功能，只有当导入用户或新建完联系人后才使能删除操作
                    myUI.savedImages.setEnabled(false);
                    myUI.enableOrDisableUserInput(false);// 禁止用户输入

                    // 删除完后，若存在设备连接，则发送给Glass告知更新为初始控件值；若不存在设备连接，则只进行删除操作
                    sendDataToGlass();
                }
            }
        }

        // 打开用户保存的图片
        if(e.getSource() == myUI.savedImages)
        {
            try
            {
                File folderPath = new File(new Constants().getRootDirectoryOfData() +
                        loadInitUserParams.myParams.userName + "_" + loadInitUserParams.myParams.userBirthdate);
                java.awt.Desktop.getDesktop().open(folderPath);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }

        // 保存用户参数
        if(e.getSource() == myUI.save)
        {
            String userName = myUI.userName.getText().trim();
            String userBirthdate = myUI.userBirthdate.getText().trim();
            String userSex;
            if(myUI.jRadioButton_male.isSelected())
            {
                userSex = "男";
            }
            else
            {
                userSex = "女";
            }
            String complementaryInfo = myUI.complementaryInfo.getText().trim();
            int Zoom = myUI.zoom_sl.getValue();
            int WhiteBalance = myUI.whitebalance.getSelectedIndex();

            // 只有当名字不为空以及出生日期合法时才能保存新建的用户
            if(userName.equals(""))
            {
                dialog(myUI.jFrame, "名字不能为空!", "提示");
            }
            else if(!isBirthdateLegal(userBirthdate))
            {
                dialog(myUI.jFrame, "出生日期格式不正确!\n正确为例如:19920916", "提示");
            }
            else
            {
                System.out.println("保存用户参数");
                String[] userInfo = {userName, userBirthdate, userSex, complementaryInfo, Integer.toString(Zoom), Integer.toString(WhiteBalance)};
                if(myUI.save.getText().equals("更新用户参数"))// 更新已有用户的参数
                {
                    boolean flag = myUI.loadUserParams.updataAnUserDataInExcel(userInfo, loadInitUserParams.myParams.indexOfUser);
                    if(flag)// 更新是否成功的标志位，只有成功才能进行下面的操作；以防更新出现异常时仍执行下面的操作
                    {
                        // 因为文件夹是按"用户名字_用户生日"来命名的，所以更新用户数据时，如果名字或生日出现改动，则需要重命名文件夹
                        // 需要在SaveAllParams之前更新
                        userFolder.updateUserFolder(loadInitUserParams.myParams.userName + "_" + loadInitUserParams.myParams.userBirthdate,
                                userName + "_" + userBirthdate);

                        myUI.saveAllParams(userName, userBirthdate, userSex, complementaryInfo,
                                Zoom , WhiteBalance);

                        // 发送给Glass告知实时更新
                        sendDataToGlass();
                    }
                }
                else// 保存新建用户的参数，并获取到excel表格的最后一行（即插入的用户所在的行）
                {
                    Object[] obj = myUI.loadUserParams.insertAnUserDataToExcel(userInfo);
                    loadInitUserParams.myParams.indexOfUser = Integer.parseInt(obj[0].toString());
                    boolean flag = Boolean.parseBoolean(obj[1].toString());
                    if(flag)// 插入是否成功的标志位，只有成功才能进行下面的操作；以防插入出现异常时仍执行下面的操作
                    {
                        myUI.saveAllParams(userName, userBirthdate, userSex, complementaryInfo,
                                Zoom , WhiteBalance);

                        myUI.save.setText("更新用户参数");
                        myUI.deleteUser.setEnabled(true);// 已经加载一个用户，使能删除操作
                        myUI.savedImages.setEnabled(true);

                        // 发送给Glass告知实时更新
                        sendDataToGlass();

                        // 新建用户时为其分配一个文件夹来存储其数据及图片
                        userFolder.createUserFolder(userName + "_" + userBirthdate);
                    }
                }
            }
        }

        // jPanel2中的控件的响应
        // 直方图均衡
        if(e.getSource() == myUI.histogram)
        {
            loadInitCameraParams.myParams.params3 = !loadInitCameraParams.myParams.params3;
            sendDataToGlass();
            if(loadInitCameraParams.myParams.params3)
            {
                myUI.histogram.setText("已开启,点击后关闭");
            }
            else
            {
                myUI.histogram.setText("未开启,点击后开启");
            }
        }

        // 检测设备：google glass
        if(e.getSource() == myUI.detectDevice)
        {
            mDevices = mADB.getDevices();
            if((mDevices != null) && (mDevices.length > 0))
            {
                mDevice = mDevices[0];
                myUI.status.setText("请点击\"建立通信\",点击前,请确保已打开Google Glass的配套应用 !");
                myUI.detectDevice.setText("检测成功");
                myUI.deviceSerialNumber.setText("设备序列号："+mDevice.getSerialNumber());
                myUI.detectDevice.setEnabled(false);
                myUI.setUpSocket.setEnabled(true);
                myUI.projectGlassScreenToPC.setEnabled(true);// 已经确保存在设备，所以使能按钮projectGlassScreenToPC
                flag_device = true;// 检测到设备
                System.out.println("已连接到设备");
            }
            else
            {
                System.out.println("未检测到设备");
                mDevices = null;
                dialog(myUI.jFrame, "请确认已连接设备 !", "提示");
            }
        }

        // 建立socket通信
        if(e.getSource() == myUI.setUpSocket)
        {
            try
            {
                mDevice.createForward(PC_LOCAL_PORT, ANDROID_PORT);
                socket = new Socket("localhost",PC_LOCAL_PORT);

                myUI.socketStatus.setText("OK");
                myUI.status.setText("通信已建立 !");
                myUI.setUpSocket.setEnabled(false);
                myUI.update.setEnabled(true);// 使能更新按钮

                // Glass相机参数变化，则PC的界面相应控件显示值也要自动修改，才能保持PC和Glass参数一致
                // 所以这里开启线程进行读相机参数操作，然后修改界面控件显示值
                System.out.println("status of socketThread:" + socketThread);
                socketThread = new Thread(new ReadThread(socket));
                socketThread.start();
                System.out.println("status of socketThread:" + socketThread);

                // 定时3分钟发送数据到Glass，保持激活socket，防止TimeOutException
                timer = new Timer(3*60*1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(flag_socket) {
                            sendDataToGlass();
                            System.out.println("定时3分钟发送数据到Glass，保持激活socket，防止TimeOutException");
                        }
                    }
                });
                timer.start();System.out.println("timer.start()");
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                System.out.println("通信过程：您可能在连接设备后拔掉USB线，因此不能建立通信 !");
                handleSocketException();
                dialog(myUI.jFrame, "请确认已连接USB数据线 ! 请连接USB数据线后重新检测设备进行连接 !", "警告");
            }
        }

        // 更新参数
        if(e.getSource() == myUI.update)
        {
            sendDataToGlass();
        }

        // 投影Glass画面到PC
        if(e.getSource() == myUI.projectGlassScreenToPC)
        {
            if(!alreadyProjectToPC)// 尚未存在投射，则点击后，开启投射
            {
                // 投影
                System.out.println("投影Glass画面到PC");
                myUI.projectGlassScreenToPC.setText("关闭Glass画面");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        glassFrame = new MainFrame(mArgs, mDevice);
                        int[] relative = myUI.getRelativePositionAndSizeToScreen(1 - 640.0/1280.0,
                                0.0/1024.0,
                                640.0/1280.0,
                                360.0/1024.0);
                        glassFrame.setLocation(relative[2], relative[3]);
                        glassFrame.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent e) {
                                // 断开投影
                                System.out.println("断开投影Glass画面到PC");
                                alreadyProjectToPC = false;// 按钮恢复为等待"传送Glass画面"状态
                                myUI.projectGlassScreenToPC.setText("传送Glass画面");
                                glassFrame.stopMonitor();// 一定要加，才能退出线程
                                glassFrame.dispose();
                                glassFrame = null;
                            }
                        });
                        glassFrame.setVisible(true);
                        glassFrame.selectDevice();
                    }
                });
                alreadyProjectToPC = true;
            }
            else
            {
                // 断开投影
                System.out.println("断开投影Glass画面到PC");
                alreadyProjectToPC = false;// 按钮恢复为等待"传送Glass画面"状态
                myUI.projectGlassScreenToPC.setText("传送Glass画面");
                glassFrame.stopMonitor();// 一定要加，才能退出线程
                glassFrame.dispose();// 关闭窗口
                glassFrame = null;
            }
        }
    }

    // 线程进行发送接收数据
    class ReadThread implements Runnable
    {
        private Socket server;

        public ReadThread(Socket server)
        {
            this.server = server;
        }

        /* 开启线程进行数据发送与接收
         * 数据发送接收在以下三种情况下出现异常：
         * 1、尚未开启Glass配套应用时就点击“建立通信”按钮进行通信；
         * 2、在socket通信过程中退出Glass配套应用；
         * 3、用户拔出usb。
         */
        @Override
        public void run()
        {
            try
            {
                transToGlass = new ObjectOutputStream(socket.getOutputStream());
                receiveFromGlass = new ObjectInputStream(server.getInputStream());
                flag_socket = true;
                flag_initConn = true;// 与Glass建立起通信
                while(flag_socket)// 判断是否断开socket连接
                {
                    try
                    {
                        if(flag_initConn) {// 刚建立起通信，则发送PC初始参数给Glass进行同步
                            sendDataToGlass();
                            flag_initConn = false;
                        }

                        Object obj = receiveFromGlass.readObject();
                        // 如果obj为空说明Glass端应用已退出，不为空则正常接收
                        if(obj != null) {
                            System.out.println("通信过程：正常接收数据");
                            loadInitCameraParams.myParams = (CameraParams)obj;

                            // 修改界面控件显示值
                            myUI.zoom_sl.setValue(loadInitCameraParams.myParams.params1);
                            myUI.zoom_tf.setText("" + loadInitCameraParams.myParams.params1);
                            myUI.whitebalance.setSelectedIndex(loadInitCameraParams.myParams.params2);
                            if(loadInitCameraParams.myParams.params3) {
                                myUI.histogram.setText("已开启,点击后关闭");
                            } else {
                                myUI.histogram.setText("未开启,点击后开启");
                            }
                        } else { //异常情况：在socket通信过程中退出Glass配套应用
                            System.out.println("通信过程：接收到的参数为空，说明Glass应用已关闭");
                            receiveFromGlass.close();// 关闭数据输入输出流
                            transToGlass.close();
                            server = null;// 重新置socket为空
                            socketThread = null;
                            timer.stop();timer = null;System.out.println("timer.stop()");
                            handleSocketException();
                            dialog(myUI.jFrame, "通信中断，请重新检测设备进行连接 !", "警告");
                        }
                    }
                    catch(Exception e) // 异常情况：用户拔出usb
                    {
                        e.printStackTrace();
                        System.out.println("通信过程：socket中断");
                        receiveFromGlass.close();// 关闭数据输入输出流
                        transToGlass.close();
                        server = null;// 重新置socket为空
                        socketThread = null;
                        timer.stop();timer = null;System.out.println("timer.stop()");
                        handleSocketException();
                        dialog(myUI.jFrame, "通信中断，请重新检测设备进行连接 !", "警告");
                    }
                }
            }
            catch(Exception e) // 异常情况；尚未开启Glass配套应用时就点击“建立通信”按钮进行通信
            {
                e.printStackTrace();
                System.out.println("通信过程：未打开Glass配套应用!");
                try {
                    transToGlass.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                server = null;// 重新置socket为空
                socketThread = null;
                timer.stop();timer = null;System.out.println("timer.stop()");
                handleSocketException();
                dialog(myUI.jFrame, "请确认已打开Glass配套应用 ! 请重新检测设备进行连接 !", "警告");
            }
        }
    }

    // 发送数据给Glass，两种情况：
    // 刚开始建立socket后，马上发送给Glass告诉Glass根据PC的参数进行同步；
    // 按下“发送参数到Glass”按钮后发送
    public void sendDataToGlass()
    {
        // 当存在设备连接且socket建立后，才发送更新；否则不更新
        if(flag_device && flag_socket)
        {
            loadInitCameraParams.myParams.params1 = myUI.zoom_sl.getValue();
            loadInitCameraParams.myParams.params2 = myUI.whitebalance.getSelectedIndex();
            loadInitCameraParams.myParams.params4 = (int)(myUI.scale * 100);
            loadInitCameraParams.myParams.params5 = myUI.locX * 2;
            loadInitCameraParams.myParams.params6 = myUI.locY * 2;
            try {
                transToGlass.writeObject(loadInitCameraParams.myParams);
                System.out.println("Send params1"+ loadInitCameraParams.myParams.params1);
                System.out.println("Send params2"+ loadInitCameraParams.myParams.params2);
                transToGlass.reset();// writeObject后，一定要reset()

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //  判断用户出生日期是否合法
    public boolean isBirthdateLegal(String str)
    {
        Pattern pattern = Pattern.compile("^((\\d{2}(([02468][048])|([13579][26]))((((0" +"[13578])|(1[02]))((0[1-9])|([1-2][0-9])|(3[01])))" +"|(((0[469])|(11))((0[1-9])|([1-2][0-9])|(30)))|" +"(02((0[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][12" +"35679])|([13579][01345789]))((((0[13578])|(1[02]))" +"((0[1-9])|([1-2][0-9])|(3[01])))|(((0[469])|(11))" +"((0[1-9])|([1-2][0-9])|(30)))|(02((0[" +"1-9])|(1[0-9])|(2[0-8]))))))");
        return pattern.matcher(str).matches();
    }

    // 弹出对话框提示
    public void dialog(JFrame jFrame, String content, String title)
    {
        JOptionPane jOptionPane = new JOptionPane();
        jOptionPane.showMessageDialog(jFrame, content, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // socket异常情况下恢复PC界面控件的初始显示
    public void handleSocketException()
    {
        flag_device = false;// 设备断开连接
        flag_socket = false;// 断开socket
        myUI.detectDevice.setEnabled(true);// 重新从检测设备开始
        myUI.detectDevice.setText("检测设备");
        myUI.deviceSerialNumber.setText("");
        myUI.status.setText("请点击\"检测设备\",点击前,请确保已连接Google Glass !");
        myUI.setUpSocket.setEnabled(false);
        myUI.projectGlassScreenToPC.setEnabled(false);
        myUI.update.setEnabled(false);
        if(glassFrame != null)// 如果关掉glass应用或拔掉usb时有打开投影功能，则关闭
        {
            // 断开投影
            System.out.println("断开投影Glass画面到PC");
            alreadyProjectToPC = false;// 按钮恢复为等待"传送Glass画面"状态
            myUI.projectGlassScreenToPC.setText("传送Glass画面");
            glassFrame.stopMonitor();
            glassFrame.dispose();// 关闭窗口
            glassFrame = null;
        }
    }
}