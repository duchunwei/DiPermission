package org.di.permission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale;

public class DiPermission {

    private OnRationaleListener mOnRationaleListener;
    private SimpleCallback mSimpleCallback;
    private FullCallback mFullCallback;

    private final LinkedHashSet<String> mPermissions = new LinkedHashSet<>();
    private final List<String> mPermissionsRequest = new ArrayList<>();
    private final List<String> mPermissionsGranted = new ArrayList<>();
    private List<String> mPermissionsDenied;
    private List<String> mPermissionsDeniedForever;


    public String[] getPermissionsRequest() {
        return mPermissionsRequest.toArray(new String[0]);
    }

    public static DiPermission instance;

    public static DiPermission build(String... permissions) {
        if (instance == null) {
            instance = new DiPermission(permissions);
        }
        return instance;
    }

    private DiPermission(String... permissions) {
        for (@PermissionConstants.Permission String permission : permissions) {
            //这里把你申请的权限中的权限组 转换成数组，检查单个权限是否在manifest中定义  做到权限申请最小化。
            //比如 你申请了一个Manifest.permission_group.STORAGE权限组，但是manifest却只定义了Manifest.permission.READ_EXTERNAL_STORAGE。
            // 如果拿着权限组去申请，会失败的。所以此时就只去申请Manifest.permission.READ_EXTERNAL_STORAGE
            for (String singlePermission : PermissionConstants.getPermissions(permission)) {
                //你申请的权限 必须在manifest 里面 已经定义了的才行
                if (PermissionUtil.MANIFEST_PERMISSIONS.contains(singlePermission)) {
                    mPermissions.add(singlePermission);
                }
            }
        }
    }

    /**
     * 如果被永远拒绝了 ，弹框解释为什么申请权限的回调
     *
     * @param listener 拒绝权限监听
     * @return 构建者自己
     */
    public DiPermission rationale(OnRationaleListener listener) {
        mOnRationaleListener = listener;
        return this;
    }

    /**
     * 简单回调，只会告诉你已授权 还是未授权，不区分那个权限
     *
     * @param callback 权限监听
     * @return 构建者自己
     */
    public DiPermission callback(SimpleCallback callback) {
        mSimpleCallback = callback;
        return this;
    }

    /**
     * 这个会回调 那些已授权，那些未授权，跟上面那个SimpleCallback看场景使用
     *
     * @param callback 权限监听
     * @return 构建者自己
     */
    public DiPermission callback(FullCallback callback) {
        mFullCallback = callback;
        return this;
    }

    /**
     * 开始申请权限
     */
    @SuppressLint("ObsoleteSdkInt")
    public void request() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mPermissionsGranted.addAll(mPermissions);
            requestCallback();
        } else {
            for (String permission : mPermissions) {
                if (PermissionUtil.isGranted(permission)) {
                    mPermissionsGranted.add(permission);
                } else {
                    mPermissionsRequest.add(permission);
                }
            }
            if (mPermissionsRequest.isEmpty()) {
                requestCallback();
            } else {
                startPermissionActivity();
            }
        }
    }

    private void requestCallback() {
        if (mSimpleCallback != null) {
            //只有当所有权限都被授权的时候才会回调onGranted，否则只会把被拒绝的权限回调到onDenied
            //你也可以改成 把本次授权的回调到onGranted，被拒绝的回调到onDenied
            if (mPermissionsRequest.size() == 0 || mPermissions.size() == mPermissionsGranted.size()
            ) {
                mSimpleCallback.onGranted();
            } else {
                if (!mPermissionsDenied.isEmpty()) {
                    mSimpleCallback.onDenied();
                }
            }
            mSimpleCallback = null;
        }

        if (mFullCallback != null) {
            //只有当所有权限都被授权的时候才会回调onGranted，否则只会把被拒绝的权限回调到onDenied
            //你也可以改成 把本次授权的回调到onGranted，被拒绝的回调到onDenied
            //但我们认为现在这种比较好，可以做到一次权限申请最小化，不要在首次启动一次性申请那么多，按需申请
            if (mPermissionsRequest.size() == 0 || mPermissions.size() == mPermissionsGranted.size()
            ) {
                mFullCallback.onGranted(mPermissionsGranted);
            } else {
                if (!mPermissionsDenied.isEmpty()) {
                    mFullCallback.onDenied(mPermissionsDeniedForever, mPermissionsDenied);
                }
            }
            mFullCallback = null;
        }
        mOnRationaleListener = null;
    }

    private void startPermissionActivity() {
        mPermissionsDenied = new ArrayList<String>();
        mPermissionsDeniedForever = new ArrayList<String>();
        assert PermissionUtil.sApplication != null;
        new PermissionActivity().start(
                PermissionUtil.sApplication.getApplicationContext(),
                PermissionActivity.TYPE_RUNTIME
        );
    }

    public boolean rationale(Activity activity) {
        boolean isRationale = false;
        if (mOnRationaleListener != null) {
            for (String permission : mPermissionsRequest) {
                if (shouldShowRequestPermissionRationale(activity, permission)) {
                    getPermissionsStatus(activity);
                    mOnRationaleListener.rationale(new ShouldRequest() {

                        @Override
                        public void again(boolean again) {
                            activity.finish();
                            if (again) {
                                startPermissionActivity();
                            } else {
                                requestCallback();
                            }
                        }
                    });
                    isRationale = true;
                    break;
                }
            }
            mOnRationaleListener = null;
        }
        return isRationale;
    }

    private void getPermissionsStatus(Activity activity) {
        for (String permission : mPermissionsRequest) {
            if (PermissionUtil.isGranted(permission)) {
                mPermissionsGranted.add(permission);
            } else {
                mPermissionsDenied.add(permission);
                if (!shouldShowRequestPermissionRationale(activity, permission)) {
                    mPermissionsDeniedForever.add(permission);
                }
            }
        }
    }

    public void onRequestPermissionsResult(Activity activity) {
        getPermissionsStatus(activity);
        requestCallback();
    }
}
