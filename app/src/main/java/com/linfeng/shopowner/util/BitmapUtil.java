package com.linfeng.shopowner.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;

public class BitmapUtil {
    // 旋转 Bitmap 根据 Exif 信息
    public static Bitmap rotateBitmap(Bitmap bitmap, String imagePath) {
        int rotation = getImageRotation(imagePath);

        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            return bitmap;
        }
    }

    // 获取图片的旋转角度
    private static int getImageRotation(String imagePath) {
        int rotation = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
                // Add more cases if needed
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rotation;
    }
}
