package org.di.permission;

/**
 * 不带参数权限回调
 */
public interface SimpleCallback {
    void onGranted();

    void onDenied();
}
