package com.che168.network.httpdns;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.che168.network.http.HttpUtil;
import com.che168.network.http.callback.Failed;
import com.che168.network.http.callback.Success;
import com.che168.network.http.exception.BaseHttpException;
import com.che168.network.httpdns.util.Hex;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Dnspod http
 * <p/>
 * Created by hcp on 16/1/20.
 */
public class HttpDNS {

	private static final String TAG = "HttpDNS";

	private static HttpDNS instance = new HttpDNS();
	//存正在异步解析中的host
	private static HashMap<String, String> resolvingMap = new HashMap<>();

	private static HttpDNSAnalyticAgentListener httpDNSAnalyticAgentListener;


	private HttpDNS() {
		if (HttpDnsConfig.domains == null) {
			HttpDnsConfig.initConfig();
		}
	}

	public static HttpDNS getInstance() {
		return instance;
	}

	/**
	 * 添加统计事件用到
	 *
	 * @param httpDNSAnalyticAgentListener
	 */
	public static void setHttpDNSAnalyticAgentListener(
			HttpDNSAnalyticAgentListener httpDNSAnalyticAgentListener) {
		HttpDNS.httpDNSAnalyticAgentListener = httpDNSAnalyticAgentListener;
	}

	/**
	 * 根据URL取缓存中dnspod结果 针对于WebView
	 *
	 * @param context
	 * @param url
	 * @return
	 */
	public String getWebViewDnspodUrl(Context context, String url) {
		boolean isParse = checkWebViewDnsPod(url);//是否需要Dnspod解析
		String host = null;
		if (isParse) {
			//判断是否有host
			host = getUrlHost(url);
			if (TextUtils.isEmpty(host)) {
				isParse = false;
			}
		}

		if (isParse) {
			return getDnspodCacheUrl(context, host, url);
		} else {
			return url;
		}
	}

	/**
	 * 根据URL取缓存中dnspod结果 针对http请求
	 *
	 * @param context
	 * @param url
	 * @return
	 */
	public void getHttpDnspodUrl(Context context, String url, final HttpDNSListener listener) {
		if (listener == null) {
			return;
		}
		boolean isParse = false;
		String host = null;

		if (HttpDnsConfig.enableDnsPod) {//是否开启了Dnspod
			isParse = checkMeetDnspod(url);//验证此URL满足dns解析条件
			if (isParse) {
				//判断是否有host
				host = getUrlHost(url);
				if (TextUtils.isEmpty(host)) {
					isParse = false;
				}
			}
		}

		if (isParse) {
			listener.finish(host, getDnspodCacheUrl(context, host, url));
		} else {
			listener.finish(null, url);
		}
	}

	/**
	 * 根据URL取缓存中dnspod结果 针对http请求
	 *
	 * @param context
	 * @param url
	 * @return
	 */
	public String getHttpDnspodUrl(Context context, String url) {
		boolean isParse = false;
		String host = null;

		if (HttpDnsConfig.enableDnsPod) {//是否开启了Dnspod
			isParse = checkMeetDnspod(url);//验证此URL满足dns解析条件
			if (isParse) {
				//判断是否有host
				host = getUrlHost(url);
				if (TextUtils.isEmpty(host)) {
					isParse = false;
				}
			}
		}

		if (isParse) {
			return getDnspodCacheUrl(context, host, url);
		} else {
			return url;
		}
	}

	/**
	 * 通过url获取host
	 *
	 * @param url
	 * @return
	 */
	public String getUrlHost(String url) {
		String host = null;
		try {
			URL oldUrl = new URL(url);
			host = oldUrl.getHost();
		} catch (MalformedURLException e) {
			host = null;
		}
		return host;
	}

