package com.windy.sendexceltoweichar.io;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.widget.Toast;


import com.windy.sendexceltoweichar.ConstantValues;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static com.windy.sendexceltoweichar.ConstantValues.FILENAME_SUFFIX;

public class GenerateReport {

    public static void generateReport(Context context) throws IOException {

        //Asset 里的模版考到外存储
        String targetFolder = Environment.getExternalStorageDirectory() + File.separator + "windy";
        String targetFile = generateFileName();
        WFileUtils.getInstance(context).copyAssetsToSD("template.xlsx",targetFolder,targetFile);


        InputStream inputStream = new FileInputStream(targetFolder+File.separator+targetFile);
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

        XSSFSheet sheet = workbook.getSheetAt(0);
        //一共多少行
        int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
        //设置体温,算上标题和人数最少9行
        if (physicalNumberOfRows>=9){
            //从第三行开始
            for (int i = 2; i < physicalNumberOfRows; i++) {
                XSSFRow row = sheet.getRow(i);
                XSSFCell nameCell = row.getCell(1);
                XSSFCell tempCell = row.getCell(3);
                if (tempCell!=null && nameCell.getStringCellValue().length()>1){
                    tempCell.setCellValue(generateTemperature());
                }

            }
        }
        //设置日期
        sheet.getRow(0).getCell(5).setCellValue(generateDate());
        //设置"上班" or "休息"
        //修改结束
        inputStream.close();


        //保存
        OutputStream outputStream = new FileOutputStream(targetFolder+File.separator+targetFile);
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
        workbook.close();

        Toast.makeText(context,"生成成功，请分享WX",Toast.LENGTH_SHORT).show();
    }


    public static double generateTemperature(){

        //正数部分的数值
        Random random = new Random();
        int t1 = random.nextInt(ConstantValues.MAX_TEMP- ConstantValues.MIN_TEMP + 1) + ConstantValues.MIN_TEMP;

        //小数部分的数值
        Random random2 = new Random();
        int t2 = random2.nextInt(9-0+1) + 0;

        double temp = t1 + Double.parseDouble("0."+t2);
        return temp;
    }

    public static String generateFileName(){

        Date today = new Date();
        int month = today.getMonth() + 1;
        String monthStr = String.valueOf(month);
        if (month<10){
            monthStr="0"+monthStr;
        }

        int date = today.getDate();
        String dateStr = String.valueOf(date);
        if (date<10){
            dateStr="0"+monthStr;
        }

        return ConstantValues.FILENAME_PREFIX + monthStr+dateStr +FILENAME_SUFFIX;

    }

    public static String generateDate(){

        Date today = new Date();
        int month = today.getMonth() + 1;
        String monthStr = String.valueOf(month);
        if (month<10){
            monthStr="0"+monthStr;
        }

        int date = today.getDate();
        String dateStr = String.valueOf(date);
        if (date<10){
            dateStr="0"+monthStr;
        }

        Calendar cal = Calendar.getInstance();

        return  String.valueOf(cal.get(Calendar.YEAR)+"."+monthStr+"."+dateStr);

    }


}
