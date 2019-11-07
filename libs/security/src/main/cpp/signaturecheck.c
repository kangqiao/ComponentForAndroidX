//
// Created by zhaopan on 2019-11-06.
//

#include <string.h>
#include <android/log.h>
#include <jni.h>
#include <malloc.h>
#include "signaturecheck.h"
#include "md5.h"


const int HOST_TYPE_ERROR   = -1;
const int HOST_TYPE_APP     = 1;
const int HOST_TYPE_APP2    = 2;

/**
 * 通过应用进程主启动类ActivityThread.currentApplication()获取根Application对象.
 * 避免Hook.
 * @param env
 * @return
 */
static jobject get_application(JNIEnv *env) {
    jobject application = NULL;
    jclass activity_thread_clz = (*env)->FindClass(env, (const char *)"android/app/ActivityThread");
    if (activity_thread_clz != NULL) {
        jmethodID currentApplication = (*env)->GetStaticMethodID(env, activity_thread_clz, (const char *)"currentApplication", "()Landroid/app/Application;");
        if (currentApplication != NULL) {
            application = (*env)->CallStaticObjectMethod(env, activity_thread_clz, currentApplication);
            LOGI("getApplication successful!");
        } else {
            LOGE("Cannot find method: currentApplication() in ActivityThread.");
        }
        (*env)->DeleteLocalRef(env, activity_thread_clz);
    } else {
        LOGE("Cannot find class: android.app.ActivityThread.");
    }

    return application;
}

/**
 * 反射调用com.zp.androidx.SecurityKey.getHostType()方法
 * 获取当前业务线编码.
 * @param env
 * @return
 */
static int get_host_type(JNIEnv *env) {
    int ret = 0;
    jclass security_key_clz = (*env)->FindClass(env, (const char *) "com/zp/androidx/security/SecurityManager");
    if (security_key_clz != NULL) {
        jmethodID getBusinessLine = (*env)->GetStaticMethodID(env, security_key_clz, (const char *) "getHostType", "()Ljava/lang/Integer;");
        if (getBusinessLine != NULL) {
            jobject line_obj = (*env)->CallStaticObjectMethod(env, security_key_clz, getBusinessLine);
            if (line_obj != NULL) {
                jclass integer_clz = (*env)->FindClass(env, "java/lang/Integer");
                if (integer_clz != NULL) {
                    jmethodID intValue = (*env)->GetMethodID(env, integer_clz, "intValue", "()I");
                    if (intValue != NULL) {
                        ret = (*env)->CallIntMethod(env, line_obj, intValue);
                    }
                }
                (*env)->DeleteLocalRef(env, integer_clz);
            }
        } else {
            LOGE("Cannot find method: getHostType() in SecurityManager.");
        }
        (*env)->DeleteLocalRef(env, security_key_clz);
    } else {
        LOGE("Cannot find class: com.zp.androidx.security.SecurityManager.");
    }

    LOGI("get_host_type result(%d)", ret);
    return ret;
}

/**
 * 计算data的MD5值
 * @param data  字符串
 * @param len   字符串长度
 * @return
 */
char *calc_md5(const char *data, int len) {
    MD5_CTX ctx;
    unsigned char md[16];
    char buf[32] = {0};
    char *dest = NULL;

    if (NULL == data || len <= 0) {
        LOGE("calc_md5: The parameter(data) may be NULL, Or len has to be greater than 0.");
        return NULL;
    }

    MD5Init(&ctx);
    MD5Update(&ctx, data, len);
    MD5Final(md, &ctx);

    for (int i = 0; i < 16; i++) {
        sprintf(buf, "%s%02X", buf, md[i]);
    }
    int size = sizeof(char) * sizeof(buf);
    //LOGI("calc_md5=>%s<", buf);
    dest = (char *) malloc(size + 1);
    memset(dest, 0, size + 1);
    strncpy(dest, buf, size);
    return dest;
}

/**
 * JNI JByteArray转char*
 * https://blog.csdn.net/bzlj2912009596/article/details/78715658
 * http://www.liuling123.com/2016/06/jni-type-change.html
 */
