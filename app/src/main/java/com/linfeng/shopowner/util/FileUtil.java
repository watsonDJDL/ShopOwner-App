package com.linfeng.shopowner.util;
import android.graphics.Bitmap;
import android.os.Environment;

import com.linfeng.shopowner.ShopOwner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {

    public static boolean copyFile(File sourceFile, File destinationFile) {
        try (InputStream inputStream = new FileInputStream(sourceFile);
             OutputStream outputStream = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return true; // 文件复制成功
        } catch (IOException e) {
            e.printStackTrace();
            return false; // 文件复制失败
        }
    }

    // 重载方法，接受文件路径作为参数
    public static boolean copyFile(String sourceFilePath, String destinationFilePath) {
        File sourceFile = new File(sourceFilePath);
        File destinationFile = new File(destinationFilePath);

        return copyFile(sourceFile, destinationFile);
    }

    /*
     * 获取手机外部存储路径
     * */
    public static String getOutputFile() {
        File mediaFile = null;
        boolean OutputExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (OutputExist) {
            mediaFile = ShopOwner.getContext().getExternalCacheDir();
            return mediaFile.toString();
        }
        return null;
    }


    public static Bitmap cropBitmap(Bitmap originalBitmap, int left, int top, int right, int bottom) {
        return Bitmap.createBitmap(originalBitmap, left, top, right - left, bottom - top);
    }

    public static void saveBitmapToFile(Bitmap bitmap, String filePath) {
        File file = new File(filePath);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
