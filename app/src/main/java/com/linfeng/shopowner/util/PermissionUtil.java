package com.linfeng.shopowner.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

public class PermissionUtil {

    public static boolean checkReadExternalStoragePermission(Context context) {
        // 检查权限是否已经被授予
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true; // 已经有权限
        } else {
            return false; // 没有权限
        }
    }
}
