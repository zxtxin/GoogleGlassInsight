package cn.edu.fudan.ee.glasscameracontrol.GUIWidget;

import cn.edu.fudan.ee.glasscameracontrol.DataBase.LoadInitCameraParams;
import cn.edu.fudan.ee.glasscameracontrol.DataBase.LoadInitUserParams;
import cn.edu.fudan.ee.glasscameracontrol.DataBase.LoadUserParams;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by hbj on 2014/12/1.
 */
public class UI extends JFrame {
    public JFrame jFrame;
    public JPanel jPanel;
    public JPanel jPanel1;
    public JPanel jPanel2;
    public JButton loadAnUserParams;
    public JButton createNewUser;
    public JButton deleteUser;
    public JTextField userName;
    public JTextField userBirthdate;
    public JRadioButton jRadioButton_male;
    public JRadioButton jRadioButton_female;
    public JTextArea complementaryInfo;
    public JLabel status;
    public JButton detectDevice;
    public JLabel deviceSerialNumber;
    public JLabel socketStatus;
    public JButton setUpSocket;
    public JButton projectGlassScreenToPC;
    public JSlider zoom_sl;
    public JTextField zoom_tf;
    public JComboBox whitebalance;
    public JButton histogram;
    public JButton update;
    public JButton save;
    public JButton savedImages;
    public JPanel glassFullScreen;
    public JLabel glassVisibleView;
    public LoadInitCameraParams loadInitCameraParams = LoadInitCameraParams.getInstance();// 加载初始相机参数类
    public LoadInitUserParams loadInitUserParams = LoadInitUserParams.getInstance();// 加载用户初始参数类
    // LoadUserParams类用于从excel读取用户数据导入JTable中；更新用户，或者插入用户到excel
    public LoadUserParams loadUserParams;

    private int mouseX0, mouseY0;// 鼠标在glassVisibleView上按下时的坐标
    public int locX, locY;// 鼠标按下时，glassVisibleView相对glassFullScreen的位置
    public float scale = 1.0f;// glassVisibleView初始缩放系数为1.0f

    public UI() {
        // select Look and Feel, 美化界面
        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 创建窗体
        jFrame = new JFrame("控制端");
        jFrame.add(jPanel);

        // 点击JTextField时光标定位在最后一格
        cursorLocateToLast(userName);
        cursorLocateToLast(userBirthdate);
        cursorLocateToLast(zoom_tf);

        // JTextField屏蔽非法输入，只允许输入数字
        disableIllegalInput(userBirthdate);
        disableIllegalInput(zoom_tf);

        // 绑定zoom_sl和zoom_tf
        zoom_sl.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                zoom_tf.setText("" + zoom_sl.getValue());
            }
        });
        zoom_tf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoom_sl.setValue(new Integer(zoom_tf.getText()));
            }
        });

        // glassVisibleView添加鼠标按下事件
        glassVisibleView.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if(e.getClickCount() == 2)// 双击恢复全屏画面
                {
                    scale = 1.0f;
                    locX = 0;locY = 0;
                    glassVisibleView.setLocation(locX, locY);
                    glassVisibleView.setSize((int)(scale * glassFullScreen.getWidth()), (int)(scale * glassFullScreen.getHeight()));
                }
                else
                {
                    mouseX0 = e.getX();
                    mouseY0 = e.getY();
//                    System.out.println("(mouseX0, mouseY0) = "+"("+mouseX0+", "+mouseY0+")");
                }
            }
        });

        // glassVisibleView添加鼠标拖动事件
        glassVisibleView.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
