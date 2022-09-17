# 动态权限申请库
# 1.注册清单
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.CALL_PHONE" />
# 2.调用方式
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