	/**
	 * 根据URL取缓存中dnspod结果，如果缓存失效或没有返回原Url，并进行异步DnsPod解析工作
	 *
	 * @param context
	 * @param url
	 * @return
	 */
	private String getDnspodCacheUrl(Context context, String host, String url) {
		Log.i(TAG, "getDnspodCacheUrl: ...[host:" + host + "][url:" + url + "]");
		boolean isCacheExpires = false;//缓存是否过期
		boolean isAsyncParseDns = false;//是否需要异步解析DNS

		String netWorkType = NetworkUtil.getNetworkType(context);
		//判断缓存是否过期
		HttpDnsBean domainInfo = HttpDnsCache.getDoaminInfoByHost(host);
		if (domainInfo != null) {
			if (netWorkType.equals(domainInfo.netWorkType)) {
				long time = (System.currentTimeMillis() - domainInfo.time) / 1000;
				long cacheRemainingTime = domainInfo.ttl - time;//缓存剩余时间

				//1.如果当前http请求使用dns结果有效时间大于0s时，使用当前dns结果。
				if (cacheRemainingTime < 1) {
					isCacheExpires = true;//缓存过期了
				}
				//2.如果当前http请求使用dns结果有效时间小于15s时，请求新的dns结果。
				if (cacheRemainingTime < 15) {
					isAsyncParseDns = true;
				}
			}
		} else {
			isCacheExpires = true;
		}
		if (isAsyncParseDns || isCacheExpires) {//需要异步Dns解析或者缓存过期
			Log.i(TAG, "getDnspodCacheUrl: 异步进行Dns解析");
			asyncParseDns(context, host, netWorkType);
		}

		if (isCacheExpires) {//缓存过期了
			Log.i(TAG, "getDnspodCacheUrl: 缓存过期了");
			return url;
		} else {
			if (httpDNSAnalyticAgentListener != null) {
				httpDNSAnalyticAgentListener.devDnsResultState(3);
			}
			Log.i(TAG, "getDnspodCacheUrl: 取缓存中的Dns解析结果");
			return domainInfo.getDnspodUrl(url);
		}
	}


	/**
	 * 进行域名解析 异步方式
	 *
	 * @param host        默认Host
	 * @param netWorkType 网络类型
	 */
	private void asyncParseDns(final Context context, final String host, final String netWorkType) {
		if (TextUtils.isEmpty(host) || TextUtils.isEmpty(netWorkType))
			return;
		//3.如果当前http请求使用dns结果有效时间小于15s并且当前正在请求dns结果，不再发起新的请求dns结果。
		final String resolvingKey = host + netWorkType;
		if (resolvingMap.containsKey(resolvingKey)) {
			return;
		} else {
			resolvingMap.put(resolvingKey, resolvingKey);
		}
		Log.i(TAG, "asyncParseDns:开始Dns解析...[" + host + "]");

		final String dnspodUrl;
		if (HttpDnsConfig.DnsPodVersion.Enterprise.equals(HttpDnsConfig.dnsPodVersion)) {
			dnspodUrl = HttpDnsConfig.DNSPOD_SERVER_API + encrypt(host);
		} else {
			dnspodUrl = HttpDnsConfig.DNSPOD_SERVER_API + host;
		}
		final long startTime = System.currentTimeMillis();
		//替换网络请求
		HttpUtil.Builder builder = new HttpUtil.Builder();
		builder.url(dnspodUrl)
				.success(new Success() {
					@Override
					public void success(String responseStr) {
						resolvingMap.remove(resolvingKey);

						String result = responseStr;
						if (HttpDnsConfig.DnsPodVersion.Enterprise.equals(
								HttpDnsConfig.dnsPodVersion)) {//如果是企业版
							result = decrypt(responseStr);
						}

						HttpDnsBean domainInfo = strToEntity(context, result, host, netWorkType);

						if (domainInfo != null && httpDNSAnalyticAgentListener != null) {
							Log.i(TAG, "asyncParseDns:Dns解析成功[" + host + ":"
									+ domainInfo.ip + "(ttl:" + domainInfo.ttl + ")]");

							httpDNSAnalyticAgentListener.devDnsResultState(1);

							long time = System.currentTimeMillis() - startTime;
							httpDNSAnalyticAgentListener.devDnsResultStateSuccessTime(
									netWorkType, time, domainInfo.ttl);
						}
					}
				})
				.failed(new Failed() {
					@Override
					public void failed(BaseHttpException exception) {
						Log.i(TAG, "asyncParseDns:Dns解析失败[" + exception == null ? "" : exception.toString() + "]");
						resolvingMap.remove(resolvingKey);
						if (httpDNSAnalyticAgentListener != null) {
							httpDNSAnalyticAgentListener.devDnsResultState(2);
							httpDNSAnalyticAgentListener.reportDevDnsResultStateFail(exception == null ? "" : exception.toString());
						}
					}
				})
				.doRequest();
	}

