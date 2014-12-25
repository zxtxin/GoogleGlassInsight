package cn.edu.fudan.ee.glasscameracontrol.GUIWidget;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by hbj on 2014/12/1.
 * 加载用户时，新建一个窗体，从excel导入数据显示在新建的窗体的JTable上
 */
public class SelectUser {
    public JFrame jFrame;
    private JPanel jPanel;
    public JTable jTable;
    private JScrollPane jScrollPane;

    public SelectUser(final UI myUI, Object[] tableTitle, Object[][] tableData) {
        DefaultTableModel model = new DefaultTableModel(tableData, tableTitle) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;// 设置JTable单元格不可编辑
            }

            public Class getColumnClass(int column) {
                Class returnValue;
                if ((column >= 0) && (column < getColumnCount())) {
                    returnValue = getValueAt(0, column).getClass();
                } else {
                    returnValue = Object.class;
                }
                return returnValue;
            }
        };
        jTable.setModel(model);
        // 排序
        RowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(model);
        jTable.setRowSorter(rowSorter);
        // 单选
        jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 设置对齐
//        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
//        defaultTableCellRenderer.setHorizontalAlignment(JLabel.CENTER);
//        jTable.setDefaultRenderer(Object.class, defaultTableCellRenderer);
        // 设置对齐
        String[] align = {"left", "center", "center", "center", "center", "center"};
        setJTableAlignment(jTable, tableTitle, align);
        // 尺寸及位置及其他属性
        int[] relative = myUI.getRelativePositionAndSizeToScreen(0, 0, 600.0/1280, 0);
        jScrollPane.setPreferredSize(new Dimension(relative[4], jTable.getRowHeight()*10));// 最多只显示10个用户，超过则滚动显示
        jFrame = new JFrame("双击选择导入用户");
        jFrame.add(jScrollPane);
        jFrame.setSize(relative[4], jTable.getRowHeight() * 10);
        jFrame.setLocationRelativeTo(myUI.jFrame);
        jFrame.setResizable(false);
        jFrame.pack();
        jFrame.setVisible(true);
        // 监听关闭窗口
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                myUI.jFrame.setEnabled(true);// 使能主窗体
            }
        });
    }

    // 用来设置表格每列居中还是左对齐
    public void setJTableAlignment(JTable jTable, Object[] tableTitle, String[] align) {
        for(int i=0; i<jTable.getColumnCount(); i++) {
            jTable.getColumn(tableTitle[i]).setCellRenderer(new MyTableCellRenderer(align[i]));
        }
    }

    public class MyTableCellRenderer extends DefaultTableCellRenderer {
        public MyTableCellRenderer(String align) {
            if(align.equals("center")) {
                setHorizontalAlignment(JLabel.CENTER);
            }
            else if(align.equals("left")) {
                setHorizontalAlignment(JLabel.LEFT);
            }
        }
    }
}
