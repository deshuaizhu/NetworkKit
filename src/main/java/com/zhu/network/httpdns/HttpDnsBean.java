package com.che168.network.httpdns;

import android.text.TextUtils;

/**
 * HttpDnsBean
 * <p>
 * Created by hcp on 16/2/18.
 */
public class HttpDnsBean {
    public long time;
    public int ttl;
    public String netWorkType;
    public String ip;
    public String host;

    public HttpDnsBean(String ip, int ttl, long time, String netWorkType, String host) {
        this.time = time;
        this.ttl = ttl;
        this.netWorkType = netWorkType;
        this.ip = ip;
        this.host = host;
    }

    public String getDnspodUrl(String url) {
        if (!TextUtils.isEmpty(url) &&
                !TextUtils.isEmpty(host) &&
                !TextUtils.isEmpty(ip)) {
            return url.replace(host, ip);
        }
        return url;
    }
}
