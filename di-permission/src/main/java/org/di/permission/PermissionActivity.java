package org.di.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PermissionActivity extends Activity {

    private static final String TYPE = "TYPE";
    public static final int TYPE_RUNTIME = 0x01;
    public static final int TYPE_WRITE_SETTINGS = 0x02;
    public static final int TYPE_DRAW_OVERLAYS = 0x03;

    public void start(Context context, int type) {
        Intent starter = new Intent(context, PermissionActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.putExtra(TYPE, type);
        context.startActivity(starter);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //不拦截点击事件,接受out_side事件，其余down/move/up不接受
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        int byteExtra = getIntent().getIntExtra(TYPE, TYPE_RUNTIME);
        Log.d("PermissionActivity", "byteExtra: " + byteExtra);
        if (byteExtra == TYPE_RUNTIME) {
            if (DiPermission.instance == null) {
                Log.e("PermissionActivity", "request permissions failed");
                finish();
                return;
            }
            //申请权限之前 如果需要需要弹窗说明,则这里暂停
            if (DiPermission.instance.rationale(this)) {
                return;
            }
            if (DiPermission.instance.getPermissionsRequest() != null) {
                int size = DiPermission.instance.getPermissionsRequest().length;
                if (size <= 0) {
                    finish();
                    return;
                }
                requestPermissions(DiPermission.instance.getPermissionsRequest(), 1);
            }
        } else if (byteExtra == TYPE_WRITE_SETTINGS) {
            PermissionUtil.startWriteSettingsActivity(this, TYPE_WRITE_SETTINGS);
        } else if (byteExtra == TYPE_DRAW_OVERLAYS) {
            PermissionUtil.startOverlayPermissionActivity(this, TYPE_DRAW_OVERLAYS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (DiPermission.instance != null) {
            DiPermission.instance.onRequestPermissionsResult(this);
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TYPE_WRITE_SETTINGS) {
            if (PermissionUtil.sSimpleCallback4WriteSettings == null) return;
            if (PermissionUtil.isGrantedWriteSettings()) {
                PermissionUtil.sSimpleCallback4WriteSettings.onGranted();
            } else {
                PermissionUtil.sSimpleCallback4WriteSettings.onDenied();
            }
            PermissionUtil.sSimpleCallback4WriteSettings = null;
        } else if (requestCode == TYPE_DRAW_OVERLAYS) {
            if (PermissionUtil.sSimpleCallback4DrawOverlays == null) {
                return;
            }
            if (PermissionUtil.isGrantedDrawOverlays()) {
                PermissionUtil.sSimpleCallback4DrawOverlays.onGranted();
            } else {
                PermissionUtil.sSimpleCallback4DrawOverlays.onDenied();
            }
            PermissionUtil.sSimpleCallback4DrawOverlays = null;
        }
        finish();
    }

}