	/**
	 * Dnspod 返回数据解析为对应实体
	 *
	 * @param response    响应内容
	 * @param host        默认Host
	 * @param netWorkType 网络类型
	 * @return
	 */
	private HttpDnsBean strToEntity(Context context, String response, String host, String netWorkType) {
		if (TextUtils.isEmpty(response))
			return null;

		if (TextUtils.isEmpty(host))
			return null;

		String[] r1 = response.split(",");
		if (r1.length != 2) {
			return null;
		}
		int ttl = Integer.parseInt(r1[1]);

		String[] ips = r1[0].split(";");
		if (ips.length < 1) {
			return null;
		}
		Random r = new Random();
		String dnspodIp = ips[r.nextInt(ips.length)];
//        String dnspodIp = ips[0];
		HttpDnsBean domainInfo = new HttpDnsBean(dnspodIp, ttl, System.currentTimeMillis(), netWorkType, host);
		HttpDnsCache.put(host, domainInfo);

		if (HttpDnsConfig.enableDnsPing && context != null && ips.length > 1) {
			HttpDnsPingUtil.startPing(context, host, Arrays.asList(ips), new HttpDnsPingUtil.PingListener() {
				@Override
				public void onFinished(String host, String ip) {
					if (!TextUtils.isEmpty(host) && !TextUtils.isEmpty(ip)) {
						HttpDnsBean domainInfo = HttpDnsCache.getDoaminInfoByHost(host);
						if (domainInfo != null) {
							domainInfo.ip = ip;
							HttpDnsCache.put(host, domainInfo);
						}
					}
					System.out.println("hcp-->host:" + host + "**ip:" + ip);
					HttpDnsPingUtil.stopPing();
				}
			});
		}
		return domainInfo;
		//以后扩展返回多个ip
//            HttpDnsBean[] domainInfos = new HttpDnsBean[ips.length];
//            long time = System.currentTimeMillis();
//            for (int i = 0; i < ips.length; i++) {
//                String dnspodUrl = defaultUrl.replace(oldUrl.getHost(), ips[i]);
//                domainInfos[i] = new HttpDnsBean(ips[i], ttl, time, netWorkType, defaultUrl, dnspodUrl);
//            }
//            return domainInfos;
	}

	/**
	 * 判断URL是否需要Dns解析 针对于WebView
	 *
	 * @param url
	 * @return
	 */
	public boolean checkWebViewDnsPod(String url) {
		//是否开启了Dnspod
		if (HttpDnsConfig.enableHtmlDnsPod) {
			return checkMeetDnspod(url);
		}
		return false;
	}

