package com.yk.bluetools;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.text.TextUtils;
import android.util.Log;



import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    /**
     * 拷贝assets文件下文件到指定路径
     *
     * @param context
     * @param targetDir 目标文件夹
     */
    public static void copyAssets(Context context, String targetDir) {

        if (TextUtils.isEmpty(targetDir)) {
            return ;
        }
        try {
            //判断文件夹是否存在
            File targetfile = new File(targetDir);
            if (!targetfile.exists()) {
                targetfile.mkdirs();
            }

            File targetfileChild = new File(targetDir+"/save_data");
            if (!targetfileChild.exists()) {
                targetfileChild.mkdirs();
            }
            File targetfileChild1 = new File(targetDir+"/char_error");
            if (!targetfileChild1.exists()) {
                targetfileChild1.mkdirs();
            }
            File targetfileChild2 = new File(targetDir+"/correction_error");
            if (!targetfileChild2.exists()) {
                targetfileChild2.mkdirs();
            }
            File targetfileChild3 = new File(targetDir+"/logs");
            if (!targetfileChild3.exists()) {
                targetfileChild3.mkdirs();
            }
            File targetfileChild4 = new File(targetDir+"/src_img");
            if (!targetfileChild4.exists()) {
                targetfileChild4.mkdirs();
            }
            File targetfileChild5 = new File(targetDir+"/test");
            if (!targetfileChild5.exists()) {
                targetfileChild5.mkdirs();
            }
            //遍历assets下的model文件里的文件
            String[] fileModels = context.getResources().getAssets().list("model");

            for (String filemodel : fileModels) {
                final File fileName = new File(targetDir + "/" + filemodel);
                if (fileName.exists()) {
                    FileInputStream targetInput = new FileInputStream(fileName);
                    InputStream assetInput = context.getAssets().open("model/"+filemodel);
                    Log.i("copyAssets", "........................................................................................................." + (targetInput.available() == assetInput.available()));
                    if (targetInput.available() == assetInput.available()) {
                        continue;
                    } else {
                        copyAssetsFile(context,"model/"+filemodel,fileName);
                    }
                } else {
                    copyAssetsFile(context,"model/"+filemodel,fileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void copyAssetsTwo(Context context, String oldPath, String targetDir) {
        if (TextUtils.isEmpty(targetDir)) {
            return ;
        }
        File targetfileChild = new File(targetDir+"/save_data");
        if (!targetfileChild.exists()) {
            targetfileChild.mkdirs();
        }
        File targetfileChild1 = new File(targetDir+"/char_error");
        if (!targetfileChild1.exists()) {
            targetfileChild1.mkdirs();
        }
        File targetfileChild2 = new File(targetDir+"/correction_error");
        if (!targetfileChild2.exists()) {
            targetfileChild2.mkdirs();
        }
        File targetfileChild3 = new File(targetDir+"/logs");
        if (!targetfileChild3.exists()) {
            targetfileChild3.mkdirs();
        }
        File targetfileChild4 = new File(targetDir+"/src_img");
        if (!targetfileChild4.exists()) {
            targetfileChild4.mkdirs();
        }
        File targetfileChild5 = new File(targetDir+"/test");
        if (!targetfileChild5.exists()) {
            targetfileChild5.mkdirs();
        }
        copyFilesFassets(context,oldPath, targetDir);
    }


    public static void copyFilesFassets(Context context, String oldPath, String newPath) {

        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File dir = new File(newPath);
                if (!dir.exists())
                    dir.mkdir();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFassets(context,oldPath + "/" + fileName,newPath+"/"+fileName);
                }
            } else {//如果是文件
                copyAssetsFile(context,oldPath,new File(newPath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyAssetsFile(Context context, String fileName, File path) {

        try {
            InputStream myInput;
            OutputStream myOutput = new FileOutputStream(path);
            myInput = context.getAssets().open(fileName);
            byte[] buffer = new byte[2048];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }
            myOutput.flush();
            myInput.close();
            myOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static Bitmap getYuv2Jpeg(byte[] yuv, int mWidth, int mHeight) {
        YuvImage yuvImage = new YuvImage(yuv, ImageFormat.YUY2, mWidth, mHeight, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(yuv.length);
        boolean result = yuvImage.compressToJpeg(new Rect(0, 0, mWidth, mHeight), 100, bos);
        if (result) {
            byte[] buffer = bos.toByteArray();
            Bitmap RgbBitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
            Matrix matrix = new Matrix();
            matrix.setRotate(0);
            Bitmap bmp = Bitmap.createBitmap(RgbBitmap, 0, 0, RgbBitmap.getWidth(), RgbBitmap.getHeight(), matrix, true);
            return bmp;
        }
        return null;
    }

    // 转化十六进制编码为字符串
    public static String toStringHex2(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(
                        i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "utf-8");// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }


    /**
     * 读取assets本地json
     * @param fileName
     * @param context
     * @return
     */
    public static String getJson(String fileName, Context context) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 获取指定目录内所有文件路径
     *
     * @param dirPath 需要查询的文件目录
     * @param _type   查询类型，比如mp3什么的
     */
    public static List<String> getAllFiles(String dirPath, String _type) {
        File f = new File(dirPath);
        if (!f.exists()) {//判断路径是否存在
            return null;
        }

        File[] files = f.listFiles();
        if (files == null) {//判断权限
            return null;
        }

        List<String> list = new ArrayList<>();
        for (File _file : files) {//遍历目录
            if (_file.isFile() && _file.getName().endsWith(_type)) {
                String _name = _file.getName();
                String filePath = _file.getAbsolutePath();//获取文件路径
                String fileName = _file.getName().substring(0, _name.length() - 4);//获取文件名
                list.add(filePath);
            } else if (_file.isDirectory()) {//查询子目录
                getAllFiles(_file.getAbsolutePath(), _type);
            }
        }
        return list;
    }


    public static Bitmap getBitmapFromPath(String path) {
        if (!new File(path).exists()) {
            System.err.println("getBitmapFromPath: file not exists");
            return null;
        }
        byte[] buf = new byte[1024 * 1024];// 1M
        Bitmap bitmap = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            int len = fis.read(buf, 0, buf.length);
            bitmap = BitmapFactory.decodeByteArray(buf, 0, len);
            if (bitmap == null) {
                System.out.println("len= " + len);
                System.err.println("path: " + path + "  could not be decode!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }





    public static String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * 判断服务是否开启
     *
     * @return
     */
    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (TextUtils.isEmpty(ServiceName)) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(Integer.MAX_VALUE);
        if (runningService.size() <= 0) {
            return false;
        }
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString().equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }
}
