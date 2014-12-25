package cn.edu.fudan.ee.glasscameracontrol.DataBase;

import org.apache.poi.hssf.usermodel.*;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by hbj on 2014/11/20.
 * excel操作
 */
public class LoadUserParams {

    String excelFolderPath = null;
    String excelFileName = "userData.xls";
    String excelFilePath = null;
    public JFrame jFrame;

    public LoadUserParams(JFrame jFrame)
    {
        this.jFrame = jFrame;
        // 当保存所有用户参数的excel文件不存在时，创建excel工作簿，并且创建一个工作表，
        // 并且输入标题：姓名、出生日期、性别、补充信息、缩放、白平衡
        createExcelFile();
    }

    // 当保存所有用户参数的excel文件不存在时，创建excel工作簿，并且创建一个工作表，
    // 并且输入标题：姓名、出生日期、性别、补充信息、缩放、白平衡
    public void createExcelFile()
    {
        // excel文件路径 = excel文件所在文件夹路径 + excel文件名
        excelFolderPath = new Constants().getRootDirectoryOfData();
        excelFilePath = excelFolderPath + excelFileName;

        File folder = new File(excelFolderPath);
        if(!folder.exists() && !folder.isDirectory())
        {
            folder.mkdir();
            System.out.println("不存在保存Excel文件的文件夹，现已创建");
        }
        else
        {
            System.out.println("存在保存excel文件的文件夹");
        }

        File file = new File(excelFilePath);
        if(!file.exists())
        {
            try
            {
                // 创建excel工作簿
                HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
                // 新建名为"userData"的工作表
                HSSFSheet hssfSheet = hssfWorkbook.createSheet("userData");
                // 在索引为0的位置创建行（最顶端的行）
                HSSFRow hssfRow = hssfSheet.createRow(0);
                String[] str = {"姓名", "出生日期", "性别", "补充信息", "缩放", "白平衡"};
                for(int i=0; i<str.length; i++)
                {
                    // 在索引i的位置创建单元格（已设置为第一行）
                    HSSFCell hssfCell = hssfRow.createCell(i);
                    // 定义单元格为字符串类型
                    hssfCell.setCellType(HSSFCell.CELL_TYPE_STRING);
                    // 在单元格中输入内容
                    hssfCell.setCellValue(str[i]);
                }
                // 新建文件输出流
                FileOutputStream fileOutputStream = new FileOutputStream(excelFilePath);
                // 将文件存盘
                hssfWorkbook.write(fileOutputStream);
                fileOutputStream.flush();
                // 操作结束，关闭文件
                fileOutputStream.close();
                System.out.println("不存在excel工作簿，现已创建，且已创建工作表，并且输入姓名、出生日期、性别标题、补充信息、缩放、白平衡");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("存在excel文件");
        }
    }

    // 更新一个用户的数据，并返回更新是否成功的标志位
    public boolean updataAnUserDataInExcel(String[] userInfo, int index)
    {
        try
        {
            // 创建对excel工作簿的引用
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(new FileInputStream(excelFilePath));
            // 创建对第一个工作表(userData)的引用
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
            // 在索引为index的位置创建行
            HSSFRow hssfRow = hssfSheet.createRow(index);
            // 创建单元格填入用户信息
            for(int i = 0; i<userInfo.length; i++)
            {
                HSSFCell hssfCell = hssfRow.createCell(i);
                // 定义单元格为字符串类型
                hssfCell.setCellType(HSSFCell.CELL_TYPE_STRING);
                // 在单元格中输入内容
                hssfCell.setCellValue(userInfo[i]);
            }
            // 新建文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(excelFilePath);
            // 将文件存盘
            hssfWorkbook.write(fileOutputStream);
            fileOutputStream.flush();
            // 操作结束，关闭文件
            fileOutputStream.close();
            System.out.println("已更新用户数据");
            return true;// 返回标志位，更新成功
        }
        catch (Exception e)
        {
            e.printStackTrace();
            warning();// 弹出警告框提示异常
            return false;// 返回标志位，更新异常
        }
    }

    // 插入一个用户的数据到excel， 并返回插入的行号(即excel最后一行)，
    // 并返回插入是否成功的标志位（这里创建了Object[2]用来存储行号和标志位）
    public Object[] insertAnUserDataToExcel(String[] userInfo)
    {
        int lastRowIndex;
        try
        {
            // 创建对excel工作簿的引用
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(new FileInputStream(excelFilePath));
            // 创建对第一个工作表(userData)的引用
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
            // 获取excel文件中最后一行的索引，以便在下一行新建用户
            lastRowIndex = hssfSheet.getLastRowNum();
            // 在索引为lastRowIndex + 1的位置创建行
            HSSFRow hssfRow = hssfSheet.createRow(lastRowIndex + 1);
            // 创建单元格填入用户信息
            for(int i = 0; i<userInfo.length; i++)
            {
                HSSFCell hssfCell = hssfRow.createCell(i);
                // 定义单元格为字符串类型
                hssfCell.setCellType(HSSFCell.CELL_TYPE_STRING);
                // 在单元格中输入内容
                hssfCell.setCellValue(userInfo[i]);

            }
            // 新建文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(excelFilePath);
            // 将文件存盘
            hssfWorkbook.write(fileOutputStream);
            fileOutputStream.flush();
            // 操作结束，关闭文件
            fileOutputStream.close();
            System.out.println("已插入用户到excel");
            return new Object[]{lastRowIndex + 1, true};
        }
        catch (Exception e)
        {
            e.printStackTrace();
            warning();// 弹出警告框提示异常
            return new Object[]{-1, false};
        }
    }

    // 从excel中删除一个用户，删除后之后的行会上移，并返回删除是否成功的标志位
    public boolean deleteAnUserInExcel(int indexOfUser)
    {
        try
        {
            // 创建对excel工作簿的引用
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(new FileInputStream(excelFilePath));
            // 创建对第一个工作表(userData)的引用
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
            // 获取最后一行的索引，则删除后需上移的行数 = 最后一行索引 - 所删除行索引 - 1
            int lastRowIndex = hssfSheet.getLastRowNum();
            // 删除用户，后面的行上移
            hssfSheet.shiftRows(indexOfUser + 1, lastRowIndex + 1, -1);
            hssfSheet.removeRow(hssfSheet.getRow(lastRowIndex));
            // 新建文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(excelFilePath);
            // 将文件存盘
            hssfWorkbook.write(fileOutputStream);// write()后生效
            fileOutputStream.flush();
            // 操作结束，关闭文件
            fileOutputStream.close();
            System.out.println("已删除用户");
            return true;// 返回标志位，删除成功
        }
        catch (Exception e)
        {
            e.printStackTrace();
            warning();// 弹出警告框提示异常
            return false;// 返回标志位，删除异常
        }
    }

    // 从excel读取标题
    public Object[] readTitleFromExcel()
    {
        Object[] tableTitle = null;
        try
        {
            // 创建对excel工作簿的引用
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(new FileInputStream(excelFilePath));
            // 创建对第一个工作表(userData)的引用
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
            // 读取第一行标题
            int firstRowIndex = hssfSheet.getFirstRowNum();
            HSSFRow hssfRow = hssfSheet.getRow(firstRowIndex);
            int firstCellIndex = hssfRow.getFirstCellNum();
            int lastCellIndex = hssfRow.getLastCellNum();
            tableTitle = new Object[lastCellIndex - firstCellIndex];
            for(int cIndex = firstCellIndex; cIndex <= lastCellIndex; cIndex++)
            {
                HSSFCell hssfCell = hssfRow.getCell(cIndex);
                if(hssfCell != null)
                {
                    String value = hssfCell.toString();
//                    System.out.println(value);
                    tableTitle[cIndex] = value;// 存到Object[]中，以便导入JTable
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return tableTitle;
    }

    // 从excel读取所有用户的数据
    public Object[][] readAllUsersDataFromExcel()
    {
        Object[][] tableData = null;
        try
        {
            // 创建对excel工作簿的引用
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(new FileInputStream(excelFilePath));
            // 创建对第一个工作表(userData)的引用
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
            // 读取所有用户数据
            int firstRowIndex = hssfSheet.getFirstRowNum();
            int lastRowIndex = hssfSheet.getLastRowNum();
            HSSFRow hssfRow0 = hssfSheet.getRow(firstRowIndex);
            int firstCellIndex = hssfRow0.getFirstCellNum();
            int lastCellIndex = hssfRow0.getLastCellNum();
            tableData = new Object[lastRowIndex - firstRowIndex][lastCellIndex - firstCellIndex];
            for(int rIndex = firstRowIndex + 1; rIndex <= lastRowIndex; rIndex++)
            {
                HSSFRow hssfRow = hssfSheet.getRow(rIndex);
                if(hssfRow != null)
                {
                    for(int cIndex = firstCellIndex; cIndex <= lastCellIndex; cIndex++)
                    {
                        HSSFCell hssfCell = hssfRow.getCell(cIndex);
                        if(hssfCell != null)
                        {
                            String value = hssfCell.toString();
//                            System.out.println(value);
                            tableData[rIndex - 1][cIndex] = value;// 存到Object[][]中，以便导入JTable
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return tableData;
    }

    public void warning()
    {
        JOptionPane.showMessageDialog(jFrame, "保存用户数据的excel文件已在另一应用中打开，请关闭后重试!", "异常", JOptionPane.WARNING_MESSAGE);
    }
}