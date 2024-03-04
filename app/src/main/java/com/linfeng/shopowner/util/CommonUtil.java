package com.linfeng.shopowner.util;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommonUtil {
    public static final String PARAM_GOOD_ID = "goodID";
    public static final int RESPONSE_SUCCESS = 0;

    public static boolean isTextEmpty(String s) {
        return s == null || s.equals("");
    }

    public static boolean isEditTextEmpty(Editable s) {
        return s == null || s.toString().equals("");
    }

    public static int safeParseInt(String s, int defaultValue) {
        if (s == null || s.equals("")) {
            return defaultValue;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) < '0' || s.charAt(i) > '9') {
                return defaultValue;
            }
        }
        return Integer.parseInt(s);
    }

    public static String md5(String input, String errorResult) {
        try {
            // 获取 MD5 摘要算法的 MessageDigest 实例
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 将字符串转换为字节数组
            byte[] messageDigest = md.digest(input.getBytes());

            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return errorResult;
        }
    }

    public static String getRealPathFromUri(Uri uri, ContentResolver contentResolver) {
        String realPath = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = contentResolver.query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                realPath = cursor.getString(column_index);
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realPath;
    }


}
