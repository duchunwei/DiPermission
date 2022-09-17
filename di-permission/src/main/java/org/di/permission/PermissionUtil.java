package org.di.permission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionUtil {

    public static final Application sApplication = getApplication();
    public static final List<String> MANIFEST_PERMISSIONS = getPermissions(sApplication.getPackageName());

    public static SimpleCallback sSimpleCallback4WriteSettings;
    public static SimpleCallback sSimpleCallback4DrawOverlays;

    /**
     * 获取安装包在manifest中声明的权限集合--此时你可以在APP中 开发一个隐私权限页，告诉用户那些已授权，是什么用的
     *
     * @param packageName 包名
     * @return 返回权限列表
     */
    public static List<String> getPermissions(String packageName) {
        assert sApplication != null;
        PackageManager pm = sApplication.getPackageManager();
        try {
            String[] permissions = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions;
            if (permissions == null) {
                return new ArrayList<>();
            }
            return Arrays.asList(permissions);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 是否已经授权过了
     *
     * @param permissions 权限列表
     * @return 是否
     */
    public static boolean isGranted(String... permissions) {
        for (String permission : permissions) {
            if (!isGranted(permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isGranted(String permission) {
        assert sApplication != null;
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                sApplication,
                permission
        );
    }

    /**
     * 检查APP是否获取修改系统设置的权限
     */
    public static boolean isGrantedWriteSettings() {
        return Settings.System.canWrite(sApplication);
    }

    public static void requestWriteSettings(SimpleCallback callback) {
        if (isGrantedWriteSettings()) {
            callback.onGranted();
            return;
        }
        sSimpleCallback4WriteSettings = callback;
        assert sApplication != null;
        new PermissionActivity().start(
                sApplication.getApplicationContext(),
                PermissionActivity.TYPE_WRITE_SETTINGS
        );
    }

    static void startWriteSettingsActivity(Activity activity, int requestCode) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        assert sApplication != null;
        intent.setData(Uri.parse("package:" + sApplication.getPackageName()));
        if (!isIntentAvailable(intent)) {
            launchAppDetailsSettings();
            return;
        }
        activity.startActivityForResult(intent, requestCode);
    }


    /**
     * APP 是否可以 添加悬浮view
     * <p>
     * <p>
     * api 官网解释如下
     * Checks if the specified context can draw on top of other apps. As of API
     * * level 23, an app cannot draw on top of other apps unless it declares the
     * * [android.Manifest.permission.SYSTEM_ALERT_WINDOW] permission in its
     * * manifest.
     */
    public static boolean isGrantedDrawOverlays() {
        return Settings.canDrawOverlays(sApplication);
    }


    public static void requestDrawOverlays(SimpleCallback callback) {
        if (isGrantedDrawOverlays()) {
            callback.onGranted();
            return;
        }
        sSimpleCallback4DrawOverlays = callback;
        assert sApplication != null;
        new PermissionActivity().start(
                sApplication.getApplicationContext(),
                PermissionActivity.TYPE_DRAW_OVERLAYS
        );
    }

    static void startOverlayPermissionActivity(Activity activity, int requestCode) {
        Intent intent = new
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        assert sApplication != null;
        intent.setData(Uri.parse("package:" + sApplication.getPackageName()));
        if (!isIntentAvailable(intent)) {
            launchAppDetailsSettings();
            return;
        }
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Launch the application's details settings.
     */
    public static void launchAppDetailsSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        assert sApplication != null;
        intent.setData(Uri.parse("package:" + sApplication.getPackageName()));
        if (!isIntentAvailable(intent)) {
            return;
        }
        sApplication.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @SuppressLint("QueryPermissionsNeeded")
    private static boolean isIntentAvailable(Intent intent) {
        assert sApplication != null;
        return sApplication
                .getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .size() > 0;
    }

    @SuppressLint("PrivateApi")
    private static Application getApplication() {
        Application application = null;
        try {
            application = (Application) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return application;
    }
}
