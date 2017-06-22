package com.che168.ahnetwork.httpdns;

import java.util.ArrayList;
import java.util.List;

/**
 * Dns配置类
 * <p/>
 * Created by hcp on 16/2/18.
 */
public class HttpDnsConfig {
    /**
     * DNSPOD 服务器地址
     */
    public static final String DNSPOD_SERVER_API = "http://119.29.29.29/d?ttl=1&dn=";
    /**
     * 是否开启DNS解析
     */
    public static boolean enableDnsPod = false;
    /**
     * 是否开启 webview DNS解析
     */
    public static boolean enableHtmlDnsPod = false;
    /**
     * 是否开启 DNS Ping功能
     */
    public static boolean enableDnsPing = false;

    /**
     * DNS解析版本
     */
    public static DnsPodVersion dnsPodVersion = DnsPodVersion.Enterprise;


    /**
     * 需要解析的域名地址
     */
    public static List<String> domains = null;

    /**
     * 加载默认设置
     */
    public static void initConfig(){
        domains = new ArrayList<>();
        domains.add("https://appapi.che168.com");
        domains.add("https://appsapi.che168.com");
        domains.add("https://m.che168.com");
        domains.add("https://app.che168.com");
        domains.add("https://mhao.autohome.com.cn");
        domains.add("https://cacheapi.che168.com");
        //图片
        domains.add("https://2sc0.autoimg.cn");
        domains.add("https://2sc2.autoimg.cn");
        domains.add("https://2sc1.autoimg.cn");
        domains.add("https://car0.autoimg.cn");
        domains.add("https://car2.autoimg.cn");
        domains.add("https://car3.autoimg.cn");
        domains.add("https://img.autoimg.cn");
        domains.add("https://img2.autoimg.cn");
        domains.add("https://www.autoimg.cn");
        domains.add("https://x.autoimg.cn");
        domains.add("https://s.autoimg.cn");
        domains.add("https://i1.autoimg.cn");
        domains.add("https://i2.autoimg.cn");
        domains.add("https://i3.autoimg.cn");
        domains.add("https://adm3.autoimg.cn");
        domains.add("https://baojia0.autoimg.cn");

        dnsPodVersion = DnsPodVersion.Enterprise;
    }

    /**
     * 选择版本
     */
    public enum DnsPodVersion {
        Free, Enterprise
    }

    /**
     * 企业加密版本
     **/
    // dn 参数对应为加密后的字符串，id 对应为您的密钥 ID
    // http://119.29.29.29/d?dn=ac7875d400dacdf09954edd788887719&id=1&ttl=1
    // 结果：59.37.96.63;14.17.32.211;14.17.42.40,56

    /**
     * 免费版本
     **/
    // 请求格式为“http://119.29.29.29/d?dn=www.dnspod.cn.&ip=1.1.1.1&ttl=1”
    // dn 表示要查询的域名，
    // ip 表示用户 ip，可以不携带 ip 参数，当没有这个 ip 参数时，服务器会把 http 报文的源 ip 当做用户 ip。
    // ttl=1 表示要求 D+服务器在响应结果中携带解析结果的 ttl 值，返回的ttl 和域名解析结果用英文逗号分割

    // 接入指南：https://www.dnspod.cn/httpdns/guide

    /**
     * DnsPod 企业版本 Id
     */
    public static final String DnspodEnterpriseId = "123";
//    public static final String DnspodEnterpriseId = "10";
    /**
     * DnsPod 企业版本 Key
     */
    public static final String DnspodEnterpriseKey = "29Blp39J";
//    public static final String DnspodEnterpriseKey = ")9cRHN@a";
}
