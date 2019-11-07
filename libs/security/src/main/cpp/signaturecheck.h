//
// Created by zhaopan on 2019-11-06.
//

#define   LOG_SIGNATURE_CHECK_TAG    "signature_check_c_zp:::"
#define   LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_SIGNATURE_CHECK_TAG,__VA_ARGS__)
#define   LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_SIGNATURE_CHECK_TAG,__VA_ARGS__)
#define   LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_SIGNATURE_CHECK_TAG,__VA_ARGS__)

#ifndef AESJNIENCRYPT_SIGNACTURECHECK_H
#define AESJNIENCRYPT_SIGNACTURECHECK_H

/**
 * 校验APP 包名和签名是否合法
 *
 * 返回值为1 表示合法
 */
int verify_sign(JNIEnv *env);

/**
 * 计算data的MD5值
 * @param data  字符串
 * @param len   字符串长度
 * @return
 */
char *calc_md5(const char *data, int len);

#endif //AESJNIENCRYPT_SIGNACTURECHECK_H
