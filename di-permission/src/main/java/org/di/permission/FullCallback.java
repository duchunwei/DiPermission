package org.di.permission;

import java.util.List;

/**
 * 带参数的权限回调
 */
public interface FullCallback {
    void onGranted(List<String> permissionsGranted);

    void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied);
}
