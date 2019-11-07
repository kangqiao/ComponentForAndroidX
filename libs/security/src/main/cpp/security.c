#include <android/log.h>
#include <string.h>
#include <strings.h>
#include <jni.h>
#include <malloc.h>
#include <stdbool.h>
#include "signaturecheck.h"


#define   LOG_TAG    "security_c_zp:::"
#define   LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define   LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define   LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


static int is_sign_right = 0;


static jboolean check_signature(JNIEnv *env, jclass clazz) {
    if (is_sign_right) {
        return true;
    }
    return false;
}

static jstring get_api_sign_id(JNIEnv *env, jclass clazz) {
    if (is_sign_right) {
        return (*env)->NewStringUTF(env, "right_sign_id");
    }
    return (*env)->NewStringUTF(env, "wrong_sign_id");
}

static jstring get_api_sign_key(JNIEnv *env, jclass clazz) {
    if (is_sign_right) {
        return (*env)->NewStringUTF(env, "right_sign_key");
    }
    return (*env)->NewStringUTF(env, "wrong_sign_key");
}

static jstring get_md5(JNIEnv *env, jclass clazz, jstring content) {
    char *dest; //目标字符串
    const char *data = (*env)->GetStringUTFChars(env, content, 0);
    int len = (*env)->GetStringLength(env, content);

    dest = calc_md5(data, len);
    LOGI("get_md5: calc_md5(%s, %d) =>%s<", data, len, dest);

    jstring ret = (*env)->NewStringUTF(env, dest);
    free(dest);

    return ret;
}

static JNINativeMethod methods[] = {
        {"_checkSignature", "()Ljava/lang/Boolean;",                  (void *) check_signature},
        {"_getApiSignId",   "()Ljava/lang/String;",                   (void *) get_api_sign_id},
        {"_getApiSignKey",  "()Ljava/lang/String;",                   (void *) get_api_sign_key},
        {"_md5",            "(Ljava/lang/String;)Ljava/lang/String;", (void *) get_md5},
};


jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (JNI_OK == (*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4)) {
        if (verify_sign(env) == JNI_OK) {
            is_sign_right = 1;
            LOGI("签名验证成功!!!");
        } else {
            LOGE("签名不一致!!!");
        }

        const char *className = "com/zp/androidx/security/SecurityManager";
        jclass clazz = (*env)->FindClass(env, className);
        if (clazz != NULL) {
            int methodsLength = sizeof(methods) / sizeof(methods[0]);
            if ((*env)->RegisterNatives(env, clazz, methods, methodsLength) < 0) {
                LOGE("RegisterNatives failed for '%s'", className);
            }
            (*env)->DeleteLocalRef(env, clazz);
        } else {
            LOGE("not find class '%s'", className);
        }
    }
    return JNI_VERSION_1_4;
}
