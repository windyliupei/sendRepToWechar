package com.windy.sendexceltoweichar.io;

import android.content.res.AssetManager;


import com.windy.sendexceltoweichar.ConstantValues;

/*import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;*/

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class GenerateReport {

    public static void generateReport(AssetManager assets) throws IOException {

        /*InputStream inputStream = assets.open("template.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

        XSSFSheet sheet = workbook.getSheetAt(0);
        //一共多少行
        int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
        //设置体温
        if (physicalNumberOfRows>=9){
            //从第三行开始
            for (int i = 2; i < physicalNumberOfRows; i++) {
                XSSFRow row = sheet.getRow(i);
                row.getCell(4).setCellValue(generateTemperature());
            }
        }

        OutputStream outputStream = null;
        workbook.write(outputStream);*/




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

}
