package org.di.permission

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    private var isOpenSetting: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DiPermission.build(PermissionConstants.STORAGE, PermissionConstants.PHONE)
            .callback(object : FullCallback {
                override fun onGranted(permissionsGranted: MutableList<String>?) {
                    //申请通过的权限
                }

                override fun onDenied(
                    permissionsDeniedForever: MutableList<String>?,
                    permissionsDenied: MutableList<String>?
                ) {
                    //申请被拒绝的权限
                }

            }).rationale {
                //多次调用弹窗提醒权限
            }.request()
    }
}