//                System.out.println("(glassFullScreen.width, glassFullScreen.height) = "+"("+glassFullScreen.getWidth()+", "+glassFullScreen.getHeight()+")");
//                System.out.println("(mouseX1, mouseY1) = "+"("+e.getX()+", "+e.getY()+")");
//                System.out.println("(glassVisibleView.x, glassVisibleView.y) = "+"("+glassVisibleView.getX()+", "+glassVisibleView.getY()+")");

                locX = glassVisibleView.getX() + e.getX() - mouseX0;
                locY = glassVisibleView.getY() + e.getY() - mouseY0;
                int leftMargin = 0;
                int rightMargin = glassFullScreen.getWidth() - glassVisibleView.getWidth();
                int topMargin = 0;
                int bottomMargin = glassFullScreen.getHeight() - glassVisibleView.getHeight();
                if(locX <= leftMargin)
                {
                    locX = leftMargin;
                }
                if(locX >= rightMargin)
                {
                    locX = rightMargin;
                }
                if(locY <= topMargin)
                {
                    locY = topMargin;
                }
                if(locY >= bottomMargin)
                {
                    locY = bottomMargin;
                }
                glassVisibleView.setLocation(locX, locY);
            }
        });

        glassVisibleView.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // e.getWheelRotation()==-1表示向上滑动，1表示向下滑动
                float maxHorizontalScale = (float)(glassFullScreen.getWidth() - glassVisibleView.getX())/glassVisibleView.getWidth();
                float maxVerticalScale = (float)(glassFullScreen.getHeight() - glassVisibleView.getY())/glassVisibleView.getHeight();
                float maxScale = Math.min(maxHorizontalScale, maxVerticalScale);
