package com.zp.androidx.net


/**
 * Created by zhaopan on 2018/6/9.
 */
object HttpConstant {

    const val KEY_TOKEN = "token"
    const val KEY_CLOUD_TOKEN = "cloud_token"
    const val KEY_UUID = "uuid"
    const val KEY_USER_ID = "user_id"
    const val KEY_KICK_OFF = "kick_off"
    const val SETTING_DEBUG_API = "setting_debug_api"

    const val DEFAULT_TIMEOUT: Long = 15
    const val SAVE_USER_LOGIN_KEY = "user/login"
    const val SAVE_USER_REGISTER_KEY = "user/register"

    const val COLLECTIONS_WEBSITE = "lg/collect"
    const val UNCOLLECTIONS_WEBSITE = "lg/uncollect"
    const val ARTICLE_WEBSITE = "article"
    const val TODO_WEBSITE = "lg/todo"

    const val SET_COOKIE_KEY = "set-cookie"
    const val COOKIE_NAME = "Cookie"

    const val MAX_CACHE_SIZE: Long = 1024 * 1024 * 50 // 50M 的缓存大小

    fun encodeCookie(cookies: List<String>): String {
        val sb = StringBuilder()
        val set = HashSet<String>()
        cookies
                .map { cookie ->
                    cookie.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                }
                .forEach {
                    it.filterNot { set.contains(it) }.forEach { set.add(it) }
                }
        val ite = set.iterator()
        while (ite.hasNext()) {
            val cookie = ite.next()
            sb.append(cookie).append(";")
        }
        val last = sb.lastIndexOf(";")
        if (sb.length - 1 == last) {
            sb.deleteCharAt(last)
        }
        return sb.toString()
    }

    fun saveCookie(url: String?, domain: String?, cookies: String) {
        url ?: return
        NetUtils.setConfig(url, cookies)
        domain ?: return
        NetUtils.setConfig(domain, cookies)
        NetUtils.cookies = cookies
    }

}