static char *convert_jbytearray_to_chars(JNIEnv *env, jbyteArray byteArray, int chars_len) {
    char *chars = NULL;
    jbyte *bytes;
    if (NULL == byteArray || chars_len <= 0) {
        LOGE("convert_jbytearray_to_chars: The parameter(byteArray) may be NULL, Or chars_len has to be greater than 0.");
        return NULL;
    }
    //int chars_len = (*env)->GetArrayLength(env, byteArray);
    bytes = (*env)->GetByteArrayElements(env, byteArray, 0);
    chars = malloc(chars_len + 1);
    memset(chars, 0, chars_len + 1);
    memcpy(chars, bytes, chars_len);
    chars[chars_len] = 0;

    (*env)->ReleaseByteArrayElements(env, byteArray, bytes, 0);

    return chars;
}

/**
 * 比较 <获取的应用包的签名MD5值> 和 <写死的宿主工程签名MD5值>
 * @param line
 * @param sign_md5
 * @return
 */
static int compare_sign_md5(int type, const char *sign_md5) {
    int result = JNI_ERR;
    int cmp = HOST_TYPE_ERROR;
    if (type == HOST_TYPE_APP) {
        static const char *PRO_SIGN_MD5 = "ADC7DE60BB8AC1E654583403652E9568";
        cmp = strncmp(sign_md5, PRO_SIGN_MD5, 32);
    } else if (type == HOST_TYPE_APP2) {
        static const char *PRO_SIGN_MD5 = "22222222222222222222222222222222";
        cmp = strncmp(sign_md5, PRO_SIGN_MD5, 32);
    } else {
        LOGE("Unmatched host app(%d)!!!", type);
    }

    result = cmp == 0 ? JNI_OK : JNI_ERR;
    LOGI("compare_sign_md5 <JNI_OK=%d,JNI_ERR=%d> result(%d) ", JNI_OK, JNI_ERR, result);

    return result;
}

/**
 * 校验应用签名信息的MD5值是否正确.
 * @param env
 * @return
 */
