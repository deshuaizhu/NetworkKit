package com.che168.network.httpdns;

import android.text.TextUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hcp on 16/2/18.
 */
public class HttpDnsCache {
    private static ConcurrentHashMap<String, HttpDnsBean> cache = null;

    /**
     * 通过host 获取缓存
     *
     * @param host
     * @return
     */
    public static HttpDnsBean getDoaminInfoByHost(String host) {
        if (!TextUtils.isEmpty(host) && cache != null) {
            return cache.get(host);
        }
        return null;
    }


    /**
     * 保存缓存
     *
     * @param host
     * @param domainInfo
     */
    public static void put(String host, HttpDnsBean domainInfo) {
        if (!TextUtils.isEmpty(host) && domainInfo != null) {
            if (cache == null) {
                cache = new ConcurrentHashMap<>();
            }
            cache.put(host, domainInfo);
        }
    }

    /**
     * 保存缓存
     *
     * @param host
     */
    public static void remove(String host) {
        if (!TextUtils.isEmpty(host) && cache != null) {
            cache.remove(host);
        }
    }

    public static void clear() {
        if (cache != null) {
            cache.clear();
            cache = null;
        }
    }
}
