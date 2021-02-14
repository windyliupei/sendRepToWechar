package com.windy.sendexceltoweichar.io;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.widget.Toast;


import androidx.core.content.FileProvider;

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
import java.util.List;
import java.util.Random;

import static com.windy.sendexceltoweichar.ConstantValues.FILENAME_SUFFIX;
import static com.windy.sendexceltoweichar.ConstantValues.PACKAGE_WECHAT;
import static com.windy.sendexceltoweichar.ConstantValues.VERSION_CODE_FOR_WEI_XIN_VER7;

public class GenerateReport {

    public static void generateReport(Context context, boolean work) throws IOException {

        //Asset 里的模版考到外存储
        String targetFolder = Environment.getExternalStorageDirectory() + File.separator + "windy";
        String targetFile = generateFileName();
        WFileUtils.getInstance(context).copyAssetsToSD("template.xlsx",targetFolder,targetFile);

        File oldFile = new File(targetFolder+File.separator+targetFile);
        oldFile.deleteOnExit();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
                XSSFCell workCell = row.getCell(5);
                if (tempCell!=null && nameCell.getStringCellValue().length()>1){
                    tempCell.setCellValue(generateTemperature());

                    //设置"上班" or "休息"
                    String workOrNot = ConstantValues.SHANGBAN;
                    if (isWorkOrNot(work)){
                        workOrNot = ConstantValues.SHANGBAN;
                    }else{
                        workOrNot = ConstantValues.XIUXI;
                    }

                    workCell.setCellValue(workOrNot);
                }

            }
        }
        //设置日期
        sheet.getRow(0).getCell(5).setCellValue(generateDate());

        //修改结束
        inputStream.close();


        //保存
        OutputStream outputStream = new FileOutputStream(targetFolder+File.separator+targetFile);
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
        workbook.close();

        Toast.makeText(context,"生成成功，请分享WX",Toast.LENGTH_SHORT).show();

        shareWechatFriend(context,new File(targetFolder+File.separator+targetFile));
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

    public static boolean isWorkOrNot(boolean work){

        Calendar today = Calendar.getInstance();
        today.setFirstDayOfWeek(Calendar.MONDAY);

        if(today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ){
            return false;
        }else{
            //如果不是周末再判断是否公共假期：目前手动输入：0 上班，1：休息
            return work;
        }


    }


    /**
     * 直接文件到微信好友
     *
     * @param picFile 文件路径
     */
    public static void shareWechatFriend(Context mContext, File picFile) {
        if (isInstallApp(mContext, PACKAGE_WECHAT)) {
            Intent intent = new Intent();

            intent.setPackage(PACKAGE_WECHAT);
            intent.setAction(Intent.ACTION_SEND);
            String type="*/*";
            for (int i = 0; i < MATCH_ARRAY.length; i++) {
                //判断文件的格式
                if (picFile.getAbsolutePath().toString().contains(MATCH_ARRAY[i][0].toString())) {
                    type = MATCH_ARRAY[i][1];
                    break;
                }
            }
            intent.setType(type);
            Uri uri = null;
            if (picFile != null) {
                //这部分代码主要功能是判断了下文件是否存在，在android版本高过7.0（包括7.0版本）
                //当前APP是不能直接向外部应用提供file开头的的文件路径，需要通过FileProvider转换一下。否则在7.0及以上版本手机将直接crash。
                try {
                    ApplicationInfo applicationInfo = mContext.getApplicationInfo();
                    int targetSDK = applicationInfo.targetSdkVersion;
                    if (targetSDK >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".fileprovider", picFile);
                    } else {
                        uri = Uri.fromFile(picFile);
                    }
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (getVersionCode(mContext, PACKAGE_WECHAT) > VERSION_CODE_FOR_WEI_XIN_VER7) {
                // 微信7.0及以上版本
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }
            // context.startActivity(intent);
            mContext.startActivity(Intent.createChooser(intent, "Share"));
        } else {
            Toast.makeText(mContext, "您需要安装微信客户端", Toast.LENGTH_LONG).show();
        }
    }

    // 判断是否安装指定app
    public static boolean isInstallApp(Context context, String app_package) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pInfo = packageManager.getInstalledPackages(0);
        if (pInfo != null) {
            for (int i = 0; i < pInfo.size(); i++) {
                String pn = pInfo.get(i).packageName;
                if (app_package.equals(pn)) {
                    return true;
                }
            }
        }
        return false;
    }

    //建立一个文件类型与文件后缀名的匹配表
    private static final String[][] MATCH_ARRAY = {
            //{后缀名，    文件类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".exe", "application/octet-stream"},
            {".xlsx","application/vnd.ms-excel"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".prop", "text/plain"},
            {".rar", "application/x-rar-compressed"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/zip"},
            {"", "*/*"}
    };

    /**
     * 获取制定包名应用的版本的versionCode
     *
     * @param context
     * @param
     * @return
     */
    private static int getVersionCode(Context context, String packageName) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            int version = info.versionCode;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