int verify_sign(JNIEnv *env) {
    int result;
    int line;
    int len = 0;

    // Application object
    jobject application = get_application(env);
    if (application == NULL) {
        return JNI_ERR;
    }

    // Context(ContextWrapper) class
    jclass context_clz = (*env)->GetObjectClass(env, application);
    // getPackageManager()
    jmethodID getPackageManager = (*env)->GetMethodID(env, context_clz, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    // android.content.pm.PackageManager object
    jobject package_manager = (*env)->CallObjectMethod(env, application, getPackageManager);
    // PackageManager class
    jclass package_manager_clz = (*env)->GetObjectClass(env, package_manager);
    // getPackageInfo()
    jmethodID getPackageInfo = (*env)->GetMethodID(env, package_manager_clz, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    // context.getPackageName()
    jmethodID getPackageName = (*env)->GetMethodID(env, context_clz, "getPackageName", "()Ljava/lang/String;");
    // call getPackageName() and cast from jobject to jstring
    jstring package_name = (jstring) ((*env)->CallObjectMethod(env, application, getPackageName));
    // PackageInfo object
    jobject package_info = (*env)->CallObjectMethod(env, package_manager, getPackageInfo, package_name, 64);
    // class PackageInfo
    jclass package_info_clz = (*env)->GetObjectClass(env, package_info);
    // field signatures
    jfieldID signatures_field = (*env)->GetFieldID(env, package_info_clz, "signatures", "[Landroid/content/pm/Signature;");
    jobject signatures = (*env)->GetObjectField(env, package_info, signatures_field);
    jobjectArray signatures_array = (jobjectArray) signatures;
    jobject signature0 = (*env)->GetObjectArrayElement(env, signatures_array, 0);
    jclass signature_clz = (*env)->GetObjectClass(env, signature0);

    jmethodID toByteArray = (*env)->GetMethodID(env, signature_clz, "toByteArray", "()[B");
    // call toByteArray()
    jbyteArray signatureToByteArray = (jbyteArray) ((*env)->CallObjectMethod(env, signature0, toByteArray));

    /*
    //Begin Test
    jmethodID toCharsString = (*env)->GetMethodID(env, signature_clz, "toCharsString", "()Ljava/lang/String;");
    // call toCharsString()
    jstring signature_str = (jstring) ((*env)->CallObjectMethod(env, signature0, toCharsString));
    const char *sign = (*env)->GetStringUTFChars(env, signature_str, NULL);
    //发布记得去掉log！！！！！！！
    LOGI("应用中读取到的签名为：%s", sign);
    //End Test
     */

    // release
    (*env)->DeleteLocalRef(env, application);
    (*env)->DeleteLocalRef(env, context_clz);
    (*env)->DeleteLocalRef(env, package_manager);
    (*env)->DeleteLocalRef(env, package_manager_clz);
    (*env)->DeleteLocalRef(env, package_name);
    (*env)->DeleteLocalRef(env, package_info);
    (*env)->DeleteLocalRef(env, package_info_clz);
    (*env)->DeleteLocalRef(env, signatures);
    (*env)->DeleteLocalRef(env, signature0);
    (*env)->DeleteLocalRef(env, signature_clz);

    len = (*env)->GetArrayLength(env, signatureToByteArray);
    //char * signature_byte_arr = (char *)(*env)->GetByteArrayElements(env, signatureToByteArray, NULL);
    char *signature_byte_arr = convert_jbytearray_to_chars(env, signatureToByteArray, len);

    if (signature_byte_arr != NULL) {
        char *signature_md5 = calc_md5(signature_byte_arr, len);
        if (signature_md5 != NULL) {
            line = get_host_type(env);

            //检验签名是否正确
            result = compare_sign_md5(line, signature_md5);

            //释放cacl_md5函数中分配的内存.
            free(signature_md5);
        }
        free(signature_byte_arr);
    }
    (*env)->DeleteLocalRef(env, signatureToByteArray);

    LOGI("verify_sign: result(%d)", result);
    return result;
}



/*

static jstring charToJstring(JNIEnv *env, char *src) {

    jsize len = strlen(src);
    jclass clsstring = (*env)->FindClass(env, "java/lang/String");
    jstring strencode = (*env)->NewStringUTF(env, "UTF-8");
    jmethodID mid = (*env)->GetMethodID(env, clsstring, (const char *) "<init>",
                                        "([BLjava/lang/String;)V");
    jbyteArray barr = (*env)->NewByteArray(env, len);
    (*env)->SetByteArrayRegion(env, barr, 0, len, (jbyte *) src);

    return (jstring) (*env)->NewObject(env, clsstring, mid, barr, strencode);
}

static jint checkSignature(JNIEnv *env, jobject thiz, jobject context) {
    //Context的类
    jclass context_clazz = (*env)->GetObjectClass(env, context);
    // 得到 getPackageManager 方法的 AID
    jmethodID methodID_getPackageManager = (*env)->GetMethodID(env,
                                                               context_clazz, "getPackageManager",
                                                               "()Landroid/content/pm/PackageManager;");

    // 获得PackageManager对象
    jobject packageManager = (*env)->CallObjectMethod(env, context,
                                                      methodID_getPackageManager);
//	// 获得 PackageManager 类
    jclass pm_clazz = (*env)->GetObjectClass(env, packageManager);
    // 得到 getPackageInfo 方法的 AID
    jmethodID methodID_pm = (*env)->GetMethodID(env, pm_clazz, "getPackageInfo",
                                                "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
//
//	// 得到 getPackageName 方法的 AID
    jmethodID methodID_pack = (*env)->GetMethodID(env, context_clazz,
                                                  "getPackageName", "()Ljava/lang/String;");

    // 获得当前应用的包名
    jstring application_package = (*env)->CallObjectMethod(env, context,
                                                           methodID_pack);
    const char *package_name = (*env)->GetStringUTFChars(env,
                                                         application_package, 0);
    //LOGE("packageName: %s\n", package_name);

    // 获得PackageInfo
    jobject packageInfo = (*env)->CallObjectMethod(env, packageManager,
                                                   methodID_pm, application_package, 64);

    jclass packageinfo_clazz = (*env)->GetObjectClass(env, packageInfo);
    jfieldID fieldID_signatures = (*env)->GetFieldID(env, packageinfo_clazz,
                                                     "signatures", "[Landroid/content/pm/Signature;");
    jobjectArray signature_arr = (jobjectArray) (*env)->GetObjectField(env,
                                                                       packageInfo, fieldID_signatures);
    //Signature数组中取出第一个元素
    jobject signature = (*env)->GetObjectArrayElement(env, signature_arr, 0);
    //读signature的hashcode
    jclass signature_clazz = (*env)->GetObjectClass(env, signature);
    jmethodID methodID_hashcode = (*env)->GetMethodID(env, signature_clazz,
                                                      "hashCode", "()I");
    jint hashCode = (*env)->CallIntMethod(env, signature, methodID_hashcode);
    LOGE("hashcode: %d\n", hashCode);

    if (strcmp(package_name, app_packageName) != 0) {
        return -1;
    }
    if (hashCode != app_signature_hash_code) {
        return -2;
    }
    return hashCode;
}

*/