//                if((glassVisibleView.getX() + glassVisibleView.getWidth() >= glassFullScreen.getWidth()) ||
//                        (glassVisibleView.getY() + glassVisibleView.getHeight() >= glassFullScreen.getHeight()))
//                {
//                    if(e.getWheelRotation() == 1)
//                    {
//                        scale -= e.getWheelRotation() * 0.05;
//                        if(scale <= 0.3f) scale = 0.3f;
//                        if(scale >= maxScale) scale = maxScale;
//                        System.out.println("--------------  "+scale);
//                        glassVisibleView.setSize((int) (scale * glassFullScreen.getWidth()), (int) (scale * glassFullScreen.getHeight()));
//                    }
//                }
//                else
//                {
//                    scale -= e.getWheelRotation() * 0.05;
//                    if(scale <= 0.3f) scale = 0.3f;
//                    if(scale >= maxScale) scale = maxScale;
//                    System.out.println("--------------  " + scale);
//                    glassVisibleView.setSize((int) (scale * glassFullScreen.getWidth()), (int) (scale * glassFullScreen.getHeight()));
//                }
                // 上面注释的一段的简洁写法
                if(!(((glassVisibleView.getX() + glassVisibleView.getWidth() >= glassFullScreen.getWidth()) ||
                        (glassVisibleView.getY() + glassVisibleView.getHeight() >= glassFullScreen.getHeight())) &&
                        (e.getWheelRotation() == -1)))
                {
                    scale -= e.getWheelRotation() * 0.05;
                    if(scale <= 0.3f) scale = 0.3f;
                    if(scale >= maxScale) scale = maxScale;
//                        System.out.println("--------------  "+scale);
                    glassVisibleView.setSize((int) (scale * glassFullScreen.getWidth()), (int) (scale * glassFullScreen.getHeight()));
                }
            }
        });

        // glassVisibleView的setSize和move
        glassVisibleView.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                glassVisibleView.setSize((int) (scale * glassFullScreen.getWidth()), (int) (scale * glassFullScreen.getHeight()));
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                super.componentMoved(e);
                glassVisibleView.setLocation(locX, locY);
            }
        });

        // 用户参数类
        loadUserParams = new LoadUserParams(jFrame);

        // 默认不开启直方图均衡，采用原始RGB效果
        loadInitCameraParams.myParams.params3 = false;
        // 默认画面比例为100%
        loadInitCameraParams.myParams.params4 = 100;

        // 开启软件时，根据上次退出软件时有无用户来初始化控件内容显示
        if(loadInitUserParams.myParams.indexOfUser != -1)// 如果上次退出软件时，界面存在某个用户信息，则加载该用户信息
        {
            displayUserInfoIntoJFrame(loadInitUserParams.myParams.userName,
                    loadInitUserParams.myParams.userBirthdate,
                    loadInitUserParams.myParams.userSex,
                    loadInitUserParams.myParams.complementaryInfo,
                    loadInitCameraParams.myParams.params1,
                    loadInitCameraParams.myParams.params2,
                    false);
            deleteUser.setEnabled(true);
            savedImages.setEnabled(true);
            save.setText("更新用户参数");
            save.setEnabled(true);
        }
        else// 上次退出软件时，界面不存在用户，则不需加载信息，此时把所有关于用户的控件屏蔽，禁止用户输入
        {
            enableOrDisableUserInput(false);
        }

        jFrame.pack();// 让容器适应内部控件的大小
        jFrame.setResizable(false);
        jFrame.setVisible(true);// 显示窗体
    }

    public static void main(String[] args) {
        new UI();
    }

    // 根据屏幕分辨率来设置窗体位置及大小
    public int[] getRelativePositionAndSizeToScreen(double left, double top, double width, double height)
    {
        int[] relative = new int[6];
        Toolkit theKit = jFrame.getToolkit();// 获取屏幕分辨率
        Dimension wndSize = theKit.getScreenSize();
        System.out.println("屏幕分辨率:"+wndSize.getWidth()+" "+wndSize.getHeight());
        relative[0] = (int) (wndSize.getWidth());// 一定要括号, screenWith
        relative[1] = (int) (wndSize.getHeight());// screenHeight
        relative[2] = (int) (left * relative[0]);// left
        relative[3] = (int) (top * relative[1]);// top
        relative[4] = (int) (width * relative[0]);// width
        relative[5] = (int) (height * relative[1]);// height
        return relative;
    }

    // 禁止或使能用户输入
    public void enableOrDisableUserInput(boolean flag)
    {
        userName.setEnabled(flag);
        userBirthdate.setEnabled(flag);
        jRadioButton_male.setEnabled(flag);
        jRadioButton_female.setEnabled(flag);
        complementaryInfo.setEnabled(flag);
    }

    // 将一个用户的所有信息加载到界面相应控件中；或者用于清空界面控件
    public void displayUserInfoIntoJFrame(String user_Name, String user_Birthdate, String user_Sex, String user_complementaryInfo,
                                          int user_Zoom, int user_WhiteBalance, boolean flag)
    {
        userName.setText(user_Name);
        userBirthdate.setText(user_Birthdate);
        if(user_Sex.equals("男"))
        {
            jRadioButton_male.setSelected(true);
        }
        else
        {
            jRadioButton_female.setSelected(true);
        }
        complementaryInfo.setText(user_complementaryInfo);
        zoom_sl.setValue(user_Zoom);
        zoom_tf.setText(String.valueOf(user_Zoom));
        whitebalance.setSelectedIndex(user_WhiteBalance);

        if(flag)
        {
            // 保存所有参数到savedInitialParams文件
            saveAllParams(user_Name, user_Birthdate, user_Sex, user_complementaryInfo, user_Zoom , user_WhiteBalance);
        }
    }

    // 保存所有参数到savedInitialCameraParams、savedInitialUserParams文件
    public void saveAllParams(String user_Name, String user_Birthdate, String user_Sex, String user_complementaryInfo,
                              int user_Zoom , int user_WhiteBalance)
    {
        loadInitUserParams.myParams.userName = user_Name;
        loadInitUserParams.myParams.userBirthdate = user_Birthdate;
        loadInitUserParams.myParams.userSex = user_Sex;
        loadInitUserParams.myParams.complementaryInfo = user_complementaryInfo;
        loadInitCameraParams.myParams.params1 = user_Zoom;
        loadInitCameraParams.myParams.params2 = user_WhiteBalance;

        // 保存相机参数到savedInitialCameraParams文件
        loadInitCameraParams.saveParams(loadInitCameraParams.myParams);
        // 保存用户参数到savedInitialUserParams文件
        loadInitUserParams.saveParams(loadInitUserParams.myParams);
    }

    // JTextField屏蔽非法输入，只允许输入数字
    public void disableIllegalInput(JTextField jTextField)
    {
        jTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                int keyChar = e.getKeyChar();
                if(!(keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9))
                {
                    e.consume(); //屏蔽掉非法输入
                }
            }
        });
    }

    // 点击JTextField时光标定位在最后一格
    public void cursorLocateToLast(final JTextField jTextField)
    {
        jTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                jTextField.setCaretPosition(jTextField.getText().length());
            }
        });
    }
}