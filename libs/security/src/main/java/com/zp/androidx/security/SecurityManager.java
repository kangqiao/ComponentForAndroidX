package com.zp.androidx.security;

/**
 * Created by zhaopan on 2019-11-06.
 */
public final class SecurityManager {

    private static final String TAG = "SecurityManager";

    static {
        System.loadLibrary("security");
    }

    public static Integer getHostType() {
        return 1;
    }

    public static boolean checkSignature() {
        if (BuildConfig.DEBUG) {
            return true;
        }
        return _checkSignature();
    }

    /**
     * 获取接口签名Id
     * @return
     */
    public static String getApiSignId() {
        if (BuildConfig.DEBUG) {
            return "debug_sign_id";
        }
        return _getApiSignId();
    }

    /**
     * 获取接口签名Key
     * @return
     */
    public static String getApiSignKey() {
        if (BuildConfig.DEBUG) {
            return "debug_sign_key";
        }
        return _getApiSignKey();
    }

    /**
     * 计算Md5值
     * @param content
     * @return
     */
    public static String calcMD5(String content) {
        if (BuildConfig.DEBUG) {
            return "debug_calc_md5";
        }
        return _md5(content);
    }

    private static native Boolean _checkSignature();

    private static native String _getApiSignId();

    private static native String _getApiSignKey();

    private static native String _md5(String content);

}