	/**
	 * 验证此URL满足dns解析条件
	 *
	 * @param url
	 * @return
	 */
	private boolean checkMeetDnspod(String url) {
		//是否满足Dnspod解析条件
		boolean result = false;

		//如果是域名解析服务器地址直接请求而不进行Dnspod解析，是否进行域名解析开关
		if (!TextUtils.isEmpty(url) &&
				!url.startsWith(HttpDnsConfig.DNSPOD_SERVER_API) && //过滤 Dnspod 服务器
				HttpDnsConfig.domains != null &&
				HttpDnsConfig.domains.size() > 0) {
			//判断是否需要进行域名解析
			for (String list : HttpDnsConfig.domains) {
				if (url.startsWith(list)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * 移出不解析的host
	 *
	 * @param host
	 */
	public synchronized void removeDomain(String host) {
		if (!TextUtils.isEmpty(host) && HttpDnsConfig.domains != null
				&& HttpDnsConfig.domains.size() > 0) {
			String tmpHost = host;
			if (!tmpHost.startsWith("http://")) {
				tmpHost = "http://" + host;
			}
			if (HttpDnsConfig.domains.contains(tmpHost)) {
				HttpDnsConfig.domains.remove(tmpHost);
			}
		}
	}

	/**
	 * 域名加密
	 *
	 * @param domain
	 * @return
	 */
	private String encrypt(String domain) {
		try {
			SecretKeySpec key = new SecretKeySpec(HttpDnsConfig.DnspodEnterpriseKey.getBytes("utf-8"), "DES");

			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encryptedString = cipher.doFinal(domain.getBytes("utf-8"));
			return Hex.encodeHexString(encryptedString) + "&id=" + HttpDnsConfig.DnspodEnterpriseId;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * Dnspod 数据解密
	 *
	 * @param data
	 * @return
	 */
	private String decrypt(String data) {
		try {
			SecretKeySpec key = new SecretKeySpec(HttpDnsConfig.DnspodEnterpriseKey.getBytes("utf-8"), "DES");

			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] decrypted = cipher.doFinal(Hex.decodeHex(data.toCharArray()));

			return new String(decrypted);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public interface HttpDNSListener {
		void finish(String host, String url);
	}

	/**
	 * 4.dns统计 dev_dns_result_state   换为 dev_dns_result_state_new   ，
	 * 5.dev_dns_result_state_success_time换为dev_dns_result_state_success_time_new
	 * 参数：key:time（请求时间）  ,value: 1-1000ms  平分为50份，每份20ms，1001-3000ms平分为40份，每份50ms。
	 * 添加网络制式2G，3G，4G，wifi，无网络。例子：3G－1-20
	 * key:ttl （ttl有效时间）  ,value:ttl有效时间／60s   单位：分钟  ，大于900分钟，统一记为900分钟+
	 */
	public interface HttpDNSAnalyticAgentListener {
		void reportDevDnsResultStateFail(String errorcontent);

		/**
		 * （1:成功，2:失败，3:缓存）
		 *
		 * @param result
		 */
		void devDnsResultState(int result);

		void devDnsResultStateSuccessTime(String netWorkType, long time, int ttl);
	}


	private static class NetworkUtil {

		/**
		 * Get the network info
		 *
		 * @return
		 */
		public static NetworkInfo getNetworkInfo(Context context) {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			return cm.getActiveNetworkInfo();
		}

		/**
		 * Check if there is any connectivity
		 *
		 * @return
		 */
		public static boolean isConnected(Context context) {
			NetworkInfo info = NetworkUtil.getNetworkInfo(context);
			return (info != null && info.isConnected());
		}

		/**
		 * Check if there is any connectivity to a Wifi network
		 *
		 * @return
		 */
		public static boolean isWifi(Context context) {
			NetworkInfo info = NetworkUtil.getNetworkInfo(context);
			return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
		}

		public static String getNetworkType(Context context) {
			if (context == null) return "unknown";
			if (isConnected(context)) {
				if (isWifi(context)) {
					return "Wifi";
				} else {
					TelephonyManager mTelephonyManager = (TelephonyManager)
							context.getSystemService(Context.TELEPHONY_SERVICE);
					int networkType = mTelephonyManager.getNetworkType();
					switch (networkType) {
						case TelephonyManager.NETWORK_TYPE_GPRS:
						case TelephonyManager.NETWORK_TYPE_EDGE:
						case TelephonyManager.NETWORK_TYPE_CDMA:
						case TelephonyManager.NETWORK_TYPE_1xRTT:
						case TelephonyManager.NETWORK_TYPE_IDEN:
							//2G信号
							return "2G";
						case TelephonyManager.NETWORK_TYPE_UMTS:
						case TelephonyManager.NETWORK_TYPE_EVDO_0:
						case TelephonyManager.NETWORK_TYPE_EVDO_A:
						case TelephonyManager.NETWORK_TYPE_HSDPA:
						case TelephonyManager.NETWORK_TYPE_HSUPA:
						case TelephonyManager.NETWORK_TYPE_HSPA:
						case TelephonyManager.NETWORK_TYPE_EVDO_B:
						case TelephonyManager.NETWORK_TYPE_EHRPD:
						case TelephonyManager.NETWORK_TYPE_HSPAP:
							//3G信号
							return "3G";
						case TelephonyManager.NETWORK_TYPE_LTE:
							//4G信号
							return "4G";
						default:
							return "unknown";
					}
				}

			}
			return "connect_off";
		}

	}